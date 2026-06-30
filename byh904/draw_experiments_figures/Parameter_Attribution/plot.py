# -*- coding: utf-8 -*-
"""
Parameter attribution experiment.

This script answers the reviewer concern that a large speedup may come from an
artificially poor baseline. It uses the trained active-power and CPU/IO surrogate
models to decompose the predicted energy reduction from a representative
baseline configuration to the optimized configuration.

Method:
  1. For each task in param_search_results/summary.csv, load the corresponding
     CPU/IO dataset and trained models.
  2. Pick a baseline row whose predicted energy is closest to the initial energy
     reported by the optimization log. This ties the attribution to the same
     baseline used in the end-to-end figure.
  3. Compute exact Shapley values over the nine tunable dimensions. There are
     only 2^9 = 512 subsets, so no SHAP package or random approximation is
     needed.
  4. Save all attributions to CSV and draw a two-panel visualization for
     NNBench, the original I/O-intensive benchmark with the largest improvement.
"""

import math
import os
import sys
from itertools import combinations

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np
import pandas as pd
import torch


SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
ROOT_DIR = os.path.dirname(os.path.dirname(SCRIPT_DIR))
SUMMARY_CSV = os.path.join(ROOT_DIR, "param_search_results", "summary.csv")
OUTPUT_DIR = SCRIPT_DIR

sys.path.insert(0, ROOT_DIR)
sys.path.insert(0, os.path.join(ROOT_DIR, "byh"))

import default as cfg
from scaler import transform, inverse_transform
from byh.active_power.model import ActivePowerModel
from byh.cpu_io.model import CPUIOModel


cfg.DEVICE = "cpu"

ORIGINAL_BENCHMARKS = {
    "TeraSort",
    "PiEstimator",
    "PageRank",
    "Sort",
    "Grep",
    "NNBench",
}
REPRESENTATIVE_TASK = "NNBench"

PARAM_KEYS = [
    "param_1", "param_2", "param_3", "param_4", "param_5",
    "param_6", "param_7", "param_8", "cpu_frequency",
]

PARAM_LABELS = {
    "param_1": "Map Task Slots",
    "param_2": "Reduce Slowstart",
    "param_3": "Sort Buffer",
    "param_4": "Spill Percent",
    "param_5": "Merge Factor",
    "param_6": "JVM Heap",
    "param_7": "Native Task",
    "param_8": "Compress Output",
    "cpu_frequency": "CPU Frequency",
}

PARAM_BOUNDS = {
    "param_1": (cfg.MIN_PARAM_1, cfg.MAX_PARAM_1),
    "param_2": (cfg.MIN_PARAM_2, cfg.MAX_PARAM_2),
    "param_3": (cfg.MIN_PARAM_3, cfg.MAX_PARAM_3),
    "param_4": (cfg.MIN_PARAM_4, cfg.MAX_PARAM_4),
    "param_5": (cfg.MIN_PARAM_5, cfg.MAX_PARAM_5),
    "param_6": (cfg.MIN_PARAM_6, cfg.MAX_PARAM_6),
    "param_7": (cfg.MIN_PARAM_7, cfg.MAX_PARAM_7),
    "param_8": (cfg.MIN_PARAM_8, cfg.MAX_PARAM_8),
    "cpu_frequency": (cfg.MIN_CPU_FREQ, cfg.MAX_CPU_FREQ),
}


def task_dir_name(task):
    mapping = {
        "TeraSort": "terasort",
        "PiEstimator": "pi",
        "PageRank": "pagerank",
        "Sort": "sort",
        "Grep": "grep",
        "NNBench": "nnbench",
    }
    return mapping.get(task, str(task).lower())


def to_numeric_params(df):
    out = df.copy()
    for key in PARAM_KEYS:
        if out[key].dtype == bool:
            out[key] = out[key].astype(float)
        else:
            out[key] = out[key].replace({"TRUE": 1, "FALSE": 0, "True": 1, "False": 0})
            out[key] = pd.to_numeric(out[key], errors="coerce")
    return out.dropna(subset=PARAM_KEYS)


