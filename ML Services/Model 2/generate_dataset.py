"""
Synthetic Dataset Generator
Dynamic Donation Success & Risk Prediction
Generates 20,000-28,000 realistic synthetic rows with logic-driven
(not random) labels so ML models have real signal to learn from.
"""

import numpy as np
import pandas as pd
from datetime import datetime

np.random.seed(42)

N_ROWS = 24000  # within 20,000-28,000 range

# ----------------------------------------------------------------------
# 1. DONATION FIELDS
# ----------------------------------------------------------------------
food_categories = ["Cooked Meals", "Packaged Food", "Fruits & Vegetables",
                    "Dairy", "Bakery", "Grains & Staples"]
# perishability weight per category (higher = spoils faster)
perishability_map = {
    "Cooked Meals": 0.9, "Dairy": 0.85, "Fruits & Vegetables": 0.75,
    "Bakery": 0.6, "Packaged Food": 0.2, "Grains & Staples": 0.15
}

donation_id = [f"DN{100000+i}" for i in range(N_ROWS)]
food_category = np.random.choice(food_categories, N_ROWS,
                                  p=[0.30, 0.20, 0.20, 0.10, 0.10, 0.10])
quantity_meals = np.random.gamma(shape=2.2, scale=45, size=N_ROWS).astype(int) + 5
quantity_meals = np.clip(quantity_meals, 5, 1500)

food_age_hours = np.round(np.random.exponential(scale=2.5, size=N_ROWS), 1)
food_age_hours = np.clip(food_age_hours, 0, 24)

# remaining shelf life depends on category perishability + age already used
base_shelf_life = np.array([np.random.normal(
    loc=(1 - perishability_map[c]) * 40 + 4, scale=4) for c in food_category])
remaining_shelf_life_hours = np.clip(base_shelf_life - food_age_hours * 0.3, 0.5, 72)
remaining_shelf_life_hours = np.round(remaining_shelf_life_hours, 1)

packaging_quality = np.random.choice(["Poor", "Average", "Good"], N_ROWS,
                                      p=[0.15, 0.45, 0.40])
storage_condition = np.random.choice(["Refrigerated", "Room Temperature", "Improper"],
                                      N_ROWS, p=[0.35, 0.45, 0.20])

# ----------------------------------------------------------------------
# 2. DONOR FIELD
# ----------------------------------------------------------------------
donor_reliability_score = np.round(np.clip(np.random.beta(6, 2, N_ROWS), 0, 1), 2)

# ----------------------------------------------------------------------
# 3. NGO FIELDS
# ----------------------------------------------------------------------
ngo_capacity = np.random.randint(50, 2000, N_ROWS)
ngo_available_vehicles = np.random.poisson(3, N_ROWS)
ngo_available_vehicles = np.clip(ngo_available_vehicles, 0, 12)
ngo_active_volunteers = np.random.poisson(6, N_ROWS)
ngo_active_volunteers = np.clip(ngo_active_volunteers, 0, 30)

ngo_average_pickup_delay_minutes = np.round(np.random.gamma(2, 18, N_ROWS), 1)
ngo_average_pickup_delay_minutes = np.clip(ngo_average_pickup_delay_minutes, 0, 240)

ngo_completion_rate = np.round(np.clip(np.random.beta(7, 2, N_ROWS), 0, 1), 2)
ngo_trust_score = np.round(np.clip(
    0.6 * ngo_completion_rate + 0.4 * np.random.beta(6, 2, N_ROWS), 0, 1), 2)

# ----------------------------------------------------------------------
# 4. LOGISTICS FIELDS
# ----------------------------------------------------------------------
distance_km = np.round(np.random.exponential(scale=8, size=N_ROWS) + 0.5, 1)
distance_km = np.clip(distance_km, 0.5, 60)

traffic_level = np.random.choice(["Low", "Medium", "High"], N_ROWS,
                                  p=[0.35, 0.40, 0.25])
traffic_speed_factor = pd.Series(traffic_level).map(
    {"Low": 1.0, "Medium": 1.4, "High": 2.0}).values

weather_condition = np.random.choice(["Clear", "Cloudy", "Rain", "Storm", "Extreme Heat"],
                                      N_ROWS, p=[0.45, 0.25, 0.18, 0.07, 0.05])
weather_delay_factor = pd.Series(weather_condition).map(
    {"Clear": 1.0, "Cloudy": 1.05, "Rain": 1.3, "Storm": 1.8, "Extreme Heat": 1.15}).values

estimated_travel_time_minutes = np.round(
    (distance_km * 2.4) * traffic_speed_factor * weather_delay_factor
    + np.random.normal(0, 3, N_ROWS), 1)
estimated_travel_time_minutes = np.clip(estimated_travel_time_minutes, 3, 240)

pickup_hour = np.random.randint(0, 24, N_ROWS)
day_of_week = np.random.choice(
    ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"],
    N_ROWS)
festival_day = np.random.choice([0, 1], N_ROWS, p=[0.90, 0.10])

