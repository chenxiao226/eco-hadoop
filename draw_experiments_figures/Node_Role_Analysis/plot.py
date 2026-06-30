# -*- coding: utf-8 -*-
"""
Node Role Analysis — master vs slave1 x 6 Tasks
展示 Node 角色（计算节点 vs 调度节点）对优化效果的影响。
数据全部来自 param_search log.txt 中的 [Result] 行。
"""

import os
import re
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch

SCRIPT_DIR  = os.path.dirname(os.path.abspath(__file__))
ROOT_DIR    = os.path.dirname(os.path.dirname(SCRIPT_DIR))
PS_DIR      = os.path.join(ROOT_DIR, 'byh', 'param_search', 'output')
CSV_LOG_DIR = os.path.join(ROOT_DIR, 'byh', 'output')

# ── 任务定义（展示顺序）────────────────────────────────────────────────────────
# task_key: 与 param_search/output/ 下目录名一致
# display:  图中显示名
# compute_master: master 是否为计算节点
TASKS = [
    dict(key='pagerank', display='PageRank',   compute_master=True),
    dict(key='sort',     display='Sort',        compute_master=True),
    dict(key='pi',       display='PiEst.',      compute_master=False),
    dict(key='grep',     display='Grep',        compute_master=False),
    dict(key='nnbench',  display='NNBench',     compute_master=False),
    dict(key='terasort', display='TeraSort',    compute_master=False),
]

# ── 颜色 ─────────────────────────────────────────────────────────────────────
COLOR_MASTER_COMPUTE = '#C0392B'   # 红：计算节点 master — 有效果
COLOR_MASTER_SCHED   = '#E8A598'   # 浅红：调度节点 master — 无/小效果
COLOR_SLAVE          = '#2471A3'   # 蓝：slave1

HATCH_NA = '///'   # N/A 格子斜线


# ── 从 param_search log.txt 提取最终 Improvement% ───────────────────────────
def extract_improvement_from_log(log_path):
    """从 param_search log.txt 取最后一个 [Result] 行的 Improvement 值。"""
    if not os.path.isfile(log_path):
        return None
    pat = re.compile(r'Improvement:\s*([\d.]+)%')
    last = None
    with open(log_path, 'r', encoding='utf-8', errors='replace') as f:
        for line in f:
            m = pat.search(line)
            if m:
                last = float(m.group(1))
    return last


# ── 从 byh/output CSV 提取 slave1 energy improvement（兜底） ─────────────────
def extract_slave1_from_csv(task_key, csv_log_dir):
    """
    读取 byh/output/{task}_*.csv，取最早的文件，
    返回 (init_energy - opt_energy) / init_energy * 100。
    """
    import glob, pandas as pd
    pattern = os.path.join(csv_log_dir, f'{task_key}_*.csv')
    # 大小写不敏感匹配（terasort / TeraSort）
    files = sorted(glob.glob(pattern, recursive=False))
    if not files:
        # 尝试首字母大写
        pattern2 = os.path.join(csv_log_dir, f'{task_key[0].upper()}{task_key[1:]}_*.csv')
        files = sorted(glob.glob(pattern2))
    if not files:
        return None
    try:
        df = pd.read_csv(files[0], dtype=str)
        first = df.iloc[0]
        init_e = float(first['Best_Energy_Avg'])
        epoch_rows = df.iloc[1:][df.iloc[1:]['Task_Name'].apply(
            lambda x: str(x).strip().lstrip('-').isdigit()
        )]
        if epoch_rows.empty:
            return None
        opt_e = float(epoch_rows.iloc[-1]['Lambda_Perf'])
        if opt_e <= 0 or opt_e > init_e * 1.5:
            return None
        return (init_e - opt_e) / init_e * 100
    except Exception:
        return None


# ── 收集数据 ──────────────────────────────────────────────────────────────────
def collect_data():
    data = {}
    for t in TASKS:
        key = t['key']
        master_log = os.path.join(PS_DIR, key, 'master', 'log.txt')
        slave_log  = os.path.join(PS_DIR, key, 'slave1', 'log.txt')

        master_val = extract_improvement_from_log(master_log)
        slave_val  = extract_improvement_from_log(slave_log)

        # slave1 兜底：读 byh/output CSV
        if slave_val is None:
            slave_val = extract_slave1_from_csv(key, CSV_LOG_DIR)

        data[key] = {'master': master_val, 'slave1': slave_val}
        print(f"  {key:12s}  master={data[key]['master']}  slave1={data[key]['slave1']}")
    return data


