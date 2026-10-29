import os, joblib, warnings
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from sklearn.metrics import (precision_recall_curve, average_precision_score,
                             precision_score, recall_score, f1_score,
                             confusion_matrix)
warnings.filterwarnings("ignore")

os.makedirs("plots", exist_ok=True)

print("="*60)
print("STAGE 14 -- Save / Load / Predict / PR Curve")
print("="*60)

# ─────────────────────────────────────────────────────────────────────────────
# STEP 1 -- Confirm saved artifacts exist
# ─────────────────────────────────────────────────────────────────────────────
required = [
    "artifacts/donation_success_model_tuned.pkl",
    "artifacts/tuned_model_metrics.pkl",
    "encoders/ohe_encoder.pkl",
    "encoders/ordinal_map_packaging_quality.pkl",
    "encoders/ordinal_map_storage_condition.pkl",
    "encoders/ordinal_map_traffic_level.pkl",
    "encoders/ordinal_map_weather_condition.pkl",
]
print("\nVerifying saved artifacts:")
all_ok = True
for path in required:
    exists = os.path.exists(path)
    size   = os.path.getsize(path) if exists else 0
    status = "OK" if (exists and size > 0) else "MISSING"
    if status != "OK":
        all_ok = False
    print(f"  {status}  {path}  ({size:,} bytes)")
assert all_ok, "One or more artifacts missing -- re-run preprocessing and tuning"

# ─────────────────────────────────────────────────────────────────────────────
# STEP 2 -- Load model and encoders fresh from disk
# ─────────────────────────────────────────────────────────────────────────────
print("\nLoading model and encoders from disk...")
model = joblib.load("artifacts/donation_success_model_tuned.pkl")
ohe   = joblib.load("encoders/ohe_encoder.pkl")
ord_maps = {
    "packaging_quality": joblib.load("encoders/ordinal_map_packaging_quality.pkl"),
    "storage_condition": joblib.load("encoders/ordinal_map_storage_condition.pkl"),
    "traffic_level"    : joblib.load("encoders/ordinal_map_traffic_level.pkl"),
    "weather_condition": joblib.load("encoders/ordinal_map_weather_condition.pkl"),
}
print("Model type       :", type(model).__name__)
n_est = getattr(model, "n_estimators", len(getattr(model, "estimators", [])))
print("Model estimators  :", n_est)
print("OHE categories   :", [list(c) for c in ohe.categories_])

# ─────────────────────────────────────────────────────────────────────────────
# STEP 3 -- Define preprocessing function
# This is the exact function the FastAPI inference server will call.
# Input : raw dict (as received from Spring Boot JSON)
# Output: encoded DataFrame ready for model.predict()
# ─────────────────────────────────────────────────────────────────────────────
def preprocess_input(raw: dict) -> pd.DataFrame:
    """
    Apply identical preprocessing to a raw donation dict.
    Must mirror every step in preprocessing.py exactly.
    """
    df = pd.DataFrame([raw])

    # ordinal encoding (same maps used at training time)
    for col, mapping in ord_maps.items():
        df[col] = df[col].map(mapping)
        if df[col].isnull().any():
            raise ValueError(f"Unknown category in '{col}': {raw[col]}")

    # one-hot encoding (same OHE object fitted at training time)
    ohe_cols    = ["food_category", "day_of_week"]
    ohe_array   = ohe.transform(df[ohe_cols])
    ohe_colnames= ohe.get_feature_names_out(ohe_cols)
    ohe_df      = pd.DataFrame(ohe_array, columns=ohe_colnames, index=df.index)
    df          = df.drop(columns=ohe_cols)
    df          = pd.concat([df, ohe_df], axis=1)

    # domain feature engineering
    travel_hours = df["estimated_travel_time_minutes"] / 60.0
    df["shelf_life_travel_margin"] = df["remaining_shelf_life_hours"] / (travel_hours + 0.01)
    df["perishability_urgency"] = df["food_age_hours"] / (df["remaining_shelf_life_hours"] + 0.01)
    df["ngo_capacity_ratio"] = df["ngo_capacity"] / (df["quantity_meals"] + 0.01)
    df["risk_severity_index"] = df["traffic_level"] * df["weather_condition"]

    # enforce same column order as training
    expected_cols = joblib.load("artifacts/X_processed.pkl").columns.tolist()
    df = df[expected_cols]
    return df

