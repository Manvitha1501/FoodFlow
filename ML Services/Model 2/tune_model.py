import os, joblib, warnings, time
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from sklearn.model_selection import RandomizedSearchCV, StratifiedKFold
from sklearn.metrics import (accuracy_score, precision_score, recall_score,
                             f1_score, roc_auc_score, confusion_matrix,
                             classification_report, ConfusionMatrixDisplay)
from xgboost import XGBClassifier
warnings.filterwarnings("ignore")

os.makedirs("artifacts", exist_ok=True)
os.makedirs("plots", exist_ok=True)

X_train = joblib.load("artifacts/X_train.pkl")
X_test  = joblib.load("artifacts/X_test.pkl")
y_train = joblib.load("artifacts/y_train.pkl")
y_test  = joblib.load("artifacts/y_test.pkl")
spw     = joblib.load("artifacts/scale_pos_weight.pkl")

print("STAGE 13 -- Hyperparameter Tuning")
print("Train:", X_train.shape, "| scale_pos_weight fixed:", spw)
print("Scoring: roc_auc")

param_dist = {
    "n_estimators"    : [200, 300, 400, 500, 600],
    "max_depth"       : [3, 4, 5, 6, 7],
    "learning_rate"   : [0.01, 0.03, 0.05, 0.08, 0.10],
    "subsample"       : [0.6, 0.7, 0.8, 0.9, 1.0],
    "colsample_bytree": [0.6, 0.7, 0.8, 0.9, 1.0],
    "min_child_weight": [1, 3, 5, 7, 10],
    "gamma"           : [0, 0.1, 0.5, 1.0, 2.0, 5.0],
}

base_model = XGBClassifier(
    scale_pos_weight=spw, eval_metric="logloss",
    random_state=42, n_jobs=-1, verbosity=0
)

cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)
search = RandomizedSearchCV(
    estimator=base_model, param_distributions=param_dist,
    n_iter=50, scoring="roc_auc", cv=cv,
    random_state=42, n_jobs=-1, verbose=1, refit=True
)

print("Running 50 iter x 5 folds = 250 fits...")
t0 = time.time()
search.fit(X_train, y_train)
elapsed = round(time.time() - t0, 1)
print("Done in", elapsed, "s")

best_params = search.best_params_
print("Best params:", best_params)
print("Best CV ROC-AUC:", round(search.best_score_, 4))

best_model = search.best_estimator_
y_pred_tuned  = best_model.predict(X_test)
y_proba_tuned = best_model.predict_proba(X_test)[:, 1]

acc_t  = accuracy_score(y_test, y_pred_tuned)
prec_t = precision_score(y_test, y_pred_tuned)
rec_t  = recall_score(y_test, y_pred_tuned)
f1_t   = f1_score(y_test, y_pred_tuned)
auc_t  = roc_auc_score(y_test, y_proba_tuned)
prec_f = precision_score(y_test, y_pred_tuned, pos_label=0, zero_division=0)
rec_f  = recall_score(y_test, y_pred_tuned, pos_label=0, zero_division=0)
f1_f   = f1_score(y_test, y_pred_tuned, pos_label=0, zero_division=0)

default_metrics = joblib.load("artifacts/model_metrics.pkl")
default_model   = joblib.load("artifacts/donation_success_model.pkl")
y_pred_def      = default_model.predict(X_test)
y_proba_def     = default_model.predict_proba(X_test)[:, 1]
prec_f_def = precision_score(y_test, y_pred_def, pos_label=0, zero_division=0)
rec_f_def  = recall_score(y_test, y_pred_def,  pos_label=0, zero_division=0)
f1_f_def   = f1_score(y_test, y_pred_def,      pos_label=0, zero_division=0)

print("\n--- Default vs Tuned (Test Set, pp = percentage points) ---")
rows = [
    ("Accuracy",            default_metrics["accuracy"], acc_t),
    ("Precision (Success)", default_metrics["precision"], prec_t),
    ("Recall (Success)",    default_metrics["recall"],    rec_t),
    ("F1 (Success)",        default_metrics["f1"],        f1_t),
    ("ROC-AUC",             default_metrics["roc_auc"],   auc_t),
    ("Precision (Failure)", prec_f_def,  prec_f),
    ("Recall (Failure)",    rec_f_def,   rec_f),
    ("F1 (Failure)",        f1_f_def,    f1_f),
]
print(f"{'Metric':<26} {'Default':>9} {'Tuned':>9} {'Diff(pp)':>10} {'Result':>10}")
print("-"*68)
for label, d, t in rows:
    diff = round((t - d) * 100, 2)
    tag  = "IMPROVED" if diff > 0.1 else ("DECLINED" if diff < -0.1 else "NO CHANGE")
    print(f"  {label:<24} {d:>9.4f} {t:>9.4f} {diff:>+9.2f}pp  {tag}")

print("\nClassification Report -- Tuned:")
print(classification_report(y_test, y_pred_tuned, target_names=["Failure(0)","Success(1)"]))

cm_def   = confusion_matrix(y_test, y_pred_def)
cm_tuned = confusion_matrix(y_test, y_pred_tuned)
tn,fp,fn,tp = cm_tuned.ravel()
print("Tuned CM -- TN:", tn, "FP:", fp, "FN:", fn, "TP:", tp)

fig, axes = plt.subplots(1, 2, figsize=(14, 6))
fig.suptitle("Confusion Matrix: Default vs Tuned XGBoost", fontsize=12, fontweight="bold")
for ax, cm, title in zip(axes, [cm_def, cm_tuned], ["Default", "Tuned"]):
    ConfusionMatrixDisplay(cm, display_labels=["Failure","Success"]).plot(ax=ax, colorbar=False, cmap="Blues")
    ax.set_title(title)
plt.tight_layout()
plt.savefig("plots/confusion_matrix_comparison.png", bbox_inches="tight")
print("Saved -> plots/confusion_matrix_comparison.png")

imp = pd.Series(best_model.feature_importances_, index=X_train.columns).sort_values(ascending=False)
print("\nTop 15 features (tuned):")
print(imp.head(15).round(4).to_string())
fig, ax = plt.subplots(figsize=(12, 8))
imp.head(20).sort_values().plot(kind="barh", ax=ax, color="#5b8db8", edgecolor="white")
ax.set_title("Feature Importance -- Tuned XGBoost (Top 20)")
plt.tight_layout()
plt.savefig("plots/feature_importance_tuned.png", bbox_inches="tight")
print("Saved -> plots/feature_importance_tuned.png")

joblib.dump(best_model, "artifacts/donation_success_model_tuned.pkl")
tuned_metrics = {
    "accuracy":round(acc_t,4), "precision":round(prec_t,4),
    "recall":round(rec_t,4),   "f1":round(f1_t,4),
    "roc_auc":round(auc_t,4),  "precision_failure":round(prec_f,4),
    "recall_failure":round(rec_f,4), "f1_failure":round(f1_f,4),
    "best_params":best_params
}
joblib.dump(tuned_metrics, "artifacts/tuned_model_metrics.pkl")
print("Saved -> artifacts/donation_success_model_tuned.pkl")
print("Saved -> artifacts/tuned_model_metrics.pkl")
print("STAGE 13 COMPLETE")