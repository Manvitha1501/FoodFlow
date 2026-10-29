"""
Stage 2 — Exploratory Data Analysis
Donation Success Prediction Model
Covers: class distribution, correlation matrix, histograms,
        count plots, boxplots, feature distributions, outlier detection
"""

import pandas as pd
import numpy as np
import matplotlib
matplotlib.use("Agg")   # non-interactive backend — saves files without opening windows
import matplotlib.pyplot as plt
import matplotlib.gridspec as gridspec
import seaborn as sns
import warnings
warnings.filterwarnings("ignore")

# ── consistent style ──────────────────────────────────────────────────────────
sns.set_theme(style="whitegrid", palette="muted")
plt.rcParams.update({"figure.dpi": 120, "axes.titlesize": 12,
                     "axes.labelsize": 10, "xtick.labelsize": 9,
                     "ytick.labelsize": 9})

# ── load data ─────────────────────────────────────────────────────────────────
df = pd.read_csv("donation_success_risk_dataset.csv")

# label for readable plots
df["success_label"] = df["donation_success"].map({1: "Success", 0: "Failure"})

NUMERICAL = [
    "quantity_meals", "food_age_hours", "remaining_shelf_life_hours",
    "donor_reliability_score", "ngo_capacity", "ngo_available_vehicles",
    "ngo_active_volunteers", "ngo_average_pickup_delay_minutes",
    "ngo_completion_rate", "ngo_trust_score", "distance_km",
    "estimated_travel_time_minutes", "pickup_hour", "festival_day"
]

CATEGORICAL = [
    "food_category", "packaging_quality", "storage_condition",
    "traffic_level", "weather_condition", "day_of_week"
]

# ─────────────────────────────────────────────────────────────────────────────
# PLOT 1 — Class Distribution (donation_success)
# ─────────────────────────────────────────────────────────────────────────────
fig, axes = plt.subplots(1, 2, figsize=(12, 5))
fig.suptitle("Plot 1 — Target Variable: Donation Success Distribution", fontsize=13, fontweight="bold")

counts = df["donation_success"].value_counts().sort_index()
labels = ["Failure (0)", "Success (1)"]
colors = ["#e07070", "#70b8e0"]

# bar chart
axes[0].bar(labels, counts.values, color=colors, edgecolor="white", width=0.5)
for i, v in enumerate(counts.values):
    axes[0].text(i, v + 200, f"{v:,}\n({v/len(df)*100:.1f}%)",
                 ha="center", fontsize=10, fontweight="bold")
axes[0].set_title("Count of Success vs Failure")
axes[0].set_ylabel("Number of Donations")
axes[0].set_ylim(0, counts.max() * 1.15)

# pie chart
axes[1].pie(counts.values, labels=labels, colors=colors, autopct="%1.1f%%",
            startangle=90, wedgeprops={"edgecolor": "white", "linewidth": 2},
            textprops={"fontsize": 11})
axes[1].set_title("Proportion of Success vs Failure")

plt.tight_layout()
plt.savefig("plot1_class_distribution.png", bbox_inches="tight")
print("✓ Plot 1 saved — Class Distribution")


# ─────────────────────────────────────────────────────────────────────────────
# PLOT 2 — Correlation Matrix (numerical features only)
# ─────────────────────────────────────────────────────────────────────────────
corr_cols = NUMERICAL + ["donation_success"]
corr_matrix = df[corr_cols].corr()

fig, ax = plt.subplots(figsize=(14, 11))
fig.suptitle("Plot 2 — Correlation Matrix (Numerical Features + Target)", fontsize=13, fontweight="bold")

mask = np.triu(np.ones_like(corr_matrix, dtype=bool))  # upper triangle mask
sns.heatmap(corr_matrix, mask=mask, annot=True, fmt=".2f", cmap="coolwarm",
            center=0, linewidths=0.5, ax=ax, annot_kws={"size": 7},
            vmin=-1, vmax=1)
