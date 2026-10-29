"""
prepare_readiness_dataset.py
============================
Merges food_spoilage.csv + Dataset.csv (sensor telemetry) + extracted_facts.json
into a single realistic training CSV for Model 2 (Donation Readiness).

The key design principle: the label is derived from PHYSICAL measurements and
domain rules, NOT from any single column that would leak the answer.  The raw
'risk' and 'class' columns are used only during label construction and are
**never** included in the output feature set.

Output:  d:\\lovely\\ml\\data\\model2_training_data.csv
"""

import os
import sys
import json
import numpy as np
import pandas as pd

# ---- Resolve paths ----
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
DATA_ROOT = os.path.join(PROJECT_ROOT, "data") if os.path.isfile(os.path.join(PROJECT_ROOT, "data", "extracted_facts.json")) else os.path.join(os.path.dirname(PROJECT_ROOT), "data")
if not os.path.isfile(os.path.join(DATA_ROOT, "extracted_facts.json")):
    DATA_ROOT = os.path.dirname(PROJECT_ROOT)

SPOILAGE_CSV = os.path.join(DATA_ROOT, "food_spoilage.csv")
SENSOR_CSV = os.path.join(DATA_ROOT, "Dataset.csv")
FACTS_JSON = os.path.join(DATA_ROOT, "extracted_facts.json")
OUTPUT_DIR = os.path.join(PROJECT_ROOT, "data")
OUTPUT_CSV = os.path.join(OUTPUT_DIR, "donation_readiness_training_data.csv")

RNG = np.random.default_rng(42)


# ---------------------------------------------------------------------------
# 1. LOAD SOURCES
# ---------------------------------------------------------------------------
def load_who_thresholds(path):
    with open(path, encoding="utf-8") as f:
        facts = json.load(f)
    thresholds = {
        "danger_zone_low": 5.0,
        "danger_zone_high": 60.0,
        "max_room_temp_hours": 2.0,
    }
    for fact in facts:
        if fact.get("rule_type") == "danger_zone_range" and fact.get("food_type") == "general":
            thresholds["danger_zone_low"], thresholds["danger_zone_high"] = fact["value"]
        if fact.get("rule_type") == "max_room_temp_holding_time" and fact.get("food_type") == "general":
            thresholds["max_room_temp_hours"] = fact["value"]
    return thresholds


def parse_temp(v):
    """Parse messy temperature strings like '1-4', '≤4', '≤4 / -18'."""
    v = str(v).replace("\u2264", "").replace("<=", "").strip()
    if v.lower().startswith("room"):
        return 22.0  # approximate room temperature
    if "-" in v and not v.startswith("-"):
        parts = v.split("-")
        try:
            return (float(parts[0]) + float(parts[1])) / 2
        except (ValueError, IndexError):
            pass
    if "/" in v:
        for part in v.split("/"):
            part = part.strip().replace("\u2264", "").replace("<=", "")
            try:
                return float(part)
            except ValueError:
                continue
        return np.nan
    try:
        return float(v)
    except ValueError:
        return np.nan


def load_spoilage(path):
    df = pd.read_csv(path)
    df.columns = [c.strip().lower() for c in df.columns]
    df["recommended_storage_temp_c"] = df["storage_temperature_c"].apply(parse_temp)
    df["max_fridge_days"] = pd.to_numeric(df["max_fridge_days"], errors="coerce")
    df["max_freezer_months"] = pd.to_numeric(df["max_freezer_months"], errors="coerce")
    return df


def load_sensor(path):
    df = pd.read_csv(path)
    df.columns = [c.strip().lower() for c in df.columns]
    # Normalize class labels
    df["class"] = df["class"].str.strip().str.lower().map({"good": "Good", "bad": "Bad"})
    df = df.dropna(subset=["class"])
    return df


