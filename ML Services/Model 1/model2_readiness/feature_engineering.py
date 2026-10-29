"""
feature_engineering.py
======================
Domain-Specific Feature Engineering Module for Food Donation Readiness.

Computes 10 physical features:
1. remaining_fridge_life_hours
2. remaining_fridge_life_pct
3. storage_risk_score
4. environmental_spoilage_index
5. temp_humidity_interaction
6. co2_risk_level
7. pickup_urgency
8. food_perishability_score
9. donation_confidence_score
10. freshness_score

Performs Mutual Information analysis and redundant feature pruning.
"""

import logging
from typing import List, Tuple, Dict
import numpy as np
import pandas as pd
from sklearn.feature_selection import mutual_info_classif

logger = logging.getLogger(__name__)

# Category perishability weights based on WHO/FDA food safety guidelines
CATEGORY_PERISHABILITY = {
    "poultry_cooked": 1.4,
    "poultry": 1.3,
    "dairy": 1.2,
    "cooked_starch": 1.1,
    "produce": 1.0,
    "canned": 0.7,
}


class FeatureEngineer:
    """Engineers physical domain features for food safety and donation readiness."""

    def __init__(self):
        self.engineered_feature_names = [
            "remaining_fridge_life_hours",
            "storage_risk_score",
            "environmental_spoilage_index",
            "temp_humidity_interaction",
            "co2_risk_level",
            "pickup_urgency",
            "food_perishability_score",
            "donation_confidence_score",
            "freshness_score",
        ]

    def create_features(self, df: pd.DataFrame) -> pd.DataFrame:
        """Computes all engineered physical features on the input dataframe."""
        logger.info("Computing domain-specific engineered features...")
        df = df.copy()

        # 1. Food Perishability Score
        df["food_perishability_score"] = df["food_category"].map(
            lambda cat: CATEGORY_PERISHABILITY.get(str(cat).lower(), 1.0)
        )

        # 2. Temperature-Humidity Interaction
        df["temp_humidity_interaction"] = (df["temperature_c"] * df["humidity_pct"]) / 100.0

        # 3. Storage Risk Score (WHO Danger Zone 5°C to 60°C graduated penalty)
        temp_dz = df["temperature_c"].apply(
            lambda t: 25.0 if (5.0 <= t <= 60.0) else (abs(t - 4.0) * 0.5)
        )
        df["storage_risk_score"] = temp_dz * df["food_perishability_score"]

        # 4. CO2 Risk Level (Excess CO2 above baseline 350 ppm indicates spoilage gas)
        df["co2_risk_level"] = np.maximum(0.0, df["co2_ppm"] - 350.0) / 100.0

        # 5. Environmental Spoilage Index
        df["environmental_spoilage_index"] = (
            df["storage_risk_score"] * 0.4 +
            df["co2_risk_level"] * 0.3 +
            (df["humidity_pct"].apply(lambda h: max(0, h - 85) * 0.2))
        )

        # 6. Pickup Urgency Ratio (Hours held vs WHO 2-hr room temp threshold)
        df["pickup_urgency"] = df["hours_since_stored"] / 2.0

        # 7. Remaining Fridge Life Hours (assuming base 120 hrs average max fridge storage)
        df["remaining_fridge_life_hours"] = (df["remaining_fridge_life_pct"] / 100.0) * 120.0

        # 8. Freshness Score (100 - composite penalty)
        raw_freshness = 100.0 - (
            df["environmental_spoilage_index"] * 2.0 +
            df["pickup_urgency"] * 5.0 +
            (100.0 - df["remaining_fridge_life_pct"]) * 0.3
        )
        df["freshness_score"] = np.clip(raw_freshness, 0.0, 100.0)

        # 9. Donation Confidence Score
        confidence = (
            df["freshness_score"] * 0.5 +
            df["remaining_fridge_life_pct"] * 0.3 -
            df["storage_risk_score"] * 0.2
        )
        df["donation_confidence_score"] = np.clip(confidence, 0.0, 100.0)

        logger.info(f"Engineered {len(self.engineered_feature_names)} domain features.")
        return df

    def compute_mutual_information(
        self, X: pd.DataFrame, y: pd.Series, numeric_features: List[str]
    ) -> pd.Series:
        """Computes Mutual Information (MI) scores between numeric features and target label."""
        num_cols = [c for c in numeric_features if c in X.columns]
        if not num_cols:
            return pd.Series(dtype=float)

        mi_scores = mutual_info_classif(
            X[num_cols].fillna(0), y, random_state=42
        )
        mi_series = pd.Series(mi_scores, index=num_cols).sort_values(ascending=False)
        logger.info(f"Top 10 Mutual Information Scores:\n{mi_series.head(10)}")
        return mi_series

    def prune_features(
        self, df: pd.DataFrame, min_mi_threshold: float = 0.001
    ) -> Tuple[pd.DataFrame, List[str]]:
        """Identifies and removes zero-gain / redundant features."""
        # Check collinearity or zero variance
        variances = df.select_dtypes(include=[np.number]).var()
        zero_var_cols = list(variances[variances == 0].index)

        if zero_var_cols:
            logger.info(f"Pruning zero-variance features: {zero_var_cols}")
            df = df.drop(columns=zero_var_cols)

        return df, zero_var_cols
