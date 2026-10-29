"""
train_model2_readiness.py
========================
Master Orchestrator for XGBoost Food Donation Readiness Machine Learning Pipeline.

Executes:
1. Data Preprocessing (preprocessing.py)
2. Feature Engineering & MI Analysis (feature_engineering.py)
3. XGBoost Stratified 5-Fold CV & Hyperparameter Tuning (train.py)
4. Comprehensive Evaluation & Error Analysis (evaluate.py)
5. Publication Visualizations (visualize.py)
6. Native XGBoost SHAP Interpretability & Explainability (explain.py)
7. XGBoost Model & Pipeline Serialization

Outputs land in: d:\\lovely\\ml\\outputs\\model2\\
"""

import os
import sys
import json
import logging
import joblib
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split, learning_curve
from xgboost import XGBClassifier

# Add module path
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
sys.path.insert(0, SCRIPT_DIR)

from preprocessing import DataPreprocessor
from feature_engineering import FeatureEngineer
from train import ModelTrainer
from evaluate import ModelEvaluator
from visualize import Visualizer
from explain import ModelExplainer

# Setup Logging
OUTPUT_DIR = os.path.join(PROJECT_ROOT, "outputs", "model2")
os.makedirs(OUTPUT_DIR, exist_ok=True)
LOG_FILE = os.path.join(OUTPUT_DIR, "pipeline.log")

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    handlers=[
        logging.FileHandler(LOG_FILE, mode="w", encoding="utf-8"),
        logging.StreamHandler(sys.stdout),
    ],
)
logger = logging.getLogger("MasterPipeline")

# Configuration
TRAIN_CSV = os.path.join(PROJECT_ROOT, "data", "donation_readiness_training_data.csv")
ALT_TRAIN_CSV = os.path.join(PROJECT_ROOT, "data", "model2_training_data.csv")

CONFIG = {
    "training_csv": TRAIN_CSV if os.path.isfile(TRAIN_CSV) else ALT_TRAIN_CSV,
    "output_dir": OUTPUT_DIR,
    "seed": 42,
    "test_size": 0.15,
    "val_size": 0.15,
    "target_col": "readiness_label",
    "numeric_base_features": [
        "temperature_c", "humidity_pct", "co2_ppm", "light_lux",
        "hours_since_stored", "remaining_fridge_life_pct", "batch_weight_kg",
    ],
    "categorical_features": ["food_item", "food_category"],
}