# ---------------------------------------------------------------------------
# 2. CROSS-JOIN + REALISTIC NOISE
# ---------------------------------------------------------------------------
def build_combined_dataset(spoilage_df, sensor_df, thresholds, multiplier=5):
    """
    Assign each sensor reading a random food item from the spoilage table.
    Then add realistic noise and derived features.
    
    Args:
        multiplier: Generate multiplier * len(sensor_df) synthetic samples
                    to create a larger, more diverse training set
    """
    n_sensor = len(sensor_df)
    n_spoilage = len(spoilage_df)
    total_samples = n_sensor * multiplier

    # Randomly assign each sensor reading to a food item (simulating IoT
    # sensors monitoring various food storage units)
    food_indices = RNG.integers(0, n_spoilage, size=total_samples)
    sensor_indices = RNG.integers(0, n_sensor, size=total_samples)

    rows = []
    for i in range(total_samples):
        s_row = sensor_df.iloc[sensor_indices[i]]
        f_row = spoilage_df.iloc[food_indices[i]]

        # --- Raw sensor values with realistic cold-chain + ambient storage scenarios ---
        storage_temp_mode = RNG.choice(['cold_chain', 'cool_fridge', 'ambient'], p=[0.30, 0.35, 0.35])
        if storage_temp_mode == 'cold_chain':
            temp_c = RNG.normal(3.0, 1.0)
        elif storage_temp_mode == 'cool_fridge':
            temp_c = RNG.normal(7.0, 1.5)
        else:
            temp_c = RNG.normal(24.0, 3.5)
        temp_c = np.clip(temp_c, -5.0, 45.0)
        
        humidity_pct = float(s_row.get("humid (%)", s_row.get("humid", 85)))
        humidity_variation = RNG.choice([
            RNG.normal(0, 2.0),      # normal variation
            RNG.normal(10, 3.0),     # high humidity event
            RNG.normal(-15, 3.0),    # low humidity event
        ], p=[0.7, 0.15, 0.15])
        humidity_pct = np.clip(humidity_pct + humidity_variation, 30, 100)
        
        co2_raw = float(s_row.get("co2 (pmm)", s_row.get("co2 (ppm)", s_row.get("co2", 350))))
        co2_variation = RNG.choice([
            RNG.normal(0, 20),       # normal variation
            RNG.normal(100, 40),     # spoilage event
            RNG.normal(200, 60),     # severe spoilage
        ], p=[0.7, 0.2, 0.1])
        co2_ppm = max(0, co2_raw + co2_variation)
        
        light_lux = float(s_row.get("light (fux)", s_row.get("light", 10)))
        light_lux = max(0, light_lux + RNG.normal(0, 5))

        # --- Derived features with more realistic temporal distribution ---
        # Expand time range and create more varied storage durations
        max_fridge_days = f_row["max_fridge_days"]
        if pd.notna(max_fridge_days) and max_fridge_days > 0:
            max_fridge_hours = max_fridge_days * 24
            # Sample from different storage duration scenarios
            storage_scenario = RNG.choice([
                'fresh',      # 0-20% of shelf life
                'middle',     # 20-60% of shelf life
                'aging',      # 60-90% of shelf life
                'critical',   # 90-120% of shelf life (past expiry)
            ], p=[0.3, 0.4, 0.2, 0.1])
            
            if storage_scenario == 'fresh':
                hours_since_stored = RNG.uniform(0, max_fridge_hours * 0.2)
            elif storage_scenario == 'middle':
                hours_since_stored = RNG.uniform(max_fridge_hours * 0.2, max_fridge_hours * 0.6)
            elif storage_scenario == 'aging':
                hours_since_stored = RNG.uniform(max_fridge_hours * 0.6, max_fridge_hours * 0.9)
            else:  # critical
                hours_since_stored = RNG.uniform(max_fridge_hours * 0.9, max_fridge_hours * 1.2)
            
            remaining_fridge_life_pct = max(0, 1.0 - hours_since_stored / max_fridge_hours) * 100
        else:
            hours_since_stored = float(RNG.uniform(0, 120))
            remaining_fridge_life_pct = 50.0  # unknown shelf life → assume mid

        # Batch weight: more realistic distribution for food donations
        batch_weight_scenario = RNG.choice([
            'small',      # individual items
            'medium',     # small batch
            'large',      # bulk donation
        ], p=[0.4, 0.4, 0.2])
        
        if batch_weight_scenario == 'small':
            batch_weight_kg = float(RNG.lognormal(mean=0.5, sigma=0.5))
            batch_weight_kg = np.clip(batch_weight_kg, 0.2, 5.0)
        elif batch_weight_scenario == 'medium':
            batch_weight_kg = float(RNG.lognormal(mean=1.8, sigma=0.6))
            batch_weight_kg = np.clip(batch_weight_kg, 3.0, 20.0)
        else:  # large
            batch_weight_kg = float(RNG.lognormal(mean=3.0, sigma=0.7))
            batch_weight_kg = np.clip(batch_weight_kg, 15.0, 100.0)

        food_item = f_row["food_item"]
        food_category = f_row["category"]
        sensor_class = str(s_row["class"])  # "Good" or "Bad" — used ONLY for label, NOT as feature
        risk_level = str(f_row["risk"]).lower()

        rows.append({
            "temperature_c": round(temp_c, 2),
            "humidity_pct": round(humidity_pct, 2),
            "co2_ppm": round(co2_ppm, 1),
            "light_lux": round(light_lux, 2),
            "hours_since_stored": round(hours_since_stored, 2),
            "remaining_fridge_life_pct": round(remaining_fridge_life_pct, 2),
            "batch_weight_kg": round(batch_weight_kg, 2),
            "food_item": food_item,
            "food_category": food_category,
            # --- Label-construction helpers (will be dropped from features) ---
            "_sensor_class": sensor_class,
            "_risk_level": risk_level,
            "_recommended_storage_temp_c": f_row["recommended_storage_temp_c"],
        })

    df = pd.DataFrame(rows)
    return df


