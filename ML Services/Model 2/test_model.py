"""
Stage 15 -- test_model.py
Load saved model + encoders + threshold, preprocess a raw input sample,
and display Prediction / Probability / Confidence.

The preprocess_input() function here is the canonical preprocessing function
for this project. Stage 16 (FastAPI backend) will import it directly.
"""

import os, joblib, json, warnings
import pandas as pd
import numpy as np
warnings.filterwarnings("ignore")

# =============================================================================
# LOAD ARTIFACTS (model + encoders + thresholds)
# All loaded once at module level -- mirrors how a FastAPI app loads on startup
# =============================================================================
MODEL_PATH     = "artifacts/donation_success_model_tuned.pkl"
OHE_PATH       = "encoders/ohe_encoder.pkl"
ORD_MAP_PATHS  = {
    "packaging_quality": "encoders/ordinal_map_packaging_quality.pkl",
    "storage_condition": "encoders/ordinal_map_storage_condition.pkl",
    "traffic_level"    : "encoders/ordinal_map_traffic_level.pkl",
    "weather_condition": "encoders/ordinal_map_weather_condition.pkl",
}
THRESHOLD_PATH  = "artifacts/thresholds.pkl"
X_COL_PATH      = "artifacts/X_processed.pkl"

model      = joblib.load(MODEL_PATH)
ohe        = joblib.load(OHE_PATH)
ord_maps   = {col: joblib.load(path) for col, path in ORD_MAP_PATHS.items()}
thresholds = joblib.load(THRESHOLD_PATH)
FEATURE_COLS = joblib.load(X_COL_PATH).columns.tolist()

THRESHOLD = round(float(thresholds["optimal_failure"]), 2)   # 0.56 -- maximises Failure F1


# =============================================================================
# PREPROCESSING FUNCTION
# Canonical function -- reused in Stage 16 FastAPI handler as-is.
# Input : raw dict (exactly as received from Spring Boot JSON body)
# Output: pd.DataFrame with one row, correctly encoded, columns ordered
# =============================================================================
def preprocess_input(raw: dict) -> pd.DataFrame:
    """
    Apply identical preprocessing to a raw donation dict.
    Steps mirror preprocessing.py exactly:
      1. Ordinal encoding  -- packaging_quality, storage_condition,
                              traffic_level, weather_condition
      2. One-Hot encoding  -- food_category, day_of_week
      3. Column ordering   -- enforce same order as training feature matrix
    Raises ValueError if an unknown category is passed.
    """
    df = pd.DataFrame([raw])

    # step 1: ordinal encoding
    for col, mapping in ord_maps.items():
        df[col] = df[col].map(mapping)
        if df[col].isnull().any():
            raise ValueError(
                f"Unknown value '{raw[col]}' for field '{col}'. "
                f"Accepted values: {list(mapping.keys())}"
            )

    # step 2: one-hot encoding
    ohe_input    = df[["food_category", "day_of_week"]]
    ohe_array    = ohe.transform(ohe_input)
    ohe_colnames = ohe.get_feature_names_out(["food_category", "day_of_week"])
    ohe_df       = pd.DataFrame(ohe_array, columns=ohe_colnames, index=df.index)
    df           = df.drop(columns=["food_category", "day_of_week"])
    df           = pd.concat([df, ohe_df], axis=1)

    # step 2.5: domain feature engineering
    travel_hours = df["estimated_travel_time_minutes"] / 60.0
    df["shelf_life_travel_margin"] = df["remaining_shelf_life_hours"] / (travel_hours + 0.01)
    df["perishability_urgency"] = df["food_age_hours"] / (df["remaining_shelf_life_hours"] + 0.01)
    df["ngo_capacity_ratio"] = df["ngo_capacity"] / (df["quantity_meals"] + 0.01)
    df["risk_severity_index"] = df["traffic_level"] * df["weather_condition"]

    # step 3: enforce training column order
    df = df[FEATURE_COLS]

    return df


