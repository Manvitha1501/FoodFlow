import sys
import os

# Add model2_readiness to path so joblib can find preprocessing module
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "model2_readiness"))

import joblib
import pandas as pd
import numpy as np
from model2_readiness.feature_engineering import FeatureEngineer

pipeline = joblib.load("outputs/model2/donation_readiness_model.pkl")
model = pipeline["model"]
preprocessor_mgr = pipeline["preprocessor_mgr"]
engineer = FeatureEngineer()

# Single food sample: cold milk, 12 hrs stored, 80% fridge life left
sample = pd.DataFrame([{
    "temperature_c": 4.0,
    "humidity_pct": 85.0,
    "co2_ppm": 380.0,
    "light_lux": 10.0,
    "hours_since_stored": 12.0,
    "remaining_fridge_life_pct": 80.0,
    "batch_weight_kg": 2.5,
    "food_item": "milk",
    "food_category": "dairy",
}])

sample_eng = engineer.create_features(sample)
feature_cols = pipeline["feature_cols"]
X = preprocessor_mgr.transform(sample_eng[feature_cols])
pred = model.predict(X)
proba = model.predict_proba(X)

inv_map = {v: k for k, v in pipeline["label_map"].items()}
print("Prediction:", inv_map[pred[0]])
print("Probabilities:", dict(zip(["Low", "Medium", "High"], proba[0].round(3))))