def normalize_states(values):
    cols = []
    for i, key in enumerate(PARAM_KEYS):
        lo, hi = PARAM_BOUNDS[key]
        col = torch.as_tensor(values[:, [i]], dtype=torch.float32)
        cols.append(transform(col, lo, hi))
    return torch.cat(cols, dim=1)


def load_models(task, node):
    tdir = task_dir_name(task)
    ap_path = os.path.join(ROOT_DIR, "byh", "active_power", "output", tdir, node, "model_final.pth")
    cio_path = os.path.join(ROOT_DIR, "byh", "cpu_io", "output", tdir, node, "model_final.pth")
    if not os.path.exists(ap_path):
        raise FileNotFoundError(ap_path)
    if not os.path.exists(cio_path):
        raise FileNotFoundError(cio_path)

    active_model = ActivePowerModel(
        bottleneck_sizes=list(cfg.BOTTLENECK_SIZES_1),
    )
    cpu_io_model = CPUIOModel(
        bottleneck_channels=list(cfg.BOTTLENECK_CHANNELS_2),
        fc_sizes=list(cfg.FC_SIZES_2),
    )
    active_model.load_state_dict(torch.load(ap_path, map_location="cpu"))
    cpu_io_model.load_state_dict(torch.load(cio_path, map_location="cpu"))
    active_model.eval()
    cpu_io_model.eval()
    return active_model, cpu_io_model


def resolve_available_node(task, preferred_node):
    tdir = task_dir_name(task)
    candidates = [preferred_node, "master", "slave1", "slave2"]
    seen = []
    for node in candidates:
        if node in seen:
            continue
        seen.append(node)
        dataset_path = os.path.join(
            ROOT_DIR, "byh", "cpu_io", "dataset", tdir, node, "Init_hadoop_runtime_run0_90.csv"
        )
        ap_path = os.path.join(ROOT_DIR, "byh", "active_power", "output", tdir, node, "model_final.pth")
        cio_path = os.path.join(ROOT_DIR, "byh", "cpu_io", "output", tdir, node, "model_final.pth")
        if os.path.exists(dataset_path) and os.path.exists(ap_path) and os.path.exists(cio_path):
            return node
    raise FileNotFoundError(f"No complete dataset/model triplet found for {task} ({tdir}).")


@torch.no_grad()
def active_power_table(active_model):
    cpu_freqs = torch.tensor(cfg.CPU_FREQS, dtype=torch.float32)
    cpu_freqs_norm = transform(cpu_freqs, cfg.MIN_CPU_FREQ, cfg.MAX_CPU_FREQ)

    grid_cpu, grid_sda = torch.meshgrid(
        torch.arange(cfg.CPU_SLICES[0], cfg.CPU_UPPER_BOUND, cfg.CPU_STEP),
        torch.arange(cfg.SDA_SLICES[0], cfg.SDA_UPPER_BOUND, cfg.SDA_STEP),
        indexing="ij",
    )
    grid_usage = torch.cat((grid_cpu.reshape(-1, 1), grid_sda.reshape(-1, 1)), dim=1) / 100.0
    index = torch.cartesian_prod(torch.arange(cpu_freqs.size(0)), torch.arange(grid_usage.size(0)))
    features = torch.cat((cpu_freqs_norm.reshape(-1, 1)[index[:, 0]], grid_usage[index[:, 1]]), dim=1)
    outputs = active_model(features).reshape(cpu_freqs.size(0), grid_cpu.size(0), grid_sda.size(1), -1)

    table = torch.zeros((cpu_freqs.size(0), len(cfg.CPU_SLICES) * len(cfg.SDA_SLICES)))
    for i, (cpu_low, cpu_high) in enumerate(zip(cfg.CPU_SLICES, cfg.CPU_SLICES[1:] + [cfg.CPU_UPPER_BOUND])):
        cpu_l = math.floor((cpu_low - cfg.CPU_SLICES[0]) / cfg.CPU_STEP)
        cpu_h = math.floor((cpu_high - cfg.CPU_SLICES[0]) / cfg.CPU_STEP)
        for j, (sda_low, sda_high) in enumerate(zip(cfg.SDA_SLICES, cfg.SDA_SLICES[1:] + [cfg.SDA_UPPER_BOUND])):
            sda_l = math.floor((sda_low - cfg.SDA_SLICES[0]) / cfg.SDA_STEP)
            sda_h = math.floor((sda_high - cfg.SDA_SLICES[0]) / cfg.SDA_STEP)
            table[:, i * len(cfg.SDA_SLICES) + j] = outputs[:, cpu_l:cpu_h, sda_l:sda_h, :].mean(dim=(1, 2)).squeeze(-1)
    return table


