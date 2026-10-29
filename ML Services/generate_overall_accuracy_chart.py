import os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

# FoodFlow 3-Model Accuracy Performance Data
models = [
    "Model 1:\nFood Readiness\n(XGBoost)",
    "Model 2:\nDelivery Risk\n(Ensemble)",
    "Model 3:\nVisual Food Scanner\n(EfficientNetB0)"
]

accuracies = [81.76, 81.17, 94.20]
f1_scores = [84.12, 88.75, 94.35]
colors = ["#3498db", "#f39c12", "#2ecc71"]

# Figure Setup
fig, ax = plt.subplots(figsize=(10, 6), dpi=300)

x = range(len(models))
width = 0.38

bars1 = ax.bar([p - width/2 for p in x], accuracies, width, label="Test Accuracy (%)", color=colors, edgecolor="#2c3e50", linewidth=1.5)
bars2 = ax.bar([p + width/2 for p in x], f1_scores, width, label="F1-Score / Weighted Metric (%)", color=[c + "88" for c in colors], edgecolor="#2c3e50", linewidth=1.2, hatch="//")

# Title & Labels
ax.set_title("FoodFlow AI Engine: Model Performance Metrics Across Architecture", fontsize=14, fontweight="bold", pad=20, color="#1a252f")
ax.set_ylabel("Performance Metric (%)", fontsize=12, fontweight="bold", labelpad=10)
ax.set_xticks(x)
ax.set_xticklabels(models, fontsize=11, fontweight="bold")
ax.set_ylim(60, 102)
ax.grid(axis="y", linestyle="--", alpha=0.5)
ax.legend(loc="upper left", fontsize=11, framealpha=0.9)

# Value Labels on Bars
for bar in bars1:
    yval = bar.get_height()
    ax.text(bar.get_x() + bar.get_width()/2.0, yval + 1.2, f"{yval:.2f}%", ha="center", va="bottom", fontweight="bold", fontsize=11, color="#1a252f")

for bar in bars2:
    yval = bar.get_height()
    ax.text(bar.get_x() + bar.get_width()/2.0, yval + 1.2, f"{yval:.2f}%", ha="center", va="bottom", fontweight="bold", fontsize=10, color="#555555")

plt.tight_layout()
output_path = "accuracy.png"
plt.savefig(output_path, dpi=300)
print(f"Successfully generated {output_path} with updated Model 3 accuracy (94.20%)!")
