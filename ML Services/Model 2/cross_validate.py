import os, joblib, warnings
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from sklearn.model_selection import StratifiedKFold, cross_validate
from sklearn.metrics import (make_scorer, accuracy_score, precision_score,
                             recall_score, f1_score, roc_auc_score)
from xgboost import XGBClassifier
warnings.filterwarnings("ignore")

os.makedirs("plots", exist_ok=True)
os.makedirs("artifacts", exist_ok=True)

# ─── load processed data ──────────────────────────────────────────────────────
X   = joblib.load("artifacts/X_processed.pkl")
y   = joblib.load("artifacts/y_processed.pkl")
spw = joblib.load("artifacts/scale_pos_weight.pkl")

print("="*60)
print("STAGE 12 -- Stratified K-Fold Cross Validation (K=5)")
print("="*60)
print("X:", X.shape, "| y:", y.shape)
print("Class ratio preserved in every fold via StratifiedKFold")

# ─── define scoring metrics ───────────────────────────────────────────────────
# We track all key metrics simultaneously across folds.
# For Failure-class (0) precision/recall/F1 we set pos_label=0.
scoring = {
    "accuracy"        : make_scorer(accuracy_score),
    "precision_success": make_scorer(precision_score, pos_label=1, zero_division=0),
    "recall_success"  : make_scorer(recall_score,    pos_label=1, zero_division=0),
    "f1_success"      : make_scorer(f1_score,        pos_label=1, zero_division=0),
    "precision_failure": make_scorer(precision_score, pos_label=0, zero_division=0),
    "recall_failure"  : make_scorer(recall_score,    pos_label=0, zero_division=0),
    "f1_failure"      : make_scorer(f1_score,        pos_label=0, zero_division=0),
    "roc_auc"         : make_scorer(roc_auc_score, needs_proba=True),
}

# ─── define model (same config as Stage 6) ───────────────────────────────────
model = XGBClassifier(
    n_estimators=300,
    max_depth=5,
    learning_rate=0.05,
    subsample=0.8,
    colsample_bytree=0.8,
    scale_pos_weight=spw,
    eval_metric="logloss",
    random_state=42,
    n_jobs=-1,
    verbosity=0
)

# ─── run cross validation ─────────────────────────────────────────────────────
# cv=5: 5-fold, each fold uses 80% train / 20% test
# return_train_score=False: we only need test scores
cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)

print("\nRunning 5-fold cross validation (this takes ~1-2 minutes)...")
cv_results = cross_validate(model, X, y, cv=cv, scoring=scoring,
                             return_train_score=False, n_jobs=-1)
print("Done.")

# ─── summarise results ────────────────────────────────────────────────────────
metrics_display = {
    "Accuracy"         : "test_accuracy",
    "Precision(Success)": "test_precision_success",
    "Recall(Success)"  : "test_recall_success",
    "F1(Success)"      : "test_f1_success",
    "Precision(Failure)": "test_precision_failure",
    "Recall(Failure)"  : "test_recall_failure",
    "F1(Failure)"      : "test_f1_failure",
    "ROC-AUC"          : "test_roc_auc",
}

print("\n--- Cross Validation Results (5 Folds) ---")
print(f"{'Metric':<22} {'Fold1':>7} {'Fold2':>7} {'Fold3':>7} {'Fold4':>7} {'Fold5':>7}   {'Mean':>7} {'Std':>7}")
print("-"*80)

summary_rows = []
for label, key in metrics_display.items():
    vals = cv_results[key]
    row  = {"metric": label, "mean": vals.mean(), "std": vals.std()}
    row.update({f"fold_{i+1}": vals[i] for i in range(5)})
    summary_rows.append(row)
    folds_str = "  ".join(f"{v:.4f}" for v in vals)
    print(f"{label:<22} {folds_str}   {vals.mean():.4f} {vals.std():.4f}")

summary_df = pd.DataFrame(summary_rows)
joblib.dump(summary_df, "artifacts/cv_results.pkl")

# ─── compare CV mean vs single-split results ─────────────────────────────────
single_split = joblib.load("artifacts/model_metrics.pkl")

print("\n--- CV Mean vs Single-Split Comparison ---")
print(f"{'Metric':<20} {'Single Split':>13} {'CV Mean':>10} {'CV Std':>8}")
print("-"*55)

compare_map = {
    "Accuracy"  : ("accuracy",  "test_accuracy"),
    "Precision" : ("precision", "test_precision_success"),
    "Recall"    : ("recall",    "test_recall_success"),
    "F1"        : ("f1",        "test_f1_success"),
    "ROC-AUC"   : ("roc_auc",   "test_roc_auc"),
}
for label, (ss_key, cv_key) in compare_map.items():
    ss  = single_split[ss_key]
    cv_mean = cv_results[cv_key].mean()
    cv_std  = cv_results[cv_key].std()
    print(f"{label:<20} {ss:>13.4f} {cv_mean:>10.4f} {cv_std:>8.4f}")

# ─── plot: per-fold metric bars ───────────────────────────────────────────────
fig, axes = plt.subplots(2, 4, figsize=(18, 9))
fig.suptitle("5-Fold Cross Validation Results", fontsize=13, fontweight="bold")
axes = axes.flatten()

for i, (label, key) in enumerate(metrics_display.items()):
    vals = cv_results[key]
    folds = [f"F{j+1}" for j in range(5)]
    axes[i].bar(folds, vals, color="#5b8db8", edgecolor="white", width=0.5)
    axes[i].axhline(vals.mean(), color="red", linestyle="--",
                    linewidth=1.5, label=f"Mean={vals.mean():.4f}")
    axes[i].axhline(vals.mean() + vals.std(), color="orange",
                    linestyle=":", linewidth=1, alpha=0.7)
    axes[i].axhline(vals.mean() - vals.std(), color="orange",
                    linestyle=":", linewidth=1, alpha=0.7, label=f"Std={vals.std():.4f}")
    axes[i].set_title(label, fontsize=9)
    axes[i].set_ylim(max(0, vals.min() - 0.05), min(1.0, vals.max() + 0.05))
    axes[i].legend(fontsize=7)

plt.tight_layout()
plt.savefig("plots/cv_results.png", bbox_inches="tight")
print("\nCV plot saved -> plots/cv_results.png")

# ─── stability verdict ────────────────────────────────────────────────────────
print("\n--- Stability Assessment ---")
roc_std = cv_results["test_roc_auc"].std()
f1f_std = cv_results["test_f1_failure"].std()

print(f"ROC-AUC std across folds : {roc_std:.4f}", end="  ")
print("STABLE" if roc_std < 0.015 else "SOME VARIANCE")

print(f"Failure F1 std across folds: {f1f_std:.4f}", end="  ")
print("STABLE" if f1f_std < 0.02 else "SOME VARIANCE")

print("\n" + "="*60)
print("STAGE 12 -- Cross Validation Complete")
print("="*60)
