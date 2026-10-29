import sys, os, pandas as pd, numpy as np, joblib
from sklearn.preprocessing import OneHotEncoder

os.makedirs("artifacts", exist_ok=True)
os.makedirs("encoders", exist_ok=True)
DROP_COLS = ["donation_id", "risk_level", "donation_success"]

if __name__ == "__main__":
    df = pd.read_csv("donation_success_risk_dataset.csv")
    print("Dataset loaded. Shape:", df.shape)
    df_model = df.drop(columns=DROP_COLS).copy()
    print("Dropped:", DROP_COLS, "| Shape:", df_model.shape)
    ordinal_maps = {
        "packaging_quality": {"Poor": 0, "Average": 1, "Good": 2},
        "storage_condition": {"Improper": 0, "Room Temperature": 1, "Refrigerated": 2},
        "traffic_level":     {"Low": 0, "Medium": 1, "High": 2},
        "weather_condition": {"Clear": 0, "Cloudy": 1, "Extreme Heat": 2, "Rain": 3, "Storm": 4},
    }
    for col, mapping in ordinal_maps.items():
        df_model[col] = df_model[col].map(mapping)
        assert df_model[col].isnull().sum() == 0, f"NaN in {col}"
        joblib.dump(mapping, f"encoders/ordinal_map_{col}.pkl")
        print(f"  Ordinal: {col} -> {mapping}")
    OHE_COLS = ["food_category", "day_of_week"]
    ohe = OneHotEncoder(drop="first", sparse_output=False, handle_unknown="ignore")
    ohe_array = ohe.fit_transform(df_model[OHE_COLS])
    ohe_cols  = ohe.get_feature_names_out(OHE_COLS)
    df_model  = df_model.drop(columns=OHE_COLS)
    df_model  = pd.concat([df_model, pd.DataFrame(ohe_array, columns=ohe_cols, index=df_model.index)], axis=1)
    joblib.dump(ohe, "encoders/ohe_encoder.pkl")
    print("OHE done. New cols:", ohe_cols.tolist())
    print("Shape after OHE:", df_model.shape)

    # Domain Feature Engineering
    travel_hours = df_model["estimated_travel_time_minutes"] / 60.0
    df_model["shelf_life_travel_margin"] = df_model["remaining_shelf_life_hours"] / (travel_hours + 0.01)
    df_model["perishability_urgency"] = df_model["food_age_hours"] / (df_model["remaining_shelf_life_hours"] + 0.01)
    df_model["ngo_capacity_ratio"] = df_model["ngo_capacity"] / (df_model["quantity_meals"] + 0.01)
    df_model["risk_severity_index"] = df_model["traffic_level"] * df_model["weather_condition"]
    print("Domain feature engineering added 4 new features. Shape:", df_model.shape)

    print("Missing values:", df_model.isnull().sum().sum())
    print("Object dtype remaining:", any(df_model.dtypes == "object"))
    X = df_model.copy()
    y = df["donation_success"].copy()
    spw = round((y==0).sum() / (y==1).sum(), 4)
    print("X shape:", X.shape, "| y shape:", y.shape)
    print("Success:", (y==1).sum(), "| Failure:", (y==0).sum())
    print("scale_pos_weight:", spw)
    joblib.dump(X,   "artifacts/X_processed.pkl")
    joblib.dump(y,   "artifacts/y_processed.pkl")
    joblib.dump(spw, "artifacts/scale_pos_weight.pkl")
    print("Saved X, y, scale_pos_weight to artifacts/")
    print(X.dtypes.to_string())
    print("DONE")
