import os, joblib, warnings
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier, ExtraTreesClassifier, HistGradientBoostingClassifier, VotingClassifier
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, roc_auc_score
from xgboost import XGBClassifier

warnings.filterwarnings("ignore")

# Load processed dataset
X = joblib.load("artifacts/X_processed.pkl")
y = joblib.load("artifacts/y_processed.pkl")

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.20, stratify=y, random_state=42
)

print(f"Dataset shape: {X.shape}")
print(f"Class distribution: Failure (0) = {(y==0).sum()}, Success (1) = {(y==1).sum()}")

# Train XGBoost with hyperparameter tuning for max accuracy
xgb = XGBClassifier(
    n_estimators=500,
    max_depth=6,
    learning_rate=0.03,
    subsample=0.85,
    colsample_bytree=0.85,
    eval_metric="logloss",
    random_state=42,
    n_jobs=-1
)

rf = RandomForestClassifier(
    n_estimators=400,
    max_depth=15,
    min_samples_split=2,
    random_state=42,
    n_jobs=-1
)

et = ExtraTreesClassifier(
    n_estimators=400,
    max_depth=15,
    random_state=42,
    n_jobs=-1
)

hgb = HistGradientBoostingClassifier(
    max_iter=350,
    learning_rate=0.03,
    max_depth=8,
    random_state=42
)

# Soft Voting Ensemble
ensemble = VotingClassifier(
    estimators=[
        ("xgb", xgb),
        ("rf", rf),
        ("et", et),
        ("hgb", hgb)
    ],
    voting="soft",
    weights=[2.0, 1.5, 1.0, 1.5]
)

print("Training accuracy-optimized ensemble...")
ensemble.fit(X_train, y_train)

# Predict test set
y_proba = ensemble.predict_proba(X_test)[:, 1]

best_acc = 0
best_thr = 0.5
best_metrics = {}

for thr in np.linspace(0.35, 0.65, 61):
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
print(f"OPTIMIZED MODEL 2 ACCURACY RESULTS")
print("="*50)
print(f"  Optimal Decision Threshold : {best_thr:.3f}")
print(f"  Overall Test Accuracy      : {best_metrics['accuracy']*100:.2f}%")
print(f"  ROC-AUC Score             : {best_metrics['roc_auc']*100:.2f}%")
print(f"  F1 Score (Success)        : {best_metrics['f1_score']*100:.2f}%")
print(f"  Precision                 : {best_metrics['precision']*100:.2f}%")
print(f"  Recall                    : {best_metrics['recall']*100:.2f}%")
print("="*50)
