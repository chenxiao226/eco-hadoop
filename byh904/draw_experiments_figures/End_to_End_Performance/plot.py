# -*- coding: utf-8 -*-
"""
端到端性能实验绘图 — End-to-End Performance Comparison
只使用真实实验数据，缺数据的任务留空（不显示柱子）。

CSV 格式说明（byh/output/*.csv）：
  Epoch=0 行：完整 11 列（Task_Name, Lambda_Perf, Epoch, Best_Energy_Avg, ...）
  Epoch>0 行：只有 9 列，列向左偏移 2 位：
      CSV 列 'Task_Name'    → epoch 编号
      CSV 列 'Lambda_Perf'  → 实际 best_energy (W·s)
      CSV 列 'Best_Energy_Avg' → 实际 best_runtime (s)
"""

import os, glob
import pandas as pd
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches


# ── 路径配置 ─────────────────────────────────────────────────────────────────
SCRIPT_DIR  = os.path.dirname(os.path.abspath(__file__))
ROOT_DIR    = os.path.dirname(os.path.dirname(SCRIPT_DIR))
CSV_LOG_DIR = os.path.join(ROOT_DIR, 'byh', 'output')
OUTPUT_DIR  = SCRIPT_DIR

# ── 7个任务定义 ───────────────────────────────────────────────────────────────
TASKS = [
    dict(csv_name='terasort',     display='TeraSort',      task_type='io'),
    dict(csv_name='grep',         display='Grep',           task_type='cpu'),
    dict(csv_name='pi',           display='PiEstimator',    task_type='cpu'),
    dict(csv_name='nnbench',      display='NNBench',        task_type='io'),
    dict(csv_name='PageRank',     display='PageRank',       task_type='mixed'),
    dict(csv_name='sort',         display='Sort',           task_type='cpu'),
]

# ── CSV 解析（处理列偏移问题）────────────────────────────────────────────────
def load_real_data(csv_log_dir):
    """
    返回 dict: csv_name -> (init_energy, opt_energy, init_runtime, opt_runtime)
    只返回有完整优化结果的任务。
    取同一任务最早的那次运行（最保守、最稳定）。
    """
    real = {}
    if not os.path.isdir(csv_log_dir):
        print(f"[Warn] CSV dir not found: {csv_log_dir}")
        return real

    csv_files = sorted(glob.glob(os.path.join(csv_log_dir, '*.csv')))  # 时间戳正序→取最早

    seen = set()
    for fp in csv_files:
        try:
            df = pd.read_csv(fp, encoding='utf-8', dtype=str)
            if df.empty or 'Task_Name' not in df.columns:
                continue

            # ── 找 Epoch=0 完整行（Task_Name 是字符串任务名，非纯数字）
            first = df.iloc[0]
            task_name = str(first['Task_Name']).strip()
            if task_name.lstrip('-').replace('.', '', 1).isdigit():
                continue   # 第一行就是偏移行，无法识别任务名
            if task_name in seen:
                continue   # 已取过该任务的更早文件

            init_energy  = float(first['Best_Energy_Avg'])
            init_runtime = float(first['Best_RunTime_Avg'])

            # ── 找 Epoch>0 的偏移行（Task_Name 列 = 纯整数）
            epoch_rows = df.iloc[1:][df.iloc[1:]['Task_Name'].apply(
                lambda x: str(x).strip().lstrip('-').isdigit()
            )]

            if epoch_rows.empty:
                print(f"  [Skip] {task_name}: no epoch data in {os.path.basename(fp)}")
                continue

            # 取最后一个 epoch 行（最终优化结果）
            final = epoch_rows.iloc[-1]
            opt_energy  = float(final['Lambda_Perf'])     # 偏移后：energy
            opt_runtime = float(final['Best_Energy_Avg']) # 偏移后：runtime

            # 合理性校验
            if opt_energy <= 0 or opt_energy > init_energy * 1.5:
                print(f"  [Warn] {task_name}: suspicious opt_energy={opt_energy:.1f} "
                      f"(init={init_energy:.1f}), skip {os.path.basename(fp)}")
                continue

            drop = (init_energy - opt_energy) / init_energy * 100
            real[task_name] = (init_energy, opt_energy, init_runtime, opt_runtime)
            seen.add(task_name)
            print(f"  [Real] {task_name}: energy {init_energy:.1f} → {opt_energy:.1f} "
                  f"({drop:.1f}%), runtime {init_runtime:.1f}s → {opt_runtime:.1f}s")

        except Exception as e:
            print(f"  [Warn] {os.path.basename(fp)}: {e}")

    return real


