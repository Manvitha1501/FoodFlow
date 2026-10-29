import sys, os, joblib, warnings
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
from sklearn.metrics import (accuracy_score, precision_score, recall_score,
                             f1_score, roc_auc_score, confusion_matrix,
                             classification_report, ConfusionMatrixDisplay)
from xgboost import XGBClassifier
warnings.filterwarnings("ignore")

os.makedirs("artifacts", exist_ok=True)
os.makedirs("plots", exist_ok=True)

# =============================================================================
# STAGE 5 -- LOAD PROCESSED DATA AND SPLIT
# =============================================================================
print("="*60)
print("STAGE 5 -- Train/Test Split")
print("="*60)

X   = joblib.load("artifacts/X_processed.pkl")
y   = joblib.load("artifacts/y_processed.pkl")
spw = joblib.load("artifacts/scale_pos_weight.pkl")

print("X shape :", X.shape)
print("y shape :", y.shape)
print("scale_pos_weight:", spw)

# train_test_split
# test_size=0.2   -> 20% held out for evaluation
# stratify=y      -> preserves class ratio in both splits
# random_state=42 -> reproducible split every run
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.20, stratify=y, random_state=42
)

print("\nSplit complete:")
print("  X_train:", X_train.shape, "| y_train:", y_train.shape)
print("  X_test :", X_test.shape,  "| y_test :", y_test.shape)
print("\nClass balance in train set:")
print("  Success:", (y_train==1).sum(), "| Failure:", (y_train==0).sum())
print("  Success %:", round((y_train==1).mean()*100,2),
      "| Failure %:", round((y_train==0).mean()*100,2))
print("Class balance in test set:")
print("  Success:", (y_test==1).sum(), "| Failure:", (y_test==0).sum())
print("  Success %:", round((y_test==1).mean()*100,2),
      "| Failure %:", round((y_test==0).mean()*100,2))

joblib.dump(X_train, "artifacts/X_train.pkl")
joblib.dump(X_test,  "artifacts/X_test.pkl")
joblib.dump(y_train, "artifacts/y_train.pkl")
joblib.dump(y_test,  "artifacts/y_test.pkl")
print("\nTrain/test splits saved to artifacts/")

# =============================================================================
# STAGE 6 -- BUILD XGBOOST CLASSIFIER
# =============================================================================
print("\n" + "="*60)
print("STAGE 6 -- XGBoost Classifier (Default Parameters)")
print("="*60)

# XGBClassifier parameters explained:
#   n_estimators=300     -- 300 boosting rounds (trees)
#   max_depth=5          -- each tree can ask 5 levels of questions
#   learning_rate=0.05   -- each tree contributes 5% of its correction
#   subsample=0.8        -- each tree sees 80% of training rows (reduces overfit)
#   colsample_bytree=0.8 -- each tree sees 80% of features (reduces overfit)
#   scale_pos_weight     -- upweights Failure class (minority) during training
#   use_label_encoder=False -- suppress deprecation warning
#   eval_metric="logloss"   -- monitor log-loss during training
#   random_state=42      -- reproducibility

model = XGBClassifier(
    n_estimators=300,
    max_depth=5,
    learning_rate=0.05,
    subsample=0.8,
    colsample_bytree=0.8,
    scale_pos_weight=spw,
    eval_metric="logloss",
    random_state=42,
    n_jobs=-1
)

print("Model defined:")
print("  n_estimators    :", model.n_estimators)
print("  max_depth       :", model.max_depth)
print("  learning_rate   :", model.learning_rate)
print("  subsample       :", model.subsample)
print("  colsample_bytree:", model.colsample_bytree)
print("  scale_pos_weight:", round(spw, 4))

# =============================================================================
# STAGE 7 -- TRAIN
# =============================================================================
print("\n" + "="*60)
print("STAGE 7 -- Training")
print("="*60)

# eval_set lets XGBoost track test loss per round (used for diagnostics)
# verbose=50 prints loss every 50 rounds
model.fit(
    X_train, y_train,
    eval_set=[(X_train, y_train), (X_test, y_test)],
    verbose=50
)
print("Training complete.")

# training curve data
results   = model.evals_result()
train_loss = results["validation_0"]["logloss"]
test_loss  = results["validation_1"]["logloss"]