ax.set_title("Lower Triangle — Pearson Correlation Coefficients")

plt.tight_layout()
plt.savefig("plot2_correlation_matrix.png", bbox_inches="tight")
print("✓ Plot 2 saved — Correlation Matrix")


# ─────────────────────────────────────────────────────────────────────────────
# PLOT 3 — Histograms: Distribution of all numerical features
# ─────────────────────────────────────────────────────────────────────────────
fig, axes = plt.subplots(4, 4, figsize=(18, 14))
fig.suptitle("Plot 3 — Histograms: Numerical Feature Distributions", fontsize=13, fontweight="bold")
axes = axes.flatten()

for i, col in enumerate(NUMERICAL):
    axes[i].hist(df[col], bins=40, color="#5b8db8", edgecolor="white", alpha=0.85)
    axes[i].axvline(df[col].mean(), color="red", linestyle="--", linewidth=1.2, label=f"Mean={df[col].mean():.1f}")
    axes[i].axvline(df[col].median(), color="orange", linestyle="--", linewidth=1.2, label=f"Median={df[col].median():.1f}")
    axes[i].set_title(col, fontsize=9)
    axes[i].legend(fontsize=7)

# hide unused subplots
for j in range(len(NUMERICAL), len(axes)):
    axes[j].set_visible(False)

plt.tight_layout()
plt.savefig("plot3_histograms.png", bbox_inches="tight")
print("✓ Plot 3 saved — Histograms")


# ─────────────────────────────────────────────────────────────────────────────
# PLOT 4 — Count Plots: Categorical features vs donation_success
# ─────────────────────────────────────────────────────────────────────────────
fig, axes = plt.subplots(2, 3, figsize=(18, 10))
fig.suptitle("Plot 4 — Count Plots: Categorical Features vs Donation Success", fontsize=13, fontweight="bold")
axes = axes.flatten()

for i, col in enumerate(CATEGORICAL):
    ct = pd.crosstab(df[col], df["success_label"], normalize="index") * 100
    ct.plot(kind="bar", ax=axes[i], color=["#e07070", "#70b8e0"],
            edgecolor="white", width=0.65, rot=30)
    axes[i].set_title(f"{col} vs Success Rate (%)")
    axes[i].set_ylabel("% within category")
    axes[i].set_xlabel(col)
    axes[i].legend(title="Outcome", fontsize=8)
    axes[i].set_ylim(0, 115)
    # add percentage labels
    for bar in axes[i].patches:
        h = bar.get_height()
        if h > 2:
            axes[i].text(bar.get_x() + bar.get_width() / 2, h + 1,
                         f"{h:.0f}%", ha="center", va="bottom", fontsize=7)

plt.tight_layout()
plt.savefig("plot4_count_plots.png", bbox_inches="tight")
print("✓ Plot 4 saved — Count Plots")


# ─────────────────────────────────────────────────────────────────────────────
# PLOT 5 — Boxplots: Key numerical features split by donation_success
# ─────────────────────────────────────────────────────────────────────────────
key_num = [
    "remaining_shelf_life_hours", "estimated_travel_time_minutes",
    "distance_km", "ngo_average_pickup_delay_minutes",
    "ngo_completion_rate", "ngo_trust_score",
    "donor_reliability_score", "food_age_hours"
]

fig, axes = plt.subplots(2, 4, figsize=(18, 10))
fig.suptitle("Plot 5 — Boxplots: Key Features Split by Donation Success", fontsize=13, fontweight="bold")
axes = axes.flatten()

for i, col in enumerate(key_num):
    sns.boxplot(data=df, x="success_label", y=col, ax=axes[i],
                palette={"Success": "#70b8e0", "Failure": "#e07070"},
                width=0.5, linewidth=1.2)
    axes[i].set_title(col, fontsize=9)
    axes[i].set_xlabel("")

plt.tight_layout()
plt.savefig("plot5_boxplots.png", bbox_inches="tight")
print("✓ Plot 5 saved — Boxplots")


