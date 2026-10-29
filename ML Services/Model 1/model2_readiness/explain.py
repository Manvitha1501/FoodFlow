"""
explain.py
==========
SHAP Explainability & Model Interpretability Module.

Generates:
- Global SHAP Summary Plot
- SHAP Dependence Plot
- Local sample explanations for individual predictions
"""

import os
import logging
from typing import List, Dict, Any
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import shap

logger = logging.getLogger(__name__)


class ModelExplainer:
    """Provides global and local model explainability via SHAP values."""

    def __init__(self, model: Any, output_dir: str):
        self.model = model
        self.output_dir = output_dir
        os.makedirs(output_dir, exist_ok=True)
        self.explainer = None

    def fit_explainer(self, X_background: np.ndarray):
        """Initializes SHAP TreeExplainer or Explainer."""
        try:
            # TreeExplainer works fast for tree ensembles (XGBoost, LightGBM, CatBoost, RandomForest)
            self.explainer = shap.TreeExplainer(self.model)
            logger.info("Initialized SHAP TreeExplainer.")
        except Exception as e:
            logger.warning(f"TreeExplainer failed ({e}), falling back to KernelExplainer...")
            # Sample background for speed
            bg_sample = X_background[:100] if len(X_background) > 100 else X_background
            self.explainer = shap.KernelExplainer(self.model.predict_proba, bg_sample)

    def generate_global_explanations(
        self, X_sample: np.ndarray, feature_names: List[str], class_names: List[str]
    ):
        """Generates SHAP summary and dependence plots."""
        if self.explainer is None:
            self.fit_explainer(X_sample)

        try:
            logger.info("Computing SHAP values...")
            # Subsample 100 rows for fast, reliable SHAP plot generation
            eval_sample = X_sample[:100] if len(X_sample) > 100 else X_sample
            shap_values = self.explainer.shap_values(eval_sample)

            # 1. SHAP Summary Plot
            plt.figure(figsize=(10, 8))
            if isinstance(shap_values, list):
                # Multi-class list
                shap.summary_plot(
                    shap_values, eval_sample, feature_names=feature_names,
                    class_names=class_names, show=False
                )
            else:
                shap.summary_plot(shap_values, eval_sample, feature_names=feature_names, show=False)

            summary_path = os.path.join(self.output_dir, "readiness_shap_summary.png")
            plt.tight_layout()
            plt.savefig(summary_path, dpi=300, bbox_inches="tight")
            plt.close()
            logger.info(f"Saved SHAP summary plot: {summary_path}")

            # 2. SHAP Dependence Plot for top feature
            if isinstance(shap_values, list) and len(shap_values) > 0:
                top_vals = np.abs(shap_values[0]).mean(axis=0)
                top_feat_idx = np.argmax(top_vals)
                plt.figure(figsize=(8, 6))
                shap.dependence_plot(
                    top_feat_idx, shap_values[0], eval_sample,
                    feature_names=feature_names, show=False
                )
                dep_path = os.path.join(self.output_dir, "model2_shap_dependence.png")
                plt.tight_layout()
                plt.savefig(dep_path, dpi=300, bbox_inches="tight")
                plt.close()
                logger.info(f"Saved SHAP dependence plot: {dep_path}")

        except Exception as e:
            logger.error(f"Failed to generate SHAP plots: {e}")

    def explain_local_prediction(
        self, sample_trans: np.ndarray, feature_names: List[str], class_names: List[str]
    ) -> Dict[str, Any]:
        """Provides feature contribution impact for a single prediction sample."""
        if self.explainer is None:
            return {"status": "Explainer not initialized"}

        try:
            shap_val = self.explainer.shap_values(sample_trans)
            if isinstance(shap_val, list):
                # Take majority class contribution
                shap_contrib = shap_val[0][0]
            else:
                shap_contrib = shap_val[0]

            top_idx = np.argsort(np.abs(shap_contrib))[::-1][:10]
            explanation = {
                feature_names[i]: round(float(shap_contrib[i]), 4) for i in top_idx
            }
            return explanation
        except Exception as e:
            return {"error": str(e)}