def main():
    logger.info("=" * 70)
    logger.info("  STARTING OPTIMIZED XGBOOST MODEL 2 ML PIPELINE")
    logger.info("=" * 70)
    logger.info(f"Training Data: {CONFIG['training_csv']}")
    logger.info(f"Output Directory: {CONFIG['output_dir']}")

    # 1. Load Data
    csv_path = CONFIG["training_csv"]
    if not os.path.isfile(csv_path):
        logger.error(f"Training dataset missing at {csv_path}. Run prepare_readiness_dataset.py first.")
        sys.exit(1)

    raw_df = pd.read_csv(csv_path)
    logger.info(f"Loaded raw dataset: {len(raw_df):,} rows, {len(raw_df.columns)} columns.")

    # 2. Data Preprocessing & Cleaning
    preprocessor_mgr = DataPreprocessor(
        numeric_features=CONFIG["numeric_base_features"],
        categorical_features=CONFIG["categorical_features"],
        target_col=CONFIG["target_col"],
    )
    cleaned_df = preprocessor_mgr.audit_and_clean(raw_df)

    # 3. Feature Engineering
    engineer = FeatureEngineer()
    engineered_df = engineer.create_features(cleaned_df)

    all_numeric = CONFIG["numeric_base_features"] + engineer.engineered_feature_names
    all_numeric_unique = list(dict.fromkeys(all_numeric))

    mi_scores = engineer.compute_mutual_information(
        engineered_df, engineered_df["y"], all_numeric_unique
    )
    final_df, pruned_cols = engineer.prune_features(engineered_df)

    # Define Feature Sets
    numeric_features = [c for c in all_numeric_unique if c in final_df.columns and c not in pruned_cols]
    categorical_features = CONFIG["categorical_features"]
    feature_cols = numeric_features + categorical_features
    X = final_df[feature_cols]
    y = final_df["y"].values

    logger.info(f"Final feature set ({len(feature_cols)} features): {feature_cols}")

    # 4. Stratified Split (70% Train, 15% Val, 15% Test)
    X_train_raw, X_temp_raw, y_train, y_temp = train_test_split(
        X, y, test_size=(CONFIG["test_size"] + CONFIG["val_size"]),
        stratify=y, random_state=CONFIG["seed"]
    )
    rel_test = CONFIG["test_size"] / (CONFIG["test_size"] + CONFIG["val_size"])
    X_val_raw, X_test_raw, y_val, y_test = train_test_split(
        X_temp_raw, y_temp, test_size=rel_test, stratify=y_temp, random_state=CONFIG["seed"]
    )

    logger.info(f"Split distribution -> Train: {len(X_train_raw):,} | Val: {len(X_val_raw):,} | Test: {len(X_test_raw):,}")

    # 5. Fit Preprocessor (Train Only)
    preprocessor_mgr.numeric_features = numeric_features
    transformer = preprocessor_mgr.fit_preprocessor(X_train_raw)

    X_train_t = preprocessor_mgr.transform(X_train_raw)
    X_val_t = preprocessor_mgr.transform(X_val_raw)
    X_test_t = preprocessor_mgr.transform(X_test_raw)
    transformed_feature_names = preprocessor_mgr.get_feature_names()

    # 6. Sample Weights for Class Imbalance
    sample_weights_train = preprocessor_mgr.get_sample_weights(y_train)

    # 7. XGBoost 5-Fold Stratified CV
    trainer = ModelTrainer(seed=CONFIG["seed"], n_splits=5)
    cv_summary = trainer.evaluate_xgb_cv(X_train_t, y_train, sample_weights_train)

    # 8. Tune XGBoost Hyperparameters
    best_params = trainer.tune_hyperparameters(X_train_t, y_train, sample_weights_train)

    # 9. Fit final XGBoost model with early stopping against validation set
    best_model = XGBClassifier(**best_params, early_stopping_rounds=30, random_state=CONFIG["seed"])
    best_model.fit(
        X_train_t, y_train,
        sample_weight=sample_weights_train,
        eval_set=[(X_train_t, y_train), (X_val_t, y_val)],
        verbose=False,
    )

    # 10. Comprehensive Evaluation & Error Analysis
    evaluator = ModelEvaluator(label_map=preprocessor_mgr.label_map)
    eval_results = evaluator.evaluate_model(best_model, X_test_t, y_test)
    error_results = evaluator.perform_error_analysis(y_test, best_model.predict(X_test_t))

    # 11. Publication Visualizations
    visualizer = Visualizer(output_dir=OUTPUT_DIR, class_names=evaluator.class_names)
    visualizer.plot_confusion_matrix(np.array(eval_results["confusion_matrix"]))

    if hasattr(best_model, "predict_proba"):
        y_test_proba = best_model.predict_proba(X_test_t)
        visualizer.plot_roc_curve(y_test, y_test_proba)
        visualizer.plot_precision_recall_curve(y_test, y_test_proba)
        visualizer.plot_calibration_curve(y_test, y_test_proba)

    # Feature Importance Plot
    if hasattr(best_model, "feature_importances_"):
        visualizer.plot_feature_importance(transformed_feature_names, best_model.feature_importances_)

    # Correlation Heatmap
    visualizer.plot_correlation_heatmap(final_df[numeric_features])

    # Learning Curve
    lc_model = XGBClassifier(**best_params, random_state=CONFIG["seed"])
    train_sizes, train_scores, val_scores = learning_curve(
        lc_model, X_train_t, y_train, cv=trainer.cv, scoring="f1_macro",
        train_sizes=np.linspace(0.1, 1.0, 6), n_jobs=-1
    )
    visualizer.plot_learning_curve(train_sizes, train_scores, val_scores)

    # 12. Native XGBoost SHAP Interpretability Analysis
    explainer_mgr = ModelExplainer(best_model, output_dir=OUTPUT_DIR)
    explainer_mgr.generate_global_explanations(X_test_t, transformed_feature_names, evaluator.class_names)

    # 13. Save Results & Artifacts
    results_path = os.path.join(OUTPUT_DIR, "readiness_results_summary.json")
    full_summary = {
        "model_name": "XGBoost",
        "best_params": {k: (int(v) if isinstance(v, np.integer) else float(v) if isinstance(v, (np.floating, float)) else str(v)) for k, v in best_params.items()},
        "cv_results": cv_summary,
        "metrics": eval_results,
        "error_analysis": error_results,
        "mutual_information_scores": {k: round(float(v), 4) for k, v in mi_scores.items()},
        "pruned_features": pruned_cols,
    }
    with open(results_path, "w") as f:
        json.dump(full_summary, f, indent=2)

    # Save Pipeline Artifacts
    pipeline_dict = {
        "preprocessor": transformer,
        "preprocessor_mgr": preprocessor_mgr,
        "model": best_model,
        "label_map": preprocessor_mgr.label_map,
        "feature_cols": feature_cols,
        "engineered_feature_names": engineer.engineered_feature_names,
    }
    joblib.dump(pipeline_dict, os.path.join(OUTPUT_DIR, "donation_readiness_model.pkl"))

    logger.info("=" * 70)
    logger.info(f"  DONATION READINESS XGBOOST PIPELINE COMPLETED!")
    logger.info(f"  Test Accuracy:     {eval_results['accuracy']:.4f}")
    logger.info(f"  Test Macro F1:     {eval_results['f1_macro']:.4f}")
    logger.info(f"  Test ROC-AUC OVR:  {eval_results['roc_auc_ovr']}")
    logger.info(f"  Pipeline Artifact: {os.path.join(OUTPUT_DIR, 'donation_readiness_model.pkl')}")
    logger.info("=" * 70)


if __name__ == "__main__":
    main()