# ── 绘图 ─────────────────────────────────────────────────────────────────────
COLOR_ENERGY = '#E07B00'
COLOR_TIME   = '#F5C07A'

def plot_end_to_end(real_data, tasks, out_dir):
    available = [t for t in tasks if t['csv_name'] in real_data]
    if not available:
        print("[Error] No real data available to plot.")
        return

    n      = len(available)
    labels = [t['display'] for t in available]
    init_e = np.array([real_data[t['csv_name']][0] for t in available])
    opt_e  = np.array([real_data[t['csv_name']][1] for t in available])
    init_r = np.array([real_data[t['csv_name']][2] for t in available])
    opt_r  = np.array([real_data[t['csv_name']][3] for t in available])
    e_drop = (init_e - opt_e) / init_e * 100
    t_chg  = (opt_r  - init_r) / init_r * 100

    x  = np.arange(n)
    w  = 0.32

    FONT_SIZE = 16
    AXIS_FONT = 18

    fig, ax = plt.subplots(figsize=(max(7, n * 2.0), 5.5))
    for i, (ed, tc) in enumerate(zip(e_drop, t_chg)):
        ax.bar(x[i] - w/2,  ed,  w, color=COLOR_ENERGY, alpha=0.88, edgecolor='white', lw=0.5)
        ax.bar(x[i] + w/2, -tc,  w, color=COLOR_TIME,   alpha=0.82, edgecolor='white', lw=0.5)

    ax.axhline(0, color='black', lw=0.8)
    ax.set_xticks(x); ax.set_xticklabels(labels, fontsize=AXIS_FONT, fontweight='bold')
    ax.set_ylabel('Improvement (%)', fontsize=AXIS_FONT, fontweight='bold')
    ax.tick_params(axis='y', labelsize=AXIS_FONT)
    ax.set_ylim(top=40)
    ax.set_yticks([t for t in ax.get_yticks() if t <= 35])
    ax.yaxis.grid(True, linestyle='--', alpha=0.40); ax.set_axisbelow(True)
    ax.spines[['top', 'right']].set_visible(False)
    ax.legend(handles=[
        mpatches.Patch(color=COLOR_ENERGY, alpha=0.88, label='Energy Reduction'),
        mpatches.Patch(color=COLOR_TIME,   alpha=0.82, label='Time Reduction'),
    ], fontsize=FONT_SIZE, prop={'size': FONT_SIZE},
    loc='upper left', framealpha=0.85)

    for fmt in ('png', 'pdf'):
        out = os.path.join(out_dir, f'end_to_end_comparison.{fmt}')
        plt.savefig(out, dpi=200, bbox_inches='tight')
        print(f"[Saved] {out}")
    plt.close()


# ── 主程序 ────────────────────────────────────────────────────────────────────
if __name__ == '__main__':
    print("=== End-to-End Performance Plot (Real Data Only) ===")
    print(f"CSV dir: {CSV_LOG_DIR}\n")
    real_data = load_real_data(CSV_LOG_DIR)

    print(f"\n{len(real_data)} task(s) with real data: {list(real_data.keys())}")
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    plot_end_to_end(real_data, TASKS, OUTPUT_DIR)
    print("\nDone.")