# ----------------------------------------------------------------------
# 5. RISK SCORE (weighted logic + noise) -> risk_level & donation_success
# ----------------------------------------------------------------------
total_transit_buffer_hours = estimated_travel_time_minutes / 60.0

# individual risk components, each normalized to roughly 0-1
r_shelf = np.clip(
    (total_transit_buffer_hours + 0.5) / np.maximum(remaining_shelf_life_hours, 0.1) - 0.3,
    0, 1)
r_ngo_completion = 1 - ngo_completion_rate
r_ngo_delay = np.clip(ngo_average_pickup_delay_minutes / 180, 0, 1)
r_ngo_trust = 1 - ngo_trust_score
r_traffic = pd.Series(traffic_level).map({"Low": 0.1, "Medium": 0.45, "High": 0.85}).values
r_weather = pd.Series(weather_condition).map(
    {"Clear": 0.05, "Cloudy": 0.15, "Rain": 0.5, "Storm": 0.9, "Extreme Heat": 0.35}).values
r_distance = np.clip(distance_km / 40, 0, 1)
r_capacity = np.clip((quantity_meals - ngo_capacity) / np.maximum(ngo_capacity, 1), 0, 1)
r_vehicle = np.clip(1 - (ngo_available_vehicles / 6), 0, 1)
r_volunteer = np.clip(1 - (ngo_active_volunteers / 10), 0, 1)
r_donor = 1 - donor_reliability_score
r_packaging = pd.Series(packaging_quality).map(
    {"Good": 0.05, "Average": 0.3, "Poor": 0.7}).values
r_storage = pd.Series(storage_condition).map(
    {"Refrigerated": 0.05, "Room Temperature": 0.3, "Improper": 0.8}).values
r_festival = festival_day * 0.3

risk_score = (
    0.20 * r_shelf +
    0.14 * r_ngo_completion +
    0.10 * r_ngo_delay +
    0.08 * r_ngo_trust +
    0.10 * r_traffic +
    0.10 * r_weather +
    0.08 * r_distance +
    0.08 * r_capacity +
    0.03 * r_vehicle +
    0.03 * r_volunteer +
    0.03 * r_donor +
    0.02 * r_packaging +
    0.02 * r_storage +
    0.04 * r_festival
)

# add noise so classes aren't perfectly separable
risk_score = np.clip(risk_score + np.random.normal(0, 0.07, N_ROWS), 0, 1)

# rescale using percentiles so we get a realistic, non-degenerate spread
# across Low/Medium/High instead of everything clustering near zero
p50, p80 = np.percentile(risk_score, [50, 80])
risk_level = np.where(risk_score <= p50, "Low",
              np.where(risk_score <= p80, "Medium", "High"))
risk_level = pd.Categorical(risk_level, categories=["Low", "Medium", "High"])

# success is inversely related to risk: use a percentile cut so we get a
# realistic ~78/22 success/failure split, then flip a small % randomly
# (noise) so the label isn't a deterministic function of risk_score alone
success_cut = np.percentile(risk_score, 78)
donation_success = (risk_score <= success_cut).astype(int)
flip_mask = np.random.rand(N_ROWS) < 0.015  # Clean 1.5% label noise
donation_success = np.where(flip_mask, 1 - donation_success, donation_success)

# ----------------------------------------------------------------------
# ASSEMBLE DATAFRAME
# ----------------------------------------------------------------------
df = pd.DataFrame({
    "donation_id": donation_id,
    "food_category": food_category,
    "quantity_meals": quantity_meals,
    "food_age_hours": food_age_hours,
    "remaining_shelf_life_hours": remaining_shelf_life_hours,
    "packaging_quality": packaging_quality,
    "storage_condition": storage_condition,
    "donor_reliability_score": donor_reliability_score,
    "ngo_capacity": ngo_capacity,
    "ngo_available_vehicles": ngo_available_vehicles,
    "ngo_active_volunteers": ngo_active_volunteers,
    "ngo_average_pickup_delay_minutes": ngo_average_pickup_delay_minutes,
    "ngo_completion_rate": ngo_completion_rate,
    "ngo_trust_score": ngo_trust_score,
    "distance_km": distance_km,
    "estimated_travel_time_minutes": estimated_travel_time_minutes,
    "traffic_level": traffic_level,
    "weather_condition": weather_condition,
    "pickup_hour": pickup_hour,
    "day_of_week": day_of_week,
    "festival_day": festival_day,
    "risk_level": risk_level,
    "donation_success": donation_success
})

# shuffle rows
df = df.sample(frac=1, random_state=42).reset_index(drop=True)

out_path = "donation_success_risk_dataset.csv"
df.to_csv(out_path, index=False)

print("Shape:", df.shape)
print("\nRisk level distribution:\n", df["risk_level"].value_counts(normalize=True))
print("\nSuccess distribution:\n", df["donation_success"].value_counts(normalize=True))
print("\nSaved to:", out_path)
