"""
visualize.py
============
Publication-Quality Visualization Suite for Food Donation Readiness.

Generates and saves:
- Confusion Matrix
- Multi-class ROC Curve
- Precision-Recall Curve
- Learning Curve
- Validation Curve
- Feature Importance Plot
- Correlation Heatmap
- Calibration Curve
"""

import os
import logging
from typing import List, Dict, Any, Optional
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.metrics import roc_curve, precision_recall_curve, auc
from sklearn.calibration import calibration_curve

logger = logging.getLogger(__name__)

# Styling setup for publication-ready figures
sns.set_theme(style="whitegrid", palette="muted")
plt.rcParams.update({
    "font.size": 11,
    "axes.labelsize": 12,
    "axes.titlesize": 13,
    "xtick.labelsize": 10,
    "ytick.labelsize": 10,
    "figure.titlesize": 14,
})


class Visualizer:
    """Generates research-grade plots and diagnostic visualizations."""

    def __init__(self, output_dir: str, class_names: List[str]):
        self.output_dir = output_dir
        self.class_names = class_names
        os.makedirs(output_dir, exist_ok=True)

    def plot_confusion_matrix(self, cm: np.ndarray, filename: str = "readiness_confusion_matrix.png"):
        """Plots normalized and raw confusion matrix heatmap."""
        plt.figure(figsize=(7, 6))
        sns.heatmap(
            cm, annot=True, fmt="d", cmap="Blues", cbar=True,
            xticklabels=self.class_names, yticklabels=self.class_names
        )
        plt.xlabel("Predicted Readiness Label")
        plt.ylabel("True Readiness Label")
        plt.title("Donation Readiness — Confusion Matrix")
        plt.tight_layout()
        save_path = os.path.join(self.output_dir, filename)
        plt.savefig(save_path, dpi=300, bbox_inches="tight")
        plt.close()
        logger.info(f"Saved confusion matrix: {save_path}")

    def plot_roc_curve(self, y_test: np.ndarray, y_proba: np.ndarray, filename: str = "readiness_roc_curve.png"):
        """Plots multi-class One-vs-Rest (OVR) ROC curves."""
        if y_proba is None:
            return
        plt.figure(figsize=(8, 6))
        n_classes = len(self.class_names)
        colors = ["#2ecc71", "#f39c12", "#e74c3c"]

        for i in range(n_classes):
            y_binary = (y_test == i).astype(int)
            fpr, tpr, _ = roc_curve(y_binary, y_proba[:, i])
            roc_auc = auc(fpr, tpr)
            plt.plot(
                fpr, tpr, color=colors[i % len(colors)], lw=2,
                label=f"Class {self.class_names[i]} (AUC = {roc_auc:.4f})"
            )

        plt.plot([0, 1], [0, 1], "k--", lw=1.5, label="Random Chance")
        plt.xlim([0.0, 1.0])
        plt.ylim([0.0, 1.05])
        plt.xlabel("False Positive Rate")
        plt.ylabel("True Positive Rate")
        plt.title("Donation Readiness — Multi-Class ROC Curve")
        plt.legend(loc="lower right")
        plt.tight_layout()
        save_path = os.path.join(self.output_dir, filename)
        plt.savefig(save_path, dpi=300, bbox_inches="tight")
        plt.close()
        logger.info(f"Saved ROC curve: {save_path}")

    def plot_precision_recall_curve(self, y_test: np.ndarray, y_proba: np.ndarray, filename: str = "readiness_pr_curve.png"):
        """Plots Precision-Recall curves for each class."""
        if y_proba is None:
            return
        plt.figure(figsize=(8, 6))
        colors = ["#2ecc71", "#f39c12", "#e74c3c"]

        for i, cname in enumerate(self.class_names):
            y_binary = (y_test == i).astype(int)
            precision, recall, _ = precision_recall_curve(y_binary, y_proba[:, i])
            pr_auc = auc(recall, precision)
            plt.plot(recall, precision, color=colors[i % len(colors)], lw=2, label=f"Class {cname} (PR-AUC = {pr_auc:.4f})")

        plt.xlabel("Recall")
        plt.ylabel("Precision")
        plt.title("Donation Readiness — Precision-Recall Curve")
        plt.legend(loc="lower left")
        plt.tight_layout()
        save_path = os.path.join(self.output_dir, filename)
        plt.savefig(save_path, dpi=300, bbox_inches="tight")
        plt.close()
        logger.info(f"Saved Precision-Recall curve: {save_path}")

    def plot_learning_curve(
        self, train_sizes: np.ndarray, train_scores: np.ndarray, val_scores: np.ndarray,
        filename: str = "readiness_learning_curve.png"
    ):
        """Plots train vs validation F1 scores across training set sizes."""
        train_mean = np.mean(train_scores, axis=1)
        train_std = np.std(train_scores, axis=1)
        val_mean = np.mean(val_scores, axis=1)
        val_std = np.std(val_scores, axis=1)

        plt.figure(figsize=(8, 5))
        plt.plot(train_sizes, train_mean, "o-", color="#3498db", label="Training Score (F1 Macro)", lw=2)
        plt.plot(train_sizes, val_mean, "o-", color="#e74c3c", label="Validation Score (F1 Macro)", lw=2)
        plt.fill_between(train_sizes, train_mean - train_std, train_mean + train_std, alpha=0.15, color="#3498db")
        plt.fill_between(train_sizes, val_mean - val_std, val_mean + val_std, alpha=0.15, color="#e74c3c")

        plt.xlabel("Training Set Size")
        plt.ylabel("F1 Score (Macro)")
        plt.title("Model 2 — Learning Curve Diagnostic")
        plt.legend(loc="lower right")
        plt.grid(True, alpha=0.3)
        plt.tight_layout()
        save_path = os.path.join(self.output_dir, filename)
        plt.savefig(save_path, dpi=300, bbox_inches="tight")
        plt.close()
        logger.info(f"Saved learning curve: {save_path}")

    def plot_feature_importance(
        self, feature_names: List[str], importances: np.ndarray, top_n: int = 20,
        filename: str = "readiness_feature_importance.png"
    ):
        """Plots horizontal bar chart of top N important features."""
        sorted_idx = np.argsort(importances)[::-1][:top_n]
        top_names = [feature_names[i] for i in sorted_idx][::-1]
        top_vals = importances[sorted_idx][::-1]

        plt.figure(figsize=(10, 7))
        plt.barh(top_names, top_vals, color="#2980b9", edgecolor="none", height=0.7)
        plt.xlabel("Feature Importance (Gain)")
        plt.title(f"Donation Readiness — Top {top_n} Feature Importance Ranking")
        plt.tight_layout()
        save_path = os.path.join(self.output_dir, filename)
        plt.savefig(save_path, dpi=300, bbox_inches="tight")
        plt.close()
        logger.info(f"Saved feature importance plot: {save_path}")

    def plot_correlation_heatmap(self, df: pd.DataFrame, filename: str = "readiness_correlation_heatmap.png"):
        """Plots feature correlation matrix heatmap."""
        num_df = df.select_dtypes(include=[np.number])
        if num_df.empty:
            return
        corr = num_df.corr()

        plt.figure(figsize=(10, 8))
        sns.heatmap(corr, annot=True, fmt=".2f", cmap="coolwarm", vmin=-1, vmax=1, square=True)
        plt.title("Donation Readiness — Feature Correlation Heatmap")
        plt.tight_layout()
        save_path = os.path.join(self.output_dir, filename)
        plt.savefig(save_path, dpi=300, bbox_inches="tight")
        plt.close()
        logger.info(f"Saved correlation heatmap: {save_path}")

    def plot_calibration_curve(self, y_test: np.ndarray, y_proba: np.ndarray, filename: str = "readiness_calibration_curve.png"):
        """Plots probability calibration curve for reliability diagnosis."""
        if y_proba is None:
            return
        plt.figure(figsize=(8, 6))

        for i, cname in enumerate(self.class_names):
            y_binary = (y_test == i).astype(int)
            prob_true, prob_pred = calibration_curve(y_binary, y_proba[:, i], n_bins=10)
            plt.plot(prob_pred, prob_true, "s-", label=f"Class {cname}")

        plt.plot([0, 1], [0, 1], "k:", label="Perfectly Calibrated")
        plt.xlabel("Mean Predicted Probability")
        plt.ylabel("Fraction of Positives")
        plt.title("Donation Readiness — Probability Calibration Curve")
        plt.legend(loc="upper left")
        plt.tight_layout()
        save_path = os.path.join(self.output_dir, filename)
        plt.savefig(save_path, dpi=300, bbox_inches="tight")
        plt.close()
        logger.info(f"Saved calibration curve: {save_path}")
