import pandas as pd
import numpy as np

df = pd.read_csv('donation_success_risk_dataset.csv')

skewed = [
    'quantity_meals', 'food_age_hours', 'distance_km',
    'estimated_travel_time_minutes', 'ngo_average_pickup_delay_minutes',
    'remaining_shelf_life_hours'
]

header = f"{'Feature':<40} {'Q1':>6} {'Q3':>6} {'IQR':>6} {'Lower':>7} {'Upper':>7} {'Outliers':>9} {'Pct':>6}"
print(header)
print('-' * len(header))

for col in skewed:
    Q1  = df[col].quantile(0.25)
    Q3  = df[col].quantile(0.75)
    IQR = Q3 - Q1
    lower = Q1 - 1.5 * IQR
    upper = Q3 + 1.5 * IQR
    n_out = len(df[(df[col] < lower) | (df[col] > upper)])
    pct   = n_out / len(df) * 100
    print(f"{col:<40} {Q1:>6.1f} {Q3:>6.1f} {IQR:>6.1f} {lower:>7.1f} {upper:>7.1f} {n_out:>9,} {pct:>5.1f}%")