# plot training curve
fig, ax = plt.subplots(figsize=(10, 5))
ax.plot(train_loss, label="Train Log-Loss", color="#1f77b4", linewidth=1.5)
ax.plot(test_loss,  label="Test Log-Loss",  color="#d62728", linewidth=1.5)
ax.set_title("XGBoost Training Curve -- Log-Loss per Boosting Round")
ax.set_xlabel("Boosting Round")
ax.set_ylabel("Log-Loss")
ax.legend()
plt.tight_layout()
plt.savefig("plots/training_curve.png", bbox_inches="tight")
print("Training curve saved -> plots/training_curve.png")

# =============================================================================
# STAGE 8 -- PREDICT
# =============================================================================
print("\n" + "="*60)
print("STAGE 8 -- Predictions")
print("="*60)

# predict() returns hard labels (0 or 1) using 0.5 threshold
y_pred = model.predict(X_test)

# predict_proba() returns [P(class=0), P(class=1)] for every row
# we use [:, 1] to get P(success=1) -- the confidence score
y_proba = model.predict_proba(X_test)[:, 1]

print("Sample predictions (first 10):")
sample = pd.DataFrame({
    "actual"    : y_test.values[:10],
    "predicted" : y_pred[:10],
    "prob_success": y_proba[:10].round(4)
})
print(sample.to_string(index=False))

# =============================================================================
# STAGE 9 -- EVALUATE
# =============================================================================
print("\n" + "="*60)
print("STAGE 9 -- Evaluation Metrics")
print("="*60)

acc       = accuracy_score(y_test, y_pred)
precision = precision_score(y_test, y_pred)
recall    = recall_score(y_test, y_pred)
f1        = f1_score(y_test, y_pred)
roc_auc   = roc_auc_score(y_test, y_proba)

print(f"  Accuracy  : {acc:.4f}  ({acc*100:.2f}%)")
print(f"  Precision : {precision:.4f}")
print(f"  Recall    : {recall:.4f}")
print(f"  F1 Score  : {f1:.4f}")
print(f"  ROC-AUC   : {roc_auc:.4f}")

print("\nClassification Report:")
print(classification_report(y_test, y_pred, target_names=["Failure (0)", "Success (1)"]))

# confusion matrix plot
cm  = confusion_matrix(y_test, y_pred)
fig, ax = plt.subplots(figsize=(7, 6))
disp = ConfusionMatrixDisplay(confusion_matrix=cm, display_labels=["Failure", "Success"])
disp.plot(ax=ax, colorbar=True, cmap="Blues")
ax.set_title("Confusion Matrix -- XGBoost Default Model")
plt.tight_layout()
plt.savefig("plots/confusion_matrix.png", bbox_inches="tight")
print("Confusion matrix saved -> plots/confusion_matrix.png")

# raw confusion matrix values
tn, fp, fn, tp = cm.ravel()
print(f"\n  TN (correct failure predictions) : {tn}")
print(f"  FP (failures predicted as success): {fp}")
print(f"  FN (successes predicted as failure): {fn}")
print(f"  TP (correct success predictions)  : {tp}")

# =============================================================================
# STAGE 10 -- FEATURE IMPORTANCE
# =============================================================================
print("\n" + "="*60)
print("STAGE 10 -- Feature Importance")
print("="*60)

importance = pd.Series(model.feature_importances_, index=X.columns)
importance = importance.sort_values(ascending=False)

print("Top 15 features by importance (F-score):")
print(importance.head(15).round(4).to_string())

fig, ax = plt.subplots(figsize=(12, 8))
importance.head(20).sort_values().plot(kind="barh", ax=ax, color="#5b8db8", edgecolor="white")
ax.set_title("XGBoost Feature Importance (Top 20)")
ax.set_xlabel("Importance Score")
plt.tight_layout()
plt.savefig("plots/feature_importance.png", bbox_inches="tight")
print("Feature importance saved -> plots/feature_importance.png")

# =============================================================================
# STAGE 11 -- SAVE MODEL
# =============================================================================
print("\n" + "="*60)
print("STAGE 11 -- Save Model")
print("="*60)

joblib.dump(model, "artifacts/donation_success_model.pkl")
print("Model saved -> artifacts/donation_success_model.pkl")

# save metrics summary
metrics = {
    "accuracy" : round(acc, 4),
    "precision": round(precision, 4),
    "recall"   : round(recall, 4),
    "f1"       : round(f1, 4),
    "roc_auc"  : round(roc_auc, 4)
}
joblib.dump(metrics, "artifacts/model_metrics.pkl")
print("Metrics saved -> artifacts/model_metrics.pkl")

print("\n" + "="*60)
print("STAGES 5-11 COMPLETE")
print("="*60)
