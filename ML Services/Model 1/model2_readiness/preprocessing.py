"""
preprocessing.py
================
Data Preprocessing Module for Donation Readiness Prediction.

Handles:
- Missing value detection and imputation
- Duplicate record detection and removal
- Outlier detection and IQR-based winsorization
- Categorical feature encoding (OneHotEncoder with handle_unknown='ignore')
- Numerical feature scaling (StandardScaler)
- Class imbalance sample weighting
"""

import logging
from typing import Tuple, Dict, Any, List
import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.utils.class_weight import compute_sample_weight

logger = logging.getLogger(__name__)


class DataPreprocessor:
    """Preprocesses tabular data for donation readiness modeling."""

    def __init__(self, numeric_features: List[str], categorical_features: List[str], target_col: str):
        self.numeric_features = numeric_features
        self.categorical_features = categorical_features
        self.target_col = target_col
        self.column_transformer: ColumnTransformer = None
        self.label_map = {"Low": 0, "Medium": 1, "High": 2}
        self.inv_label_map = {0: "Low", 1: "Medium", 2: "High"}

    def audit_and_clean(self, df: pd.DataFrame) -> pd.DataFrame:
        """Inspects and cleans raw input data (missing values, duplicates, outliers)."""
        logger.info(f"Initial raw dataset shape: {df.shape}")

        # 1. Missing Values
        missing = df.isnull().sum()
        if missing.sum() > 0:
            logger.warning(f"Missing values detected:\n{missing[missing > 0]}")
            # Numeric fill median
            df[self.numeric_features] = df[self.numeric_features].fillna(df[self.numeric_features].median())
            # Categorical fill mode/unknown
            for col in self.categorical_features:
                df[col] = df[col].astype(str).fillna("unknown")
        else:
            logger.info("No missing values found.")

        # 2. Duplicate Records
        duplicates = df.duplicated().sum()
        if duplicates > 0:
            logger.info(f"Removing {duplicates} duplicate records...")
            df = df.drop_duplicates().reset_index(drop=True)
        else:
            logger.info("No duplicate records found.")

        # 3. Outlier Handling (IQR Capping on extreme continuous bounds)
        for col in ["temperature_c", "humidity_pct", "co2_ppm", "hours_since_stored"]:
            if col in df.columns:
                q1 = df[col].quantile(0.01)
                q3 = df[col].quantile(0.99)
                iqr = q3 - q1
                lower = q1 - 1.5 * iqr
                upper = q3 + 1.5 * iqr
                n_outliers = ((df[col] < lower) | (df[col] > upper)).sum()
                if n_outliers > 0:
                    logger.info(f"Capping {n_outliers} extreme outliers in '{col}' to range [{lower:.2f}, {upper:.2f}]")
                    df[col] = np.clip(df[col], lower, upper)

        # 4. Target Mapping
        if self.target_col in df.columns:
            df["y"] = df[self.target_col].map(self.label_map)
            df = df.dropna(subset=["y"]).copy()
            df["y"] = df["y"].astype(int)
            logger.info(f"Target distribution:\n{df[self.target_col].value_counts().to_dict()}")

        logger.info(f"Cleaned dataset shape: {df.shape}")
        return df

    def fit_preprocessor(self, X_train: pd.DataFrame) -> ColumnTransformer:
        """Fits column transformer (scaling + categorical encoding) on training fold only."""
        num_cols = [c for c in self.numeric_features if c in X_train.columns]
        cat_cols = [c for c in self.categorical_features if c in X_train.columns]

        self.column_transformer = ColumnTransformer([
            ("num", StandardScaler(), num_cols),
            ("cat", OneHotEncoder(handle_unknown="ignore", sparse_output=False), cat_cols),
        ])
        self.column_transformer.fit(X_train)
        return self.column_transformer

    def transform(self, X: pd.DataFrame) -> np.ndarray:
        """Transforms feature dataframe into processed numpy matrix."""
        if self.column_transformer is None:
            raise ValueError("ColumnTransformer has not been fitted yet!")
        return self.column_transformer.transform(X)

    def get_feature_names(self) -> List[str]:
        """Retrieves transformed feature column names."""
        if self.column_transformer is None:
            return []
        num_cols = [c for c in self.numeric_features if c in self.column_transformer.feature_names_in_]
        cat_transformer = self.column_transformer.named_transformers_["cat"]
        cat_cols = [c for c in self.categorical_features if c in self.column_transformer.feature_names_in_]
        cat_encoded_names = list(cat_transformer.get_feature_names_out(cat_cols))
        return num_cols + cat_encoded_names

    @staticmethod
    def get_sample_weights(y: np.ndarray) -> np.ndarray:
        """Computes balanced class sample weights to handle severe class imbalance."""
        return compute_sample_weight("balanced", y)