# =============================================================================
# PREDICT FUNCTION
# Returns a clean result dict -- directly serialisable to JSON for FastAPI.
# =============================================================================
def predict(raw: dict, threshold: float = THRESHOLD) -> dict:
    """
    Run full inference pipeline on a raw donation dict.
    Returns:
        prediction        : 1 (Success) or 0 (Failure)
        label             : "SUCCESS" or "FAILURE"
        probability_success: P(donation succeeds)
        probability_failure: P(donation fails)
        confidence        : probability of the predicted class
        threshold_used    : threshold applied for the binary decision
        risk_flag         : True if predicted Failure
    """
    X_encoded = preprocess_input(raw)
    proba_success = float(model.predict_proba(X_encoded)[0, 1])
    proba_failure = round(1 - proba_success, 4)
    proba_success = round(proba_success, 4)

    # decision: flag as Failure if P(failure) >= threshold
    is_failure  = proba_failure >= threshold
    prediction  = 0 if is_failure else 1
    label       = "FAILURE" if is_failure else "SUCCESS"
    confidence  = round((proba_failure if is_failure else proba_success) * 100, 2)

    return {
        "prediction"         : prediction,
        "label"              : label,
        "probability_success": proba_success,
        "probability_failure": proba_failure,
        "confidence_pct"     : confidence,
        "threshold_used"     : threshold,
        "risk_flag"          : bool(is_failure),
    }


# =============================================================================
# DISPLAY FUNCTION
# =============================================================================
def display_result(sample_name: str, raw: dict, result: dict) -> None:
    print()
    print("+" + "-"*58 + "+")
    print(f"|  SAMPLE : {sample_name:<47}|")
    print("+" + "-"*58 + "+")
    print(f"|  Prediction        : {result['label']:<36}|")
    print(f"|  Probability       : Success={result['probability_success']:.4f}  "
          f"Failure={result['probability_failure']:.4f}  |")
    print(f"|  Confidence        : {result['confidence_pct']:.2f}%{'':<34}|")
    print(f"|  Risk Flag         : {'YES -- Review before dispatch' if result['risk_flag'] else 'NO  -- Safe to dispatch':<36}|")
    print(f"|  Threshold Used    : {result['threshold_used']:<36}|")
    print("+" + "-"*58 + "+")
    print(f"|  JSON Response Preview:                                  |")
    json_str = json.dumps(result, indent=2)
    for line in json_str.split("\n"):
        print(f"|    {line:<54}|")
    print("+" + "-"*58 + "+")


# =============================================================================
# STAGE 15 -- RUN INFERENCE ON ONE NEW SAMPLE
# =============================================================================
if __name__ == "__main__":
    print("="*60)
    print("STAGE 15 -- Load Model + Predict New Sample")
    print("="*60)
    print(f"Model loaded    : {MODEL_PATH}")
    print(f"Threshold used  : {THRESHOLD} (optimal Failure F1 threshold)")
    print(f"Feature columns : {len(FEATURE_COLS)} features")

    # ── New raw sample -- as received from Spring Boot ────────────────────────
    # This is the exact format Spring Boot sends as a JSON body.
    # All values are raw strings/numbers -- no pre-encoding done by the caller.
    new_sample = {
        "food_category"                  : "Cooked Meals",
        "quantity_meals"                 : 150,
        "food_age_hours"                 : 4.5,
        "remaining_shelf_life_hours"     : 6.0,
        "packaging_quality"              : "Average",
        "storage_condition"              : "Room Temperature",
        "donor_reliability_score"        : 0.55,
        "ngo_capacity"                   : 200,
        "ngo_available_vehicles"         : 1,
        "ngo_active_volunteers"          : 3,
        "ngo_average_pickup_delay_minutes": 80.0,
        "ngo_completion_rate"            : 0.60,
        "ngo_trust_score"                : 0.58,
        "distance_km"                    : 18.0,
        "estimated_travel_time_minutes"  : 75.0,
        "traffic_level"                  : "High",
        "weather_condition"              : "Rain",
        "pickup_hour"                    : 20,
        "day_of_week"                    : "Saturday",
        "festival_day"                   : 1,
    }

    print("\nRaw Input Sample:")
    for k, v in new_sample.items():
        print(f"  {k:<40}: {v}")

    # run prediction
    result = predict(new_sample)

    # display structured output
    display_result("New Donation Request", new_sample, result)

    print()
    print("="*60)
    print("STAGE 15 COMPLETE")
    print("="*60)