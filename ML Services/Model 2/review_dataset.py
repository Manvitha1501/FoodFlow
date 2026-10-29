import pandas as pd
import numpy as np

df = pd.read_csv('donation_success_risk_dataset.csv')

print('=== SHAPE ===')
print(df.shape)

print('\n=== COLUMNS ===')
print(df.columns.tolist())

print('\n=== DTYPES ===')
print(df.dtypes)

print('\n=== MISSING VALUES ===')
print(df.isnull().sum())

print('\n=== DUPLICATES ===')
print('Duplicate rows:', df.duplicated().sum())
print('Duplicate donation_id:', df['donation_id'].duplicated().sum())

print('\n=== CLASS BALANCE: donation_success ===')
print(df['donation_success'].value_counts())
print(df['donation_success'].value_counts(normalize=True).round(3))

print('\n=== RISK LEVEL DISTRIBUTION ===')
print(df['risk_level'].value_counts())
print(df['risk_level'].value_counts(normalize=True).round(3))

print('\n=== DESCRIBE NUMERICAL ===')
print(df.describe().T.to_string())

print('\n=== CATEGORICAL UNIQUE VALUES ===')
cats = ['food_category','packaging_quality','storage_condition','traffic_level','weather_condition','day_of_week','risk_level']
for c in cats:
    print(f'{c}: {df[c].unique().tolist()}')

print('\n=== risk_level vs donation_success CROSSTAB ===')
print(pd.crosstab(df['risk_level'], df['donation_success'], normalize='index').round(3))

print('\n=== CORRELATION: risk_score proxy check ===')
# check if any numeric feature has suspiciously perfect correlation with target
nums = df.select_dtypes(include=np.number).drop(columns=['donation_success'])
corr = nums.corrwith(df['donation_success']).sort_values()
print(corr.round(3))
