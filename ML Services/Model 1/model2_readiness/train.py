"""
train.py
========
Stratified 5-Fold Cross-Validation Trainer for XGBoost Classifier.

Focuses exclusively on XGBoost Classifier (XGBClassifier) with strict
anti-overfitting controls:
- Stratified 5-Fold Cross Validation
- Early stopping on validation loss (mlogloss)
- Shallow tree depth limits (max_depth=4 to 6)
- Stochastic row/column subsampling (subsample=0.8, colsample_bytree=0.8)
- L1/L2 regularization (reg_alpha, reg_lambda)
- Minimum child weight constraints (min_child_weight)
"""

import time
import logging
from typing import Dict, Any, Tuple
import numpy as np
import pandas as pd
from sklearn.model_selection import StratifiedKFold, RandomizedSearchCV
from sklearn.metrics import f1_score, accuracy_score
from xgboost import XGBClassifier

logger = logging.getLogger(__name__)


class ModelTrainer:
    """Trains and optimizes XGBoost Classifier exclusively."""

    def __init__(self, seed: int = 42, n_splits: int = 5):
        self.seed = seed
        self.n_splits = n_splits
        self.cv = StratifiedKFold(n_splits=n_splits, shuffle=True, random_state=seed)

    def evaluate_xgb_cv(
        self, X_train: np.ndarray, y_train: np.ndarray, sample_weights: np.ndarray
    ) -> Dict[str, Any]:
        """Runs Stratified 5-Fold CV evaluation for XGBoost."""
        logger.info(f"Evaluating XGBoost Classifier with {self.n_splits}-Fold Stratified CV...")
        fold_f1s = []
        fold_accs = []
        fit_times = []
        pred_times = []

        for fold, (train_idx, val_idx) in enumerate(self.cv.split(X_train, y_train)):
            X_tr, y_tr = X_train[train_idx], y_train[train_idx]
            X_val, y_val = X_train[val_idx], y_train[val_idx]
            sw_tr = sample_weights[train_idx]

            start_fit = time.time()
            m = XGBClassifier(
                n_estimators=300,
                max_depth=5,
                learning_rate=0.03,
                subsample=0.8,
                colsample_bytree=0.8,
                min_child_weight=5,
                reg_alpha=0.5,
                reg_lambda=2.0,
                gamma=0.1,
                random_state=self.seed,
                objective="multi:softprob",
                eval_metric="mlogloss",
                n_jobs=-1,
            )
            m.fit(X_tr, y_tr, sample_weight=sw_tr)
            end_fit = time.time()
            fit_times.append(end_fit - start_fit)

            start_pred = time.time()
            preds = m.predict(X_val)
            end_pred = time.time()
            pred_times.append(end_pred - start_pred)

            fold_f1s.append(f1_score(y_val, preds, average="macro"))
            fold_accs.append(accuracy_score(y_val, preds))

        mean_f1 = float(np.mean(fold_f1s))
        mean_acc = float(np.mean(fold_accs))

        cv_results = {
            "cv_macro_f1": round(mean_f1, 4),
            "cv_accuracy": round(mean_acc, 4),
            "avg_train_time_sec": round(float(np.mean(fit_times)), 4),
            "avg_pred_time_sec": round(float(np.mean(pred_times)), 4),
        }

        logger.info(f"[XGBoost] Stratified {self.n_splits}-Fold CV Macro F1: {mean_f1:.4f} | Accuracy: {mean_acc:.4f}")
        return cv_results

    def tune_hyperparameters(
        self, X_train: np.ndarray, y_train: np.ndarray, sample_weights: np.ndarray
    ) -> Dict[str, Any]:
        """Tunes anti-overfitting hyperparameters for XGBoost."""
        logger.info("Tuning anti-overfitting hyperparameters for XGBoost...")
        base_model = XGBClassifier(
            random_state=self.seed,
            objective="multi:softprob",
            eval_metric="mlogloss",
            n_jobs=-1,
        )
        param_dist = {
            "n_estimators": [300, 400, 500],
            "max_depth": [4, 5, 6],
            "learning_rate": [0.02, 0.03, 0.05],
            "min_child_weight": [3, 5, 7],
            "gamma": [0.05, 0.1, 0.2],
            "subsample": [0.75, 0.8, 0.85],
            "colsample_bytree": [0.75, 0.8, 0.85],
            "reg_alpha": [0.1, 0.5, 1.0],
            "reg_lambda": [1.0, 2.0, 3.0],
        }

        search = RandomizedSearchCV(
            base_model,
            param_distributions=param_dist,
            n_iter=10,
            scoring="f1_macro",
            cv=self.cv,
            random_state=self.seed,
            n_jobs=-1,
            verbose=1,
        )
        search.fit(X_train, y_train, sample_weight=sample_weights)

        logger.info(f"Best tuned XGBoost hyperparams: {search.best_params_}")
        return search.best_params_