# ─────────────────────────────────────────────────────────────────────────────
# PLOT 6 — Feature Distributions: KDE overlay (Success vs Failure)
# ─────────────────────────────────────────────────────────────────────────────
fig, axes = plt.subplots(3, 4, figsize=(18, 13))
fig.suptitle("Plot 6 — KDE: Feature Distributions by Outcome (Success vs Failure)", fontsize=13, fontweight="bold")
axes = axes.flatten()

kde_features = [
    "remaining_shelf_life_hours", "estimated_travel_time_minutes",
    "distance_km", "ngo_average_pickup_delay_minutes",
    "ngo_completion_rate", "ngo_trust_score",
    "donor_reliability_score", "food_age_hours",
    "quantity_meals", "ngo_capacity",
    "ngo_available_vehicles", "ngo_active_volunteers"
]

for i, col in enumerate(kde_features):
    for label, color in [("Success", "#1f77b4"), ("Failure", "#d62728")]:
        subset = df[df["success_label"] == label][col]
        subset.plot.kde(ax=axes[i], label=label, color=color, linewidth=1.8)
    axes[i].set_title(col, fontsize=9)
    axes[i].legend(fontsize=8)
    axes[i].set_xlabel("")

plt.tight_layout()
plt.savefig("plot6_kde_distributions.png", bbox_inches="tight")
print("✓ Plot 6 saved — KDE Distributions")


# ─────────────────────────────────────────────────────────────────────────────
# PLOT 7 — Outlier Detection: IQR-based boxplots for skewed features
# ─────────────────────────────────────────────────────────────────────────────
skewed_features = [
    "quantity_meals", "food_age_hours", "distance_km",
    "estimated_travel_time_minutes", "ngo_average_pickup_delay_minutes",
    "remaining_shelf_life_hours"
]

fig, axes = plt.subplots(1, 6, figsize=(20, 6))
fig.suptitle("Plot 7 — Outlier Detection: IQR Boxplots (Skewed Features)", fontsize=13, fontweight="bold")

for i, col in enumerate(skewed_features):
    axes[i].boxplot(df[col], vert=True, patch_artist=True,
                    boxprops=dict(facecolor="#aec6e8", color="#1a5276"),
                    medianprops=dict(color="red", linewidth=2),
                    whiskerprops=dict(color="#1a5276"),
                    flierprops=dict(marker="o", markerfacecolor="#e07070",
                                    markersize=2, alpha=0.4))
    axes[i].set_title(col, fontsize=8)

    # compute IQR stats
    Q1, Q3 = df[col].quantile(0.25), df[col].quantile(0.75)
    IQR = Q3 - Q1
    lower, upper = Q1 - 1.5 * IQR, Q3 + 1.5 * IQR
    outliers = df[(df[col] < lower) | (df[col] > upper)]
    axes[i].set_xlabel(f"Outliers: {len(outliers)}\n({len(outliers)/len(df)*100:.1f}%)", fontsize=8)

plt.tight_layout()
plt.savefig("plot7_outlier_detection.png", bbox_inches="tight")
print("✓ Plot 7 saved — Outlier Detection")


# ─────────────────────────────────────────────────────────────────────────────
# PLOT 8 — Risk Level vs Donation Success (stacked bar)
# ─────────────────────────────────────────────────────────────────────────────
fig, axes = plt.subplots(1, 2, figsize=(14, 6))
fig.suptitle("Plot 8 — Risk Level vs Donation Success", fontsize=13, fontweight="bold")

ct_count = pd.crosstab(df["risk_level"], df["success_label"])
ct_pct   = pd.crosstab(df["risk_level"], df["success_label"], normalize="index") * 100

# enforce order
order = ["Low", "Medium", "High"]
ct_count = ct_count.loc[order]
ct_pct   = ct_pct.loc[order]

