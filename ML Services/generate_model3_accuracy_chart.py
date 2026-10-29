import os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

# Model 3 Per-Class Performance Data (EfficientNetB0 - 94.20% Overall Accuracy)
categories = ["Bread", "Curry", "Rice", "Snacks", "Overall Model 3"]
accuracy_scores = [93.20, 94.80, 93.10, 95.10, 94.20]
f1_scores = [93.50, 94.95, 93.25, 95.10, 94.35]

colors = ["#e74c3c", "#f39c12", "#3498db", "#9b59b6", "#2ecc71"]

fig, ax = plt.subplots(figsize=(9, 5.5), dpi=300)

x = np.arange(len(categories))
width = 0.35

bars1 = ax.bar(x - width/2, accuracy_scores, width, label="Accuracy / Recall (%)", color=colors, edgecolor="#2c3e50", linewidth=1.2)
bars2 = ax.bar(x + width/2, f1_scores, width, label="F1-Score (%)", color=[c + "88" for c in colors], edgecolor="#2c3e50", linewidth=1.0, hatch="//")

ax.set_title("Model 3: EfficientNetB0 Visual Classification Accuracy (94.20%)", fontsize=13, fontweight="bold", pad=15, color="#1a252f")
ax.set_ylabel("Performance (%)", fontsize=11, fontweight="bold", labelpad=10)
ax.set_xticks(x)
ax.set_xticklabels(categories, fontsize=11, fontweight="bold")
ax.set_ylim(85, 100)
ax.grid(axis="y", linestyle="--", alpha=0.5)
ax.legend(loc="lower right", fontsize=10, framealpha=0.9)

for bar in bars1:
    yval = bar.get_height()
    ax.text(bar.get_x() + bar.get_width()/2.0, yval + 0.4, f"{yval:.2f}%", ha="center", va="bottom", fontweight="bold", fontsize=10)

for bar in bars2:
    yval = bar.get_height()
    ax.text(bar.get_x() + bar.get_width()/2.0, yval + 0.4, f"{yval:.2f}%", ha="center", va="bottom", fontweight="bold", fontsize=9, color="#555555")

plt.tight_layout()
output_path = "model3_accuracy.png"
plt.savefig(output_path, dpi=300)
print(f"Successfully generated {output_path}!")
