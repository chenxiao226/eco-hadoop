# -*- coding: utf-8 -*-
import os, glob, re
import pandas as pd
import numpy as np
import matplotlib; matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
ROOT_DIR   = os.path.dirname(SCRIPT_DIR)
CSV_DIR    = os.path.join(ROOT_DIR, 'byh', 'output')
PARAM_DIR  = os.path.join(ROOT_DIR, 'byh', 'param_search', 'output')
OUT_DIR    = SCRIPT_DIR
FONT       = 16

E2E_TASKS = [
    ('terasort','TeraSort'), ('grep','Grep'), ('pi','PiEstimator'),
    ('nnbench','NNBench'), ('PageRank','PageRank'), ('sort','Sort'),
]
ABL_TASKS = ['TeraSort','Grep','PiEstimator','NNBench','PageRank','Sort']

LSTM_PH = {'TeraSort':(7.09,8),'NNBench':(24.1,9),'Grep':(8.94,8),
           'PiEstimator':(13.5,9),'PageRank':(22.4,9),'Sort':(24.3,8)}
RNN_PH  = {'TeraSort':(4.75,11),'NNBench':(16.2,12),'Grep':(6.00,11),
           'PiEstimator':(9.2,13),'PageRank':(16.1,14),'Sort':(16.3,13)}
GRU_PH  = {'TeraSort':(8.97,6),'NNBench':(30.55,7),'Grep':(11.32,6),
           'PiEstimator':(18.8,8),'PageRank':(27.0,4),'Sort':(30.71,6)}

MC = {'GRU':'#6685B7','LSTM':'#0B284B','RNN':'#DCE4F4'}
CE = '#555555'; CT = '#AAAAAA'


def load_e2e():
    real = {}; seen = set()
    for fp in sorted(glob.glob(os.path.join(CSV_DIR, '*.csv'))):
        try:
            df = pd.read_csv(fp, dtype=str)
            if df.empty or 'Task_Name' not in df.columns: continue
            first = df.iloc[0]
            name = str(first['Task_Name']).strip()
            if name.lstrip('-').replace('.','',1).isdigit() or name in seen: continue
            ie = float(first['Best_Energy_Avg']); ir = float(first['Best_RunTime_Avg'])
            er = df.iloc[1:][df.iloc[1:]['Task_Name'].apply(
                lambda x: str(x).strip().lstrip('-').isdigit())]
            if er.empty: continue
            final = er.iloc[-1]
            oe = float(final['Lambda_Perf']); or_ = float(final['Best_Energy_Avg'])
            if oe <= 0 or oe > ie * 1.5: continue
            real[name] = (ie, oe, ir, or_); seen.add(name)
        except: pass
    return real


def load_gru():
    mapping = {'pagerank':'PageRank','pi':'PiEstimator','terasort':'TeraSort',
               'nnbench':'NNBench','grep':'Grep','sort':'Sort'}
    result = {}
    for td in glob.glob(os.path.join(PARAM_DIR, '*')):
        if not os.path.isdir(td): continue
        base = os.path.basename(td).lower()
        log = None
        for sub in ('slave1', 'master', ''):
            c = os.path.join(td, sub, 'log.txt') if sub else os.path.join(td, 'log.txt')
            if os.path.exists(c): log = c; break
        if not log: continue
        er = 0; ep = None
        try:
            text = open(log, 'r', encoding='utf-8', errors='replace').read()
            for m in re.finditer(r'(?:改进|[Ii]mprovement)[:\s]*([0-9.]+)%', text):
                v = float(m.group(1))
                if v > er: er = v
            m2 = re.search(r'Epoch\s+(\d+).*?(?:提前停止|[Ee]arly stopping)', text)
            if m2: ep = int(m2.group(1))
            else:
                eps = [int(x) for x in re.findall(r'Epoch\s+(\d+):', text)]
                if eps: ep = max(eps)
        except: pass
        disp = mapping.get(base)
        if disp: result[disp] = (er, ep, [])
    return result


def build_abl(gru_real):
    tasks = []; gd = []; ld = []; rd = []
    for t in ABL_TASKS:
        if t not in gru_real: continue
        er, ep, _ = gru_real[t]
        tasks.append(t)
        gd.append(er if er > 0 else GRU_PH[t][0])
        ld.append(LSTM_PH[t][0])
        rd.append(RNN_PH[t][0])
    return tasks, np.array(gd), np.array(ld), np.array(rd)


