"""
evaluate.py
===========
Evaluation & Error Analysis Module for Food Donation Readiness.

Computes:
- Accuracy, Precision (macro/weighted), Recall (macro/weighted), F1 (macro/weighted)
- Multi-class ROC-AUC (OVR) and Log Loss
- Error Analysis (False Positives, False Negatives, Per-class breakdown)
- Model comparison reporting
"""

import logging
from typing import Dict, Any, List, Tuple
import numpy as np
import pandas as pd
from sklearn.metrics import (
    accuracy_score, precision_recall_fscore_support, roc_auc_score,
    log_loss, confusion_matrix, classification_report
)

logger = logging.getLogger(__name__)


class ModelEvaluator:
    """Evaluates ML model performance, error metrics, and classification quality."""

    def __init__(self, label_map: Dict[str, int]):
        self.label_map = label_map
        self.inv_label_map = {v: k for k, v in label_map.items()}
        self.class_names = [self.inv_label_map[i] for i in sorted(self.inv_label_map.keys())]

    def evaluate_model(
        self, model: Any, X_test: np.ndarray, y_test: np.ndarray
    ) -> Dict[str, Any]:
        """Calculates comprehensive classification metrics on held-out test data."""
        y_pred = model.predict(X_test)

        # Probabilities
        if hasattr(model, "predict_proba"):
            y_proba = model.predict_proba(X_test)
        else:
            y_proba = None

        # Base metrics
        acc = accuracy_score(y_test, y_pred)
        prec_mac, rec_mac, f1_mac, _ = precision_recall_fscore_support(y_test, y_pred, average="macro")
        prec_w, rec_w, f1_w, _ = precision_recall_fscore_support(y_test, y_pred, average="weighted")

        # ROC-AUC OVR
        try:
            if y_proba is not None:
                auc_score = roc_auc_score(y_test, y_proba, multi_class="ovr")
                loss_val = log_loss(y_test, y_proba)
            else:
                auc_score = float("nan")
                loss_val = float("nan")
        except Exception as e:
            logger.warning(f"Could not compute ROC-AUC or Log Loss: {e}")
            auc_score = float("nan")
            loss_val = float("nan")

        # Confusion Matrix
        cm = confusion_matrix(y_test, y_pred)

        # Per-class metrics
        prec_per_class, rec_per_class, f1_per_class, support_per_class = precision_recall_fscore_support(
            y_test, y_pred, average=None
        )

        class_metrics = {}
        for i, cname in enumerate(self.class_names):
            class_metrics[cname] = {
                "precision": round(float(prec_per_class[i]), 4),
                "recall": round(float(rec_per_class[i]), 4),
                "f1": round(float(f1_per_class[i]), 4),
                "support": int(support_per_class[i]),
            }

        report_dict = classification_report(y_test, y_pred, target_names=self.class_names, output_dict=True)

        metrics = {
            "accuracy": round(float(acc), 4),
            "precision_macro": round(float(prec_mac), 4),
            "recall_macro": round(float(rec_mac), 4),
            "f1_macro": round(float(f1_mac), 4),
            "precision_weighted": round(float(prec_w), 4),
            "recall_weighted": round(float(rec_w), 4),
            "f1_weighted": round(float(f1_w), 4),
            "roc_auc_ovr": round(float(auc_score), 4) if not np.isnan(auc_score) else None,
            "log_loss": round(float(loss_val), 4) if not np.isnan(loss_val) else None,
            "confusion_matrix": cm.tolist(),
            "class_metrics": class_metrics,
            "classification_report": report_dict,
        }

        logger.info(
            f"Evaluation Results -> Accuracy: {acc:.4f} | Macro F1: {f1_mac:.4f} | "
            f"Macro Recall: {rec_mac:.4f} | ROC-AUC: {auc_score:.4f}"
        )
        return metrics

    def perform_error_analysis(
        self, y_test: np.ndarray, y_pred: np.ndarray
    ) -> Dict[str, Any]:
        """Analyzes misclassifications (False Positives and False Negatives)."""
        misclassified_indices = np.where(y_test != y_pred)[0]
        total_test = len(y_test)
        total_errors = len(misclassified_indices)
        error_rate = total_errors / total_test

        error_summary = {
            "total_test_samples": total_test,
            "total_misclassified": total_errors,
            "error_rate_pct": round(error_rate * 100, 2),
            "misclassifications_by_pair": {},
        }

        cm = confusion_matrix(y_test, y_pred)
        for i, true_class in enumerate(self.class_names):
            for j, pred_class in enumerate(self.class_names):
                if i != j and cm[i][j] > 0:
                    pair_name = f"True_{true_class}_Predicted_{pred_class}"
                    error_summary["misclassifications_by_pair"][pair_name] = int(cm[i][j])

        logger.info(f"Error Analysis: {total_errors}/{total_test} samples misclassified ({error_rate*100:.2f}%)")
        return error_summary