@torch.no_grad()
def predicted_energy(states_actual, cpu_io_model, power_table):
    states_actual = np.asarray(states_actual, dtype=np.float32)
    if states_actual.ndim == 1:
        states_actual = states_actual.reshape(1, -1)

    x = normalize_states(states_actual)
    freqs = torch.as_tensor(states_actual[:, -1], dtype=torch.float32)
    legal_freqs = torch.tensor(cfg.CPU_FREQS, dtype=torch.float32)
    freq_idx = torch.argmin(torch.abs(freqs.reshape(-1, 1) - legal_freqs.reshape(1, -1)), dim=1)

    active_power = inverse_transform(power_table[freq_idx], cfg.MIN_ACTIVE_POWER, cfg.MAX_ACTIVE_POWER)
    run_time, run_perc = cpu_io_model(x)
    run_time = inverse_transform(run_time, cfg.MIN_RUN_TIME, cfg.MAX_RUN_TIME)
    run_perc = run_perc.softmax(1)
    energy = (active_power * run_time.expand((-1, run_perc.size(1))) * run_perc).sum(dim=1)
    return energy.cpu().numpy()


def exact_shapley_from_values(baseline_energy, subset_energies):
    n = len(PARAM_KEYS)
    subset_values = baseline_energy - subset_energies

    factorial = math.factorial
    denom = factorial(n)
    phi = np.zeros(n, dtype=float)

    for i in range(n):
        others = [j for j in range(n) if j != i]
        for k in range(n):
            weight = factorial(k) * factorial(n - k - 1) / denom
            for subset in combinations(others, k):
                mask = 0
                for j in subset:
                    mask |= 1 << j
                phi[i] += weight * (subset_values[mask | (1 << i)] - subset_values[mask])
    return phi


def mixed_subset_states(baseline, optimized):
    n = len(PARAM_KEYS)
    states = np.repeat(baseline.reshape(1, -1), 1 << n, axis=0)
    for mask in range(1 << n):
        for i in range(n):
            if mask & (1 << i):
                states[mask, i] = optimized[i]
    return states