# ─────────────────────────────────────────────────────────────────────────────
# STEP 4 -- New sample prediction (raw input, as Spring Boot would send)
# ─────────────────────────────────────────────────────────────────────────────
# Three samples: one likely success, one borderline, one likely failure
samples = [
    {
        "name": "Sample A -- Likely SUCCESS (low risk)",
        "data": {
            "food_category": "Packaged Food",
            "quantity_meals": 80,
            "food_age_hours": 1.0,
            "remaining_shelf_life_hours": 48.0,
            "packaging_quality": "Good",
            "storage_condition": "Refrigerated",
            "donor_reliability_score": 0.92,
            "ngo_capacity": 500,
            "ngo_available_vehicles": 4,
            "ngo_active_volunteers": 8,
            "ngo_average_pickup_delay_minutes": 10.0,
            "ngo_completion_rate": 0.95,
            "ngo_trust_score": 0.91,
            "distance_km": 3.0,
            "estimated_travel_time_minutes": 12.0,
            "traffic_level": "Low",
            "weather_condition": "Clear",
            "pickup_hour": 10,
            "day_of_week": "Monday",
            "festival_day": 0,
        }
    },
    {
        "name": "Sample B -- Borderline",
        "data": {
            "food_category": "Cooked Meals",
            "quantity_meals": 120,
            "food_age_hours": 3.5,
            "remaining_shelf_life_hours": 8.0,
            "packaging_quality": "Average",
            "storage_condition": "Room Temperature",
            "donor_reliability_score": 0.65,
            "ngo_capacity": 300,
            "ngo_available_vehicles": 2,
            "ngo_active_volunteers": 4,
            "ngo_average_pickup_delay_minutes": 45.0,
            "ngo_completion_rate": 0.72,
            "ngo_trust_score": 0.70,
            "distance_km": 12.0,
            "estimated_travel_time_minutes": 38.0,
            "traffic_level": "Medium",
            "weather_condition": "Cloudy",
            "pickup_hour": 14,
            "day_of_week": "Friday",
            "festival_day": 0,
        }
    },
    {
        "name": "Sample C -- Likely FAILURE (high risk)",
        "data": {
            "food_category": "Cooked Meals",
            "quantity_meals": 200,
            "food_age_hours": 8.0,
            "remaining_shelf_life_hours": 2.5,
            "packaging_quality": "Poor",
            "storage_condition": "Improper",
            "donor_reliability_score": 0.30,
            "ngo_capacity": 100,
            "ngo_available_vehicles": 0,
            "ngo_active_volunteers": 1,
            "ngo_average_pickup_delay_minutes": 120.0,
            "ngo_completion_rate": 0.40,
            "ngo_trust_score": 0.35,
            "distance_km": 35.0,
            "estimated_travel_time_minutes": 140.0,
            "traffic_level": "High",
            "weather_condition": "Storm",
            "pickup_hour": 22,
            "day_of_week": "Sunday",
            "festival_day": 1,
        }
    },
]

print("\n--- Predictions on New Samples (threshold = 0.50) ---")
print(f"{'Sample':<38} {'P(success)':>11} {'Pred(0.5)':>10} {'Confidence':>12}")
print("-"*76)

for s in samples:
    X_new   = preprocess_input(s["data"])
    proba   = model.predict_proba(X_new)[0, 1]
    pred_05 = int(proba >= 0.50)
    label   = "SUCCESS" if pred_05 == 1 else "FAILURE"
    conf    = proba if pred_05 == 1 else (1 - proba)
    print(f"  {s['name']:<36} {proba:>11.4f} {label:>10} {conf*100:>10.1f}%")

# ─────────────────────────────────────────────────────────────────────────────
# STEP 5 -- Precision-Recall Curve + Alternate Threshold Analysis
# ─────────────────────────────────────────────────────────────────────────────
print("\n--- Precision-Recall Curve Analysis (Failure class) ---")

y_test   = joblib.load("artifacts/y_test.pkl")
X_test   = joblib.load("artifacts/X_test.pkl")
y_proba  = model.predict_proba(X_test)[:, 1]

# For Failure class (0), precision/recall are computed w.r.t. predicting 0
# So we invert: P(failure) = 1 - P(success)
y_proba_failure = 1 - y_proba
y_binary_failure = (y_test == 0).astype(int)   # 1 where actual = Failure

precision_curve, recall_curve, thresholds_pr = precision_recall_curve(
    y_binary_failure, y_proba_failure
)
ap_score = average_precision_score(y_binary_failure, y_proba_failure)
print(f"Average Precision (Failure class): {ap_score:.4f}")

# Find threshold that maximises Failure F1
f1_scores_pr = np.where(
    (precision_curve + recall_curve) > 0,
    2 * precision_curve * recall_curve / (precision_curve + recall_curve),
    0
)
best_idx       = np.argmax(f1_scores_pr[:-1])   # exclude last (undefined threshold)
best_threshold = thresholds_pr[best_idx]
best_f1        = f1_scores_pr[best_idx]
best_prec      = precision_curve[best_idx]
best_rec       = recall_curve[best_idx]