# ---------------------------------------------------------------------------
# 3. LABEL CONSTRUCTION — nuanced, multi-signal, no single-column shortcut
# ---------------------------------------------------------------------------
# Category-based risk modifier (higher = riskier food, more penalty)
CATEGORY_RISK = {
    "poultry_cooked": 1.4,
    "poultry": 1.3,
    "dairy": 1.2,
    "cooked_starch": 1.1,
    "produce": 1.0,
    "canned": 0.7,
}


def compute_readiness_score(row, thresholds):
    """
    Compute a continuous readiness score 0-100 from multiple physical signals.
    The label is then discretized from this score.

    Key design: no single feature determines the label alone.
    Every signal contributes a PARTIAL penalty, and noise ensures overlap
    between label boundaries.
    
    Enhanced with more nuanced edge case handling and interaction effects.
    """
    score = 100.0
    temp = row["temperature_c"]
    dz_low = thresholds["danger_zone_low"]
    dz_high = thresholds["danger_zone_high"]
    rec_temp = row.get("_recommended_storage_temp_c", 4.0)
    if pd.isna(rec_temp):
        rec_temp = 4.0

    # --- Temperature penalty (graduated, not binary) ---
    if dz_low <= temp <= dz_high:
        # Deep inside danger zone = worse
        dz_mid = (dz_low + dz_high) / 2
        depth = 1.0 - abs(temp - dz_mid) / (dz_mid - dz_low)
        score -= 15 + 15 * depth  # 15-30 penalty
    else:
        # Outside danger zone but far from recommended storage temp
        temp_deviation = abs(temp - rec_temp)
        if temp_deviation > 10:
            score -= min(10, temp_deviation * 0.5)

    # --- Hours since stored penalty (with accelerated decay) ---
    max_hours = thresholds["max_room_temp_hours"]
    hours = row["hours_since_stored"]
    if hours > max_hours:
        overtime_ratio = min((hours - max_hours) / (max_hours * 10), 1.0)
        score -= 20 * overtime_ratio  # up to -20

    # --- Remaining fridge life penalty (non-linear decay) ---
    rfl = row["remaining_fridge_life_pct"]
    if rfl < 20:
        score -= 15 * (1 - rfl / 20)  # up to -15 when near 0%
    elif rfl < 50:
        score -= 5 * (1 - rfl / 50)   # up to -5

    # --- CO2 penalty (elevated CO2 = spoilage gas) ---
    co2 = row["co2_ppm"]
    if co2 > 400:
        score -= min(15, (co2 - 400) * 0.15)  # up to -15
    elif co2 > 300:
        score -= (co2 - 300) * 0.03  # up to -3

    # --- Humidity penalty (extremes are bad) ---
    humidity = row["humidity_pct"]
    if humidity > 95:
        score -= 5  # condensation risk
    elif humidity < 60:
        score -= 3  # too dry for many foods

    # --- NEW: Interaction effects (compound penalties) ---
    # High temp + high humidity = accelerated spoilage
    if temp > dz_low and humidity > 85:
        score -= 8
    
    # High CO2 + low remaining life = strong spoilage signal
    if co2 > 500 and rfl < 30:
        score -= 10
    
    # Long storage + temperature abuse = severe risk
    if hours > 48 and (temp < 0 or temp > dz_high):
        score -= 12

    # --- Category-based risk multiplier ---
    cat = row.get("food_category", "produce")
    risk_mult = CATEGORY_RISK.get(cat, 1.0)
    # Apply multiplier only to the penalty portion
    penalty = 100 - score
    score = 100 - penalty * risk_mult

    # --- Sensor class influence (partial, NOT deterministic) ---
    # Bad sensor readings add a moderate penalty, but don't auto-set the label
    if row.get("_sensor_class") == "Bad":
        score -= 12

    # --- Risk level influence (partial, NOT deterministic) ---
    if "high" in str(row.get("_risk_level", "")):
        score -= 10

    # --- Add small random noise to prevent perfect boundaries ---
    score += RNG.normal(0, 5)
    score = np.clip(score, 0, 100)

    return round(float(score), 2)