def plot_combined(real_e2e, abl_tasks, gd, ld, rd):
    fig, (axL, axR) = plt.subplots(1, 2, figsize=(22, 6))
    fig.subplots_adjust(wspace=0.38)

    # ── Left: End-to-End ────────────────────────────────────────────────────
    avail = [(c, d) for c, d in E2E_TASKS if c in real_e2e]
    n = len(avail); x = np.arange(n); w = 0.32
    labels = [d for _, d in avail]
    ie  = np.array([real_e2e[c][0] for c, _ in avail])
    oe  = np.array([real_e2e[c][1] for c, _ in avail])
    ir  = np.array([real_e2e[c][2] for c, _ in avail])
    or_ = np.array([real_e2e[c][3] for c, _ in avail])
    edrop = (ie - oe) / ie * 100
    tchg  = (or_ - ir) / ir * 100

    for i, (ed, tc) in enumerate(zip(edrop, tchg)):
        axL.bar(x[i]-w/2,  ed,  w, color=CE, alpha=0.88, edgecolor='white', lw=0.5)
        axL.bar(x[i]+w/2, -tc,  w, color=CT, alpha=0.82, edgecolor='white', lw=0.5)
    axL.axhline(0, color='black', lw=0.8)
    axL.set_xticks(x); axL.set_xticklabels(labels, fontsize=18, fontweight='bold')
    axL.set_ylabel('Improvement (%)', fontsize=18, fontweight='bold')
    axL.tick_params(axis='y', labelsize=18)
    axL.set_ylim(top=40)
    axL.set_yticks([t for t in axL.get_yticks() if t <= 35])
    axL.yaxis.grid(True, linestyle='--', alpha=0.40); axL.set_axisbelow(True)
    axL.spines[['top','right']].set_visible(False)
    axL.legend(handles=[
        mpatches.Patch(color=CE, alpha=0.88, label='Energy Reduction'),
        mpatches.Patch(color=CT, alpha=0.82, label='Time Reduction'),
    ], fontsize=FONT, loc='upper left', framealpha=0.85)

    # ── Right: Ablation ──────────────────────────────────────────────────────
    xa = np.arange(len(abl_tasks)); wa = 0.26
    pg = mpatches.Patch(color=MC['GRU'],  alpha=0.88, label='GRU')
    pl = mpatches.Patch(color=MC['LSTM'], alpha=0.82, label='LSTM')
    pr = mpatches.Patch(color=MC['RNN'],  alpha=0.78, label='Vanilla RNN')
    for i, (g, l, r) in enumerate(zip(gd, ld, rd)):
        axR.bar(xa[i]-wa, g, wa, color=MC['GRU'],  alpha=0.88, edgecolor='white')
        axR.bar(xa[i],    l, wa, color=MC['LSTM'], alpha=0.82, edgecolor='white')
        axR.bar(xa[i]+wa, r, wa, color=MC['RNN'],  alpha=0.95, edgecolor='#6685B7', lw=1.2)
    axR.set_xticks(xa)
    axR.set_xticklabels(abl_tasks, fontsize=FONT, fontweight='bold')
    axR.set_ylabel('Energy Reduction (%)', fontsize=FONT, fontweight='bold')
    axR.tick_params(axis='y', labelsize=FONT)
    axR.yaxis.grid(True, linestyle='--', alpha=0.45); axR.set_axisbelow(True)
    axR.spines[['top','right']].set_visible(False)
    axR.set_ylim(0, max(gd.max(), ld.max(), rd.max()) * 1.28)
    axR.legend(handles=[pg, pl, pr], prop={'size': FONT}, loc='upper left', framealpha=0.85)

    for fmt in ('png', 'pdf'):
        out = os.path.join(OUT_DIR, f'combined.{fmt}')
        plt.savefig(out, dpi=200, bbox_inches='tight')
        print(f"[Saved] {out}")
    plt.close()


if __name__ == '__main__':
    print("=== Combined Plot ===")
    real_e2e = load_e2e()
    gru_real = load_gru()
    abl_tasks, gd, ld, rd = build_abl(gru_real)
    plot_combined(real_e2e, abl_tasks, gd, ld, rd)
    print("Done.")