print(f"\nDefault threshold (0.50) -- treating P(success)<0.5 as Failure:")
y_pred_default = (y_proba < 0.50).astype(int)   # predicted Failure when P(success)<0.5
p_def = precision_score(y_binary_failure, y_pred_default, zero_division=0)
r_def = recall_score(y_binary_failure, y_pred_default, zero_division=0)
f_def = f1_score(y_binary_failure, y_pred_default, zero_division=0)
print(f"  Failure Precision: {p_def:.4f}  Recall: {r_def:.4f}  F1: {f_def:.4f}")

# Alternate threshold: higher failure-prob cutoff -> higher precision
alt_threshold = round(best_threshold, 2)
y_pred_alt = (y_proba_failure >= alt_threshold).astype(int)
p_alt = precision_score(y_binary_failure, y_pred_alt, zero_division=0)
r_alt = recall_score(y_binary_failure, y_pred_alt, zero_division=0)
f_alt = f1_score(y_binary_failure, y_pred_alt, zero_division=0)

print(f"\nOptimal threshold ({alt_threshold:.2f}) -- maximises Failure F1:")
print(f"  Failure Precision: {p_alt:.4f}  Recall: {r_alt:.4f}  F1: {f_alt:.4f}")

print(f"\nPercentage-point change from threshold adjustment:")
print(f"  Precision: {(p_alt-p_def)*100:+.2f}pp")
print(f"  Recall   : {(r_alt-r_def)*100:+.2f}pp")
print(f"  F1       : {(f_alt-f_def)*100:+.2f}pp")

# PR curve plot
fig, axes = plt.subplots(1, 2, figsize=(15, 6))
fig.suptitle("Precision-Recall Curve -- Failure Class", fontsize=12, fontweight="bold")

axes[0].plot(recall_curve, precision_curve, color="#1f77b4", linewidth=2,
             label=f"PR Curve (AP={ap_score:.3f})")
axes[0].scatter(r_def, p_def, color="green",  s=120, zorder=5,
                label=f"Default threshold=0.50  (P={p_def:.3f}, R={r_def:.3f})")
axes[0].scatter(best_rec, best_prec, color="red", s=120, marker="*", zorder=5,
                label=f"Optimal threshold={alt_threshold:.2f} (P={p_alt:.3f}, R={r_alt:.3f})")
axes[0].axhline(0.5, color="grey", linestyle="--", linewidth=1, alpha=0.5, label="Precision=0.5 line")
axes[0].set_xlabel("Recall (Failure)")
axes[0].set_ylabel("Precision (Failure)")
axes[0].set_title("Precision vs Recall (Failure Class)")
axes[0].legend(fontsize=8)
axes[0].set_xlim([0, 1])
axes[0].set_ylim([0, 1])

# F1 vs threshold plot
axes[1].plot(thresholds_pr, f1_scores_pr[:-1], color="#d62728", linewidth=2)
axes[1].axvline(alt_threshold, color="red", linestyle="--", linewidth=1.5,
                label=f"Best threshold={alt_threshold:.2f}  F1={best_f1:.3f}")
axes[1].axvline(0.50, color="green", linestyle="--", linewidth=1.5,
                label=f"Default=0.50  F1={f_def:.3f}")
axes[1].set_xlabel("Threshold (P(Failure))")
axes[1].set_ylabel("Failure F1 Score")
axes[1].set_title("Failure F1 Score vs Classification Threshold")
axes[1].legend(fontsize=9)

plt.tight_layout()
plt.savefig("plots/precision_recall_curve.png", bbox_inches="tight")
print("\nPR curve saved -> plots/precision_recall_curve.png")

# ─────────────────────────────────────────────────────────────────────────────
# STEP 6 -- Re-predict samples at optimal threshold
# ─────────────────────────────────────────────────────────────────────────────
print(f"\n--- Predictions at Optimal Threshold ({alt_threshold:.2f}) ---")
print(f"{'Sample':<38} {'P(success)':>11} {'P(failure)':>11} {'Pred':>10} {'Confidence':>12}")
print("-"*86)
for s in samples:
    X_new  = preprocess_input(s["data"])
    proba  = model.predict_proba(X_new)[0, 1]
    p_fail = 1 - proba
    pred   = "FAILURE" if p_fail >= alt_threshold else "SUCCESS"
    conf   = p_fail if pred == "FAILURE" else proba
    print(f"  {s['name']:<36} {proba:>11.4f} {p_fail:>11.4f} {pred:>10} {conf*100:>10.1f}%")

# save threshold for inference server
joblib.dump({"default": 0.50, "optimal_failure": float(alt_threshold)},
            "artifacts/thresholds.pkl")
print("\nThresholds saved -> artifacts/thresholds.pkl")

print("\n" + "="*60)
print("STAGE 14 COMPLETE")
print("="*60)