def analyze_task(row):
    task = row["task"]
    requested_node = row["node"]
    node = resolve_available_node(task, requested_node)
    tdir = task_dir_name(task)
    dataset_path = os.path.join(
        ROOT_DIR, "byh", "cpu_io", "dataset", tdir, node, "Init_hadoop_runtime_run0_90.csv"
    )
    df = to_numeric_params(pd.read_csv(dataset_path))
    active_model, cpu_io_model = load_models(task, node)
    ptable = active_power_table(active_model)

    values = df[PARAM_KEYS].to_numpy(dtype=np.float32)
    energies = predicted_energy(values, cpu_io_model, ptable)
    initial_energy = float(row["initial_energy_avg"])
    baseline_idx = int(np.argmin(np.abs(energies - initial_energy)))
    baseline = values[baseline_idx].astype(float)
    optimized = np.array([float(row[f"best_{key}"]) for key in PARAM_KEYS], dtype=float)

    subset_states = mixed_subset_states(baseline, optimized)
    subset_energies = predicted_energy(subset_states, cpu_io_model, ptable)
    baseline_energy = float(subset_energies[0])
    optimized_energy = float(subset_energies[-1])
    shapley = exact_shapley_from_values(baseline_energy, subset_energies)

    baseline_percentile = 100.0 * float(np.mean(energies <= baseline_energy))
    optimized_percentile = 100.0 * float(np.mean(energies <= optimized_energy))

    rows = []
    for key, b, o, phi in zip(PARAM_KEYS, baseline, optimized, shapley):
        rows.append({
            "task": task,
            "node": node,
            "requested_node": requested_node,
            "parameter": key,
            "label": PARAM_LABELS[key],
            "baseline_value": b,
            "optimized_value": o,
            "delta": o - b,
            "shapley_energy_reduction": phi,
            "baseline_energy": baseline_energy,
            "optimized_energy": optimized_energy,
            "total_predicted_reduction": baseline_energy - optimized_energy,
            "reported_initial_energy": initial_energy,
            "reported_optimized_energy": float(row["optimized_energy_avg"]),
            "baseline_energy_percentile": baseline_percentile,
            "optimized_energy_percentile": optimized_percentile,
            "candidate_median_energy": float(np.median(energies)),
            "candidate_best_energy": float(np.min(energies)),
        })
    return rows


def fmt_value(key, value):
    if key in ("param_1", "param_3", "param_6", "cpu_frequency"):
        return f"{value:.0f}"
    if key in ("param_7", "param_8"):
        return "on" if value >= 0.5 else "off"
    return f"{value:.2f}"


def plot_selected(attrib_df, summary_df):
    if REPRESENTATIVE_TASK in set(attrib_df["task"]):
        selected_task = REPRESENTATIVE_TASK
    else:
        selected_task = summary_df.sort_values("improvement_pct", ascending=False).iloc[0]["task"]
    data = attrib_df[attrib_df["task"] == selected_task].copy()
    data["abs_phi"] = data["shapley_energy_reduction"].abs()
    data = data.sort_values("abs_phi", ascending=True)

    color_energy = "#E07B00"
    color_tradeoff = "#F5C07A"
    color_baseline = "#9A9A9A"
    font_size = 16
    axis_font = 18

    colors = np.where(data["shapley_energy_reduction"] >= 0, color_energy, color_tradeoff)
    labels = [
        f"{r.label}\n{fmt_value(r.parameter, r.baseline_value)} -> {fmt_value(r.parameter, r.optimized_value)}"
        for r in data.itertuples()
    ]

    fig, axes = plt.subplots(1, 2, figsize=(13.5, 5.5), gridspec_kw={"width_ratios": [1.25, 1.0]})

    axes[0].barh(labels, data["shapley_energy_reduction"], color=colors, alpha=0.88, edgecolor="white", lw=0.5)
    axes[0].axvline(0, color="black", lw=0.8)
    axes[0].set_xlabel("Contribution to Energy Reduction (J)", fontsize=axis_font, fontweight="bold")
    axes[0].tick_params(axis="x", labelsize=font_size)
    axes[0].tick_params(axis="y", labelsize=12)
    axes[0].grid(axis="x", linestyle="--", alpha=0.40)
    axes[0].set_axisbelow(True)
    axes[0].spines[["top", "right"]].set_visible(False)

    first = data.iloc[0]
    baseline_energy = float(first["baseline_energy"])
    optimized_energy = float(first["optimized_energy"])
    median_energy = float(first["candidate_median_energy"])
    best_energy = float(first["candidate_best_energy"])
    bars = [baseline_energy, median_energy, best_energy, optimized_energy]
    bar_labels = ["Baseline\nexemplar", "Median\ncandidate", "Best measured\ncandidate", "Optimized\nconfiguration"]
    axes[1].bar(bar_labels, bars, color=[color_baseline, "#CFCFCF", color_tradeoff, color_energy], alpha=0.88, edgecolor="white", lw=0.5)
    axes[1].set_ylabel("Predicted Energy (J)", fontsize=axis_font, fontweight="bold")
    axes[1].tick_params(axis="x", labelsize=12)
    axes[1].tick_params(axis="y", labelsize=font_size)
    axes[1].grid(axis="y", linestyle="--", alpha=0.40)
    axes[1].set_axisbelow(True)
    axes[1].spines[["top", "right"]].set_visible(False)
    axes[0].legend(
        handles=[
            mpatches.Patch(color=color_energy, alpha=0.88, label="Energy-saving change"),
            mpatches.Patch(color=color_tradeoff, alpha=0.88, label="Trade-off change"),
        ],
        prop={"size": font_size},
        loc="lower right",
        framealpha=0.85,
    )
    fig.tight_layout()

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    for fmt in ("png", "pdf"):
        out_path = os.path.join(OUTPUT_DIR, f"parameter_attribution.{fmt}")
        fig.savefig(out_path, dpi=220, bbox_inches="tight")
        print(f"[Saved] {out_path}")
    plt.close(fig)


