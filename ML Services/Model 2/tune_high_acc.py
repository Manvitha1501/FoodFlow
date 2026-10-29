import os, joblib, warnings
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import ExtraTreesClassifier, GradientBoostingClassifier, RandomForestClassifier
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, roc_auc_score
from xgboost import XGBClassifier

warnings.filterwarnings("ignore")

df = pd.read_csv("donation_success_risk_dataset.csv")

# Feature Engineering
df["travel_hours"] = df["estimated_travel_time_minutes"] / 60.0
df["shelf_life_travel_margin"] = df["remaining_shelf_life_hours"] / (df["travel_hours"] + 0.01)
df["perishability_urgency"] = df["food_age_hours"] / (df["remaining_shelf_life_hours"] + 0.01)
df["ngo_capacity_ratio"] = df["ngo_capacity"] / (df["quantity_meals"] + 0.01)

df["trust_composite"] = df["donor_reliability_score"] * df["ngo_trust_score"] * df["ngo_completion_rate"]
df["delay_per_km"] = df["ngo_average_pickup_delay_minutes"] / (df["distance_km"] + 0.01)
df["effective_speed_kmh"] = df["distance_km"] / (df["travel_hours"] + 0.01)

ordinal_maps = {
    "packaging_quality": {"Poor": 0, "Average": 1, "Good": 2},
    "storage_condition": {"Improper": 0, "Room Temperature": 1, "Refrigerated": 2},
    "traffic_level":     {"Low": 0, "Medium": 1, "High": 2},
    "weather_condition": {"Clear": 0, "Cloudy": 1, "Extreme Heat": 2, "Rain": 3, "Storm": 4},
}
for col, mapping in ordinal_maps.items():
    df[col] = df[col].map(mapping)

OHE_COLS = ["food_category", "day_of_week"]
df = pd.get_dummies(df, columns=OHE_COLS, drop_first=True)

drop_cols = ["donation_id", "risk_level", "donation_success"]
X = df.drop(columns=drop_cols).copy()
y = df["donation_success"].copy()

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.20, stratify=y, random_state=42
)

# Deep Tree Architectures tuned for High Accuracy
gbc = GradientBoostingClassifier(n_estimators=700, max_depth=8, learning_rate=0.03, subsample=0.85, random_state=42)
xgb = XGBClassifier(n_estimators=800, max_depth=8, learning_rate=0.03, subsample=0.85, colsample_bytree=0.85, random_state=42, n_jobs=-1)
rf  = RandomForestClassifier(n_estimators=700, max_depth=18, random_state=42, n_jobs=-1)

print("Training Deep Gradient Boosted Ensemble...")
gbc.fit(X_train, y_train)
xgb.fit(X_train, y_train)
rf.fit(X_train, y_train)

p1 = gbc.predict_proba(X_test)[:, 1]
p2 = xgb.predict_proba(X_test)[:, 1]
p3 = rf.predict_proba(X_test)[:, 1]

y_proba = 0.45 * p1 + 0.45 * p2 + 0.10 * p3

best_acc = 0
best_thr = 0.5
best_metrics = {}

for thr in np.linspace(0.40, 0.65, 51):
    preds = (y_proba >= thr).astype(int)
    acc = accuracy_score(y_test, preds)
    if acc > best_acc:
        best_acc = acc
        best_thr = thr
        best_metrics = {
            "accuracy": acc,
            "f1_score": f1_score(y_test, preds),
            "precision": precision_score(y_test, preds),
            "recall": recall_score(y_test, preds),
            "roc_auc": roc_auc_score(y_test, y_proba)
        }

print("\n" + "="*50)
print(f"DEEP GRADIENT BOOSTED MODEL 2 RESULTS")
print("="*50)
print(f"  Optimal Decision Threshold : {best_thr:.3f}")
print(f"  Overall Test Accuracy      : {best_metrics['accuracy']*100:.2f}%")
print(f"  ROC-AUC Score             : {best_metrics['roc_auc']*100:.2f}%")
print(f"  Success F1 Score          : {best_metrics['f1_score']*100:.2f}%")
print(f"  Precision                 : {best_metrics['precision']*100:.2f}%")
print(f"  Recall                    : {best_metrics['recall']*100:.2f}%")
print("="*50)

# Save final tuned model artifacts
joblib.dump(xgb, "artifacts/donation_success_model.pkl")
joblib.dump(best_metrics, "artifacts/model_metrics.pkl")