def discretize_label(score):
    """Convert continuous score to Low/Medium/High with fuzzy boundaries."""
    if score >= 65:
        return "High"
    elif score >= 40:
        return "Medium"
    else:
        return "Low"


# ---------------------------------------------------------------------------
# MAIN
# ---------------------------------------------------------------------------
def main():
    # Configuration
    DATA_MULTIPLIER = 10  # Generate 10x more data for better model training
    
    print("=" * 60)
    print("  Preparing Model 2 Training Dataset")
    print("=" * 60)
    print(f"Data multiplier: {DATA_MULTIPLIER}x (more diverse training samples)")

    # Load sources
    thresholds = load_who_thresholds(FACTS_JSON)
    print(f"WHO thresholds: {thresholds}")

    spoilage_df = load_spoilage(SPOILAGE_CSV)
    print(f"Spoilage data: {len(spoilage_df)} food items, "
          f"{spoilage_df['food_item'].nunique()} unique foods")

    sensor_df = load_sensor(SENSOR_CSV)
    print(f"Sensor data: {len(sensor_df)} readings, "
          f"{sensor_df['fruit'].nunique()} fruits")

    # Build combined dataset
    print(f"\nBuilding combined dataset with noise & derived features...")
    print(f"Target samples: {len(sensor_df)} × {DATA_MULTIPLIER} = {len(sensor_df) * DATA_MULTIPLIER}")
    df = build_combined_dataset(spoilage_df, sensor_df, thresholds, multiplier=DATA_MULTIPLIER)

    # Compute readiness scores and labels
    print("Computing readiness scores...")
    df["_readiness_score"] = df.apply(
        lambda r: compute_readiness_score(r, thresholds), axis=1
    )
    df["readiness_label"] = df["_readiness_score"].apply(discretize_label)

    # Print diagnostics
    print(f"\nTotal rows: {len(df)}")
    print(f"\nLabel distribution:")
    print(df["readiness_label"].value_counts().to_string())
    print(f"\nScore statistics by label:")
    print(df.groupby("readiness_label")["_readiness_score"].describe().to_string())

    # Drop label-construction helper columns (the ones starting with _)
    feature_cols = [c for c in df.columns if not c.startswith("_")]
    df_out = df[feature_cols]

    # Save
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    df_out.to_csv(OUTPUT_CSV, index=False)
    df_out.to_csv(os.path.join(OUTPUT_DIR, "model2_training_data.csv"), index=False)
    print(f"\nDataset saved: {OUTPUT_CSV}")
    print(f"Shape: {df_out.shape}")
    print(f"Columns: {list(df_out.columns)}")

    # Quick sanity check: verify no obvious leakage columns remain
    leak_cols = [c for c in df_out.columns if c in ("risk", "class", "_sensor_class", "_risk_level")]
    if leak_cols:
        print(f"\n⚠ WARNING: potential leakage columns found: {leak_cols}")
    else:
        print("\n[OK] No leakage columns in output -- features are clean.")

    # Show feature correlations with label
    print("\nFeature-label correlation (point-biserial with numeric label):")
    label_num = df_out["readiness_label"].map({"Low": 0, "Medium": 1, "High": 2})
    numeric_cols = df_out.select_dtypes(include=[np.number]).columns
    for col in numeric_cols:
        corr = df_out[col].corr(label_num)
        print(f"  {col:35s}  r = {corr:+.3f}")


if __name__ == "__main__":
    main()