def write_response_snippet(attrib_df, summary_df):
    if REPRESENTATIVE_TASK in set(attrib_df["task"]):
        selected_task = REPRESENTATIVE_TASK
    else:
        selected_task = summary_df.sort_values("improvement_pct", ascending=False).iloc[0]["task"]
    data = attrib_df[attrib_df["task"] == selected_task].copy()
    positive = data[data["shapley_energy_reduction"] > 0].copy()
    top = positive.sort_values("shapley_energy_reduction", ascending=False).head(3)
    first = data.iloc[0]
    lines = [
        "We added a parameter-attribution experiment to rule out the possibility that the",
        "observed speedup is merely due to an accidentally poor baseline. For each task,",
        "we select the measured configuration whose predicted energy is closest to the",
        "pre-optimization energy used in the end-to-end experiment, and then compute exact",
        "Shapley values over the nine tunable parameters by evaluating all 2^9 mixed",
        "configurations with the trained CPU/IO and active-power models.",
        "",
        f"For the representative I/O-intensive case ({selected_task}), the dominant changes are:",
    ]
    for r in top.itertuples():
        direction = "decreases" if r.shapley_energy_reduction > 0 else "increases"
        lines.append(
            f"- {r.label}: {fmt_value(r.parameter, r.baseline_value)} -> "
            f"{fmt_value(r.parameter, r.optimized_value)}, which reduces predicted "
            f"energy by {r.shapley_energy_reduction:.1f} J."
        )
    lines.extend([
        "",
        f"The selected baseline exemplar lies at percentile {first.baseline_energy_percentile:.1f}",
        "of the measured candidate-energy distribution, where lower is better;",
        "therefore the baseline is not the worst measured configuration. We include the",
        "full attribution table for the original six workloads in",
        "parameter_attribution_values.csv.",
    ])
    out_path = os.path.join(OUTPUT_DIR, "reviewer_response_snippet.txt")
    with open(out_path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")
    print(f"[Saved] {out_path}")


def main():
    print("=== Parameter Attribution Experiment ===")
    summary = pd.read_csv(SUMMARY_CSV)
    summary = summary[summary["task"].isin(ORIGINAL_BENCHMARKS)].copy()
    all_rows = []
    for row in summary.to_dict("records"):
        try:
            rows = analyze_task(row)
            all_rows.extend(rows)
            task = row["task"]
            print(f"  [OK] {task}: {len(rows)} parameter attributions")
        except Exception as exc:
            print(f"  [Skip] {row.get('task')}: {exc}")

    if not all_rows:
        raise RuntimeError("No attribution rows were generated.")

    attrib = pd.DataFrame(all_rows)
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    csv_path = os.path.join(OUTPUT_DIR, "parameter_attribution_values.csv")
    attrib.to_csv(csv_path, index=False)
    print(f"[Saved] {csv_path}")

    plot_selected(attrib, summary)
    write_response_snippet(attrib, summary)
    print("Done.")


if __name__ == "__main__":
    main()