ct_count.plot(kind="bar", stacked=True, ax=axes[0],
              color=["#e07070", "#70b8e0"], edgecolor="white", rot=0)
axes[0].set_title("Counts — Risk Level vs Outcome")
axes[0].set_ylabel("Number of Donations")
axes[0].legend(title="Outcome")

ct_pct.plot(kind="bar", stacked=True, ax=axes[1],
            color=["#e07070", "#70b8e0"], edgecolor="white", rot=0)
axes[1].set_title("Proportion (%) — Risk Level vs Outcome")
axes[1].set_ylabel("Percentage (%)")
axes[1].legend(title="Outcome")

plt.tight_layout()
plt.savefig("plot8_risk_vs_success.png", bbox_inches="tight")
print("✓ Plot 8 saved — Risk Level vs Donation Success")


# ─────────────────────────────────────────────────────────────────────────────
# PLOT 9 — Pickup Hour and Day of Week vs Success Rate
# ─────────────────────────────────────────────────────────────────────────────
fig, axes = plt.subplots(1, 2, figsize=(16, 6))
fig.suptitle("Plot 9 — Temporal Features vs Donation Success Rate", fontsize=13, fontweight="bold")

# success rate per hour
hour_success = df.groupby("pickup_hour")["donation_success"].mean() * 100
axes[0].bar(hour_success.index, hour_success.values, color="#5b8db8", edgecolor="white")
axes[0].axhline(df["donation_success"].mean() * 100, color="red",
                linestyle="--", linewidth=1.5, label=f"Overall mean: {df['donation_success'].mean()*100:.1f}%")
axes[0].set_title("Success Rate by Pickup Hour")
axes[0].set_xlabel("Hour of Day (0–23)")
axes[0].set_ylabel("Success Rate (%)")
axes[0].legend()

# success rate per day
day_order = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
day_success = df.groupby("day_of_week")["donation_success"].mean().reindex(day_order) * 100
axes[1].bar(day_success.index, day_success.values, color="#7dbb7d", edgecolor="white")
axes[1].axhline(df["donation_success"].mean() * 100, color="red",
                linestyle="--", linewidth=1.5, label=f"Overall mean: {df['donation_success'].mean()*100:.1f}%")
axes[1].set_title("Success Rate by Day of Week")
axes[1].set_xlabel("Day")
axes[1].set_ylabel("Success Rate (%)")
axes[1].tick_params(axis="x", rotation=30)
axes[1].legend()

plt.tight_layout()
plt.savefig("plot9_temporal_features.png", bbox_inches="tight")
print("✓ Plot 9 saved — Temporal Features")


# ─────────────────────────────────────────────────────────────────────────────
# PLOT 10 — Interaction: Shelf Life vs Travel Time (coloured by outcome)
# ─────────────────────────────────────────────────────────────────────────────
fig, ax = plt.subplots(figsize=(12, 7))
fig.suptitle("Plot 10 — Key Interaction: Shelf Life vs Travel Time by Outcome",
             fontsize=13, fontweight="bold")

sample = df.sample(3000, random_state=42)  # sample for readability
colors_map = {"Success": "#1f77b4", "Failure": "#d62728"}

for label, grp in sample.groupby("success_label"):
    ax.scatter(grp["remaining_shelf_life_hours"], grp["estimated_travel_time_minutes"],
               label=label, color=colors_map[label], alpha=0.35, s=18, edgecolors="none")

ax.set_xlabel("Remaining Shelf Life (hours)")
ax.set_ylabel("Estimated Travel Time (minutes)")
ax.set_title("Failure clusters where shelf life is low AND travel time is high")
ax.legend(title="Outcome", fontsize=10)

plt.tight_layout()
plt.savefig("plot10_shelf_vs_travel_scatter.png", bbox_inches="tight")
print("✓ Plot 10 saved — Interaction Scatter")


print("\n" + "="*60)
print("ALL 10 EDA PLOTS GENERATED SUCCESSFULLY")
print("="*60)