# ── 绘图 ─────────────────────────────────────────────────────────────────────
def plot(data):
    n   = len(TASKS)
    x   = np.arange(n)
    w   = 0.32        # 柱宽
    gap = 0.04        # 两柱间距

    fig, ax = plt.subplots(figsize=(12, 5.5))

    for i, t in enumerate(TASKS):
        key    = t['key']
        is_cmp = t['compute_master']
        m_val  = data[key]['master']
        s_val  = data[key]['slave1']

        # ── master 柱 ────────────────────────────────────────────
        master_color = COLOR_MASTER_COMPUTE if is_cmp else COLOR_MASTER_SCHED
        xm = x[i] - w/2 - gap/2
        val_show = m_val if m_val is not None else 0.0
        if val_show > 0:
            ax.bar(xm, val_show, w, color=master_color,
                   edgecolor='white', linewidth=0.6, zorder=3)
            ax.text(xm, val_show + 0.4, f'{val_show:.1f}%',
                    ha='center', va='bottom', fontsize=9.5, color='#222')
        else:
            # 0%：画空心斜线柱，高度占位 1.0 以便标注可见
            ax.bar(xm, 1.0, w,
                   color='#f5f5f5', edgecolor=master_color,
                   linewidth=1.2, hatch=HATCH_NA, zorder=3)
            ax.text(xm, 1.0 + 0.4, '0%',
                    ha='center', va='bottom', fontsize=9.5,
                    color=master_color, fontstyle='italic')

        # ── slave1 柱 ────────────────────────────────────────────
        xs = x[i] + w/2 + gap/2
        if s_val is not None:
            ax.bar(xs, s_val, w, color=COLOR_SLAVE,
                   edgecolor='white', linewidth=0.6, zorder=3)
            ax.text(xs, s_val + 0.4, f'{s_val:.1f}%',
                    ha='center', va='bottom', fontsize=9.5, color='#222')

        # ── 计算节点 master 背景高亮 ─────────────────────────────
        if is_cmp:
            ax.axvspan(x[i] - 0.5, x[i] + 0.5,
                       alpha=0.06, color='#C0392B', zorder=0)

    # ── 分隔线：计算节点 vs 调度节点 ─────────────────────────────────────────
    ax.axvline(x=1.5, color='#888', linewidth=1.2,
               linestyle='--', alpha=0.7, zorder=4)
    ymax = ax.get_ylim()[1]
    ax.text(0.5, ymax * 0.97, 'Compute Master',
            ha='center', va='top',
            fontsize=9, color='#C0392B', fontstyle='italic')
    ax.text(3.5, ymax * 0.97, 'Scheduler-only Master',
            ha='center', va='top',
            fontsize=9, color='#999', fontstyle='italic')

    # ── 坐标轴 ────────────────────────────────────────────────────────────────
    ax.set_xticks(x)
    ax.set_xticklabels([t['display'] for t in TASKS], fontsize=11)
    ax.set_ylabel('Energy Reduction (%)', fontsize=11)
    ax.set_ylim(bottom=0)
    ax.yaxis.grid(True, linestyle='--', alpha=0.35, zorder=0)
    ax.set_axisbelow(True)
    ax.spines[['top', 'right']].set_visible(False)

    # ── 图例 ──────────────────────────────────────────────────────────────────
    legend_handles = [
        mpatches.Patch(color=COLOR_MASTER_COMPUTE, label='Master (Compute Node)'),
        mpatches.Patch(color=COLOR_MASTER_SCHED,   label='Master (Scheduler Only)'),
        mpatches.Patch(color=COLOR_SLAVE,          label='Slave (Worker Node)'),
    ]
    ax.legend(handles=legend_handles, fontsize=10,
              loc='upper right', framealpha=0.88)

    fig.tight_layout()

    for fmt in ('png', 'pdf'):
        out = os.path.join(SCRIPT_DIR, f'node_role_analysis.{fmt}')
        plt.savefig(out, dpi=200, bbox_inches='tight')
        print(f'[Saved] {out}')
    plt.close()


# ── 主程序 ────────────────────────────────────────────────────────────────────
if __name__ == '__main__':
    print('=== Node Role Analysis ===')
    print(f'param_search dir: {PS_DIR}\n')
    data = collect_data()
    plot(data)
    print('\nDone.')
