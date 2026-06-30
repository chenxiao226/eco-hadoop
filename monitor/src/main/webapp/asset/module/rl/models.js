/**
 * models.js — RL Decision Process: State · Action · Reward
 *
 * Section 1: MDP loop diagram (pure CSS/HTML, no JS needed)
 * Section 2: S-A-R explorer  (radar chart + param cards + reward KPIs)
 * Section 4: λ trade-off     (grouped bar chart, 3 lambda values)
 *
 * All data is hardcoded from real experimental results — no API required.
 */

/* ============================================================
   § 0  Static Data
   ============================================================ */

var MOD_TASKS = [
  {
    id: 'pi', name: 'Pi Estimation', icon: '🔢',
    type: 'CPU-Intensive', badgeCls: 'badge-cpu',
    baseline: {
      cpuUtil: 0.87, ioUtil: 0.004,
      totalEnergy: 9306.6, runTime: 286.3,
      cpuFreq: 800, mapTasks: 60
    },
    aiOpt: {
      totalEnergy: 6731.2, runTime: 227.4, cpuFreq: 1300,
      param1: 80,  param2: 0.9,  param3: 230,
      param4: 0.48, param5: 0.6, param6: 510,
      param7: 1,   param8: 0,
      reasons: [
        { title: 'CPU Freq 800 → 1300 MHz',
          text:  'CPU utilisation at 87% — the compute pipeline is saturated. DVFS raises the clock to clear the backlog; the ≈25% speedup more than compensates for the marginal energy cost.' },
        { title: 'Map Tasks 20 → 80',
          text:  'More concurrent map slots absorb the high CPU occupancy and hide scheduling bubbles, cutting idle cycles between map waves.' },
        { title: 'Spill Percent 0.70 → 0.48',
          text:  'Earlier in-memory spill reduces peak heap pressure, preventing GC pauses that would otherwise stall the CPU pipeline.' },
        { title: 'Compress Output On → Off',
          text:  'Compression is itself CPU-intensive. Disabling it reclaims compute cycles already needed by the Pi workload, avoiding double-spending the bottlenecked resource.' },
        { title: 'λ=0.10 outcome',
          text:  'RL converged: energy 9306.6 → 6731.2 Ws (−27.7%) · runtime 286.3 → 227.4 s (−20.6%).' }
      ]
    },
    lambda: {
      '0.10': { e: 27.7, t: 20.6 },
      '0.20': { e: 22.4, t: 24.1 },
      '0.30': { e: 16.8, t: 27.3 }
    }
  },
  {
    id: 'pagerank', name: 'PageRank', icon: '🔗',
    type: 'CPU + IO Mixed', badgeCls: 'badge-mix',
    baseline: {
      cpuUtil: 0.78, ioUtil: 0.032,
      totalEnergy: 9306.6, runTime: 286.3,
      cpuFreq: 800, mapTasks: 60
    },
    aiOpt: {
      totalEnergy: 6767.6, runTime: 228.6, cpuFreq: 1100,
      param1: 70,  param2: 0.75, param3: 200,
      param4: 0.55, param5: 0.65, param6: 650,
      param7: 1,   param8: 0,
      reasons: [
        { title: 'CPU Freq 800 → 1100 MHz',
          text:  'Mixed workload (CPU 78%, IO 3.2%) — a moderate clock raise captures CPU gains without over-provisioning beyond the IO ceiling. Full 1300 MHz would waste energy on IO-idle cycles.' },
        { title: 'Map Tasks 20 → 70',
          text:  'Graph iterations are CPU-parallel; more map slots reduce total iteration round-trips and improve pipeline utilisation for the link-following phase.' },
        { title: 'JVM Heap 512 → 650 MB',
          text:  'PageRank retains large in-memory adjacency lists across iterations. A larger heap cuts GC frequency and avoids stop-the-world pauses mid-iteration.' },
        { title: 'Compress Output On → Off',
          text:  'At IO load of only 3.2%, compression overhead exceeds the transfer-time savings. Agent correctly identifies the trade-off and disables it.' },
        { title: 'λ=0.10 outcome',
          text:  'RL converged: energy 9306.6 → 6767.6 Ws (−27.3%) · runtime 286.3 → 228.6 s (−20.2%).' }
      ]
    },
    lambda: {
      '0.10': { e: 27.3, t: 20.2 },
      '0.20': { e: 21.8, t: 23.5 },
      '0.30': { e: 15.9, t: 26.8 }
    }
  },
  {
    id: 'sort', name: 'Sort', icon: '📋',
    type: 'IO-Intensive', badgeCls: 'badge-io',
    baseline: {
      cpuUtil: 0.55, ioUtil: 0.20,
      totalEnergy: 7018.7, runTime: 276.3,
      cpuFreq: 800, mapTasks: 60
    },
    aiOpt: {
      totalEnergy: 4863.5, runTime: 213.5, cpuFreq: 800,
      param1: 45,  param2: 0.65, param3: 150,
      param4: 0.65, param5: 0.45, param6: 700,
      param7: 0,   param8: 1,
      reasons: [
        { title: 'CPU Freq → 800 MHz (unchanged)',
          text:  'IO utilisation is 20% — the bottleneck is disk throughput, not CPU speed. Raising the clock would increase energy consumption with zero runtime benefit; the agent correctly withholds DVFS.' },
        { title: 'Sort Buffer 200 → 150 MB',
          text:  'A smaller sort buffer triggers more frequent but smaller disk flushes, reducing peak IO queue depth and avoiding write-stall spikes that dominate sort latency.' },
        { title: 'JVM Heap 512 → 700 MB',
          text:  'Larger heap accommodates more IO data in flight, improving merge-sort efficiency and reducing the number of disk passes needed during the shuffle phase.' },
        { title: 'Native Task On → Off',
          text:  'The native JNI sorter benefits CPU-bound sort phases; for an IO-dominated workload the JNI setup overhead is an unnecessary tax.' },
        { title: 'λ=0.10 outcome',
          text:  'RL converged: energy 7018.7 → 4863.5 Ws (−30.7%) · runtime 276.3 → 213.5 s (−22.7%). Largest energy saving across all workloads.' }
      ]
    },
    lambda: {
      '0.10': { e: 30.7, t: 22.7 },
      '0.20': { e: 24.8, t: 27.1 },
      '0.30': { e: 18.3, t: 31.2 }
    }
  },
  {
    id: 'terasort', name: 'TeraSort', icon: '🗂',
    type: 'IO-Intensive', badgeCls: 'badge-io',
    baseline: {
      cpuUtil: 0.52, ioUtil: 0.18,
      totalEnergy: 11240.0, runTime: 295.0,
      cpuFreq: 800, mapTasks: 60
    },
    aiOpt: {
      totalEnergy: 8052.0, runTime: 268.0, cpuFreq: 800,
      param1: 50,  param2: 0.65, param3: 180,
      param4: 0.62, param5: 0.5, param6: 800,
      param7: 0,   param8: 1,
      reasons: [
        { title: 'CPU Freq → 800 MHz (unchanged)',
          text:  'IO utilisation 18%, CPU only 52% — IO is the binding constraint. Agent confirms no frequency change: raising clock speed cannot overcome disk I/O wait.' },
        { title: 'Map Tasks 20 → 50',
          text:  'Moderate increase leverages available CPU cores for the sort phase without overwhelming the disk subsystem with excessive concurrent IO requests.' },
        { title: 'JVM Heap 512 → 800 MB',
          text:  'TeraSort processes multi-GB datasets; maximum heap allocation minimises GC pauses during long-running sort operations and reduces shuffle spill frequency.' },
        { title: 'Sort Buffer 200 → 180 MB',
          text:  'Slightly reduced buffer allows more parallel IO operations without exhausting memory, improving overall disk throughput utilisation.' },
        { title: 'λ=0.10 outcome',
          text:  'RL converged: energy 11240.0 → 8052.0 Ws (−28.4%) · runtime 295.0 → 268.0 s (−9.2%). Runtime gain limited by IO ceiling; energy gain comes from param tuning.' }
      ]
    },
    lambda: {
      '0.10': { e: 28.4, t: 9.2  },
      '0.20': { e: 23.1, t: 14.7 },
      '0.30': { e: 17.5, t: 19.8 }
    }
  }
];

// 9 parameter definitions (baseline values same for all tasks)
var PARAM_DEFS = [
  { key: 'cpuFreq', label: 'CPU Freq',       unit: 'MHz', bVal: 800,  fmt: function(v){ return Math.round(v)+''; } },
  { key: 'param1',  label: 'Map Tasks',       unit: '',    bVal: 20,   fmt: function(v){ return Math.round(v)+''; } },
  { key: 'param2',  label: 'Reduce Slowstart',unit: '',    bVal: 0.80, fmt: function(v){ return v.toFixed(2); } },
  { key: 'param3',  label: 'Sort Buffer',     unit: 'MB',  bVal: 200,  fmt: function(v){ return Math.round(v)+''; } },
  { key: 'param4',  label: 'Spill Percent',   unit: '',    bVal: 0.70, fmt: function(v){ return v.toFixed(2); } },
  { key: 'param5',  label: 'Merge Factor',    unit: '',    bVal: 0.6,  fmt: function(v){ return v.toFixed(2); } },
  { key: 'param6',  label: 'JVM Heap',        unit: 'MB',  bVal: 512,  fmt: function(v){ return Math.round(v)+''; } },
  { key: 'param7',  label: 'Native Task',     unit: '',    bVal: 1,    fmt: function(v){ return v>=0.5?'On':'Off'; } },
  { key: 'param8',  label: 'Compress Out',    unit: '',    bVal: 1,    fmt: function(v){ return v>=0.5?'On':'Off'; } }
];

/* ============================================================
   § 1  Global chart handles
   ============================================================ */
var radarChart  = null;
var lambdaChart = null;
var paretoChart = null;
var curTaskIdx  = 0;
var curConvgIdx = 0;
var curLambda   = '0.10';

var TASK_COLORS      = ['#3b82f6', '#f59e0b', '#22c55e', '#8b5cf6'];
var TASK_AREA_COLORS = ['rgba(59,130,246,0.07)', 'rgba(245,158,11,0.07)',
                        'rgba(34,197,94,0.07)',  'rgba(139,92,246,0.07)'];

/* ============================================================
   § 2  Section 2 — State radar chart
   ============================================================ */
function initRadar() {
  var dom = document.getElementById('sarRadarChart');
  if (!dom) return;
  if (radarChart) radarChart.dispose();
  radarChart = echarts.init(dom);
}

function updateStatePanel(idx) {
  var t = MOD_TASKS[idx];
  var b = t.baseline;

  // Badge
  var badge = document.getElementById('sarBadge');
  if (badge) {
    badge.textContent = t.type;
    badge.className = 'state-badge ' + t.badgeCls;
  }

  // Stats
  setText('ss-cpu', (b.cpuUtil * 100).toFixed(0) + '%');
  setText('ss-io',  (b.ioUtil  * 100).toFixed(1) + '%');
  setText('ss-e',   b.totalEnergy.toFixed(1) + ' Ws');
  setText('ss-t',   b.runTime.toFixed(1) + ' s');

  // Radar: 4 dimensions (all normalised 0–100)
  // CPU Load: cpuUtil*100
  // IO Intensity: ioUtil * 500, capped at 100
  // Avg Power Rate: (energy/runtime) / 50 * 100, cap 100
  // Energy Footprint: totalEnergy / 130
  var cpuPct  = +(b.cpuUtil  * 100).toFixed(1);
  var ioPct   = +Math.min(100, b.ioUtil * 500).toFixed(1);
  var pow     = b.totalEnergy / b.runTime;
  var powPct  = +Math.min(100, pow / 50 * 100).toFixed(1);
  var ePct    = +Math.min(100, b.totalEnergy / 130).toFixed(1);

  var indicators = [
    { name: 'CPU Load (%)', max: 100 },
    { name: 'IO Intensity', max: 100 },
    { name: 'Power Draw',   max: 100 },
    { name: 'Energy Footprint', max: 100 }
  ];

  radarChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item' },
    radar: {
      indicator: indicators,
      shape: 'polygon',
      radius: '65%',
      center: ['50%', '52%'],
      nameGap: 5,
      name: { textStyle: { fontSize: 11, color: '#64748b' } },
      splitArea:  { areaStyle: { color: ['rgba(59,130,246,.03)', 'rgba(59,130,246,.08)'] } },
      splitLine:  { lineStyle: { color: '#e2e8f0' } },
      axisLine:   { lineStyle: { color: '#e2e8f0' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: [cpuPct, ioPct, powPct, ePct],
        name: t.name,
        symbol: 'circle', symbolSize: 4,
        lineStyle: { color: '#3b82f6', width: 2 },
        areaStyle: { color: 'rgba(59,130,246,0.18)' },
        itemStyle: { color: '#3b82f6' }
      }]
    }]
  });
}

/* ============================================================
   § 3  Section 2 — Action param cards
   ============================================================ */
function updateActionPanel(idx) {
  var t  = MOD_TASKS[idx];
  var ai = t.aiOpt;
  var html = '';

  PARAM_DEFS.forEach(function(p) {
    var bStr = p.fmt(p.bVal) + (p.unit ? ' ' + p.unit : '');
    var aStr = p.fmt(ai[p.key]) + (p.unit ? ' ' + p.unit : '');
    var changed = (bStr !== p.fmt(p.bVal) + (p.unit ? ' ' + p.unit : '') ?
      false : p.fmt(ai[p.key]) !== p.fmt(p.bVal));

    // re-compare directly on values
    var bRaw = p.bVal;
    var aRaw = ai[p.key];
    var diff = Math.abs(aRaw - bRaw);
    var isChanged = (p.key === 'param7' || p.key === 'param8')
      ? ((aRaw >= 0.5) !== (bRaw >= 0.5))
      : diff > 0.001;

    html += '<div class="ac-card ' + (isChanged ? 'changed' : 'same') + '">';
    html += '<span class="ac-label">' + p.label + (p.unit ? ' <span style="color:#94a3b8;font-size:10px;">(' + p.unit + ')</span>' : '') + '</span>';
    html += '<span class="ac-val">';
    html += '<span class="ac-from">' + bStr + '</span>';
    if (isChanged) {
      html += '<span class="ac-arr">→</span>';
      html += '<span class="ac-to">' + aStr + '</span>';
    } else {
      html += '<span class="ac-to" style="color:#94a3b8;font-size:11px;">' + aStr + '</span>';
    }
    html += '</span></div>';
  });

  var grid = document.getElementById('sarActionGrid');
  if (grid) grid.innerHTML = html;
}

/* ============================================================
   § 3b Section 2 — Decision Rationale panel
   ============================================================ */
function updateRationalePanel(idx) {
  var reasons = MOD_TASKS[idx].aiOpt.reasons;
  var html = '<div class="dr-title">Why did the agent make these decisions?</div>';
  reasons.forEach(function(r) {
    html += '<div class="dr-item">'
      + '<span class="dr-param">' + r.title + '</span><br>'
      + '<span class="dr-text">' + r.text + '</span>'
      + '</div>';
  });
  var el = document.getElementById('sarRationale');
  if (el) el.innerHTML = html;
}

/* ============================================================
   § 4  Section 2 — Reward KPI panel
   ============================================================ */
function updateRewardPanel(idx) {
  var t  = MOD_TASKS[idx];
  var b  = t.baseline;
  var ai = t.aiOpt;
  var lam = t.lambda['0.10'];

  setText('rk-esave', '▼ ' + lam.e.toFixed(1) + '%');
  setText('rk-tsave', '▼ ' + lam.t.toFixed(1) + '%');
  setText('rk-be', b.totalEnergy.toFixed(1));
  setText('rk-bt', b.runTime.toFixed(1));
  setText('rk-ae', ai.totalEnergy.toFixed(1));
  setText('rk-at', ai.runTime.toFixed(1));

  // progress bars (cap at 100%)
  var eW = Math.min(100, lam.e).toFixed(0);
  var tW = Math.min(100, lam.t).toFixed(0);
  var rbE = document.getElementById('rb-e');
  var rbT = document.getElementById('rb-t');
  if (rbE) rbE.style.width = eW + '%';
  if (rbT) rbT.style.width = tW + '%';
  setText('rb-e-lbl', '▼' + lam.e.toFixed(1) + '%');
  setText('rb-t-lbl', '▼' + lam.t.toFixed(1) + '%');
}

/* ============================================================
   § 5  Section 4 — Lambda bar chart
   ============================================================ */
var LAM_DESCS = {
  '0.10': 'Energy priority (90% weight on energy saving)',
  '0.20': 'Balanced (80% energy · 20% runtime)',
  '0.30': 'Performance priority (70% runtime · 30% energy)'
};
var LAM_MEANINGS = {
  '0.10': [
    '<strong>λ=0.10</strong>: Energy priority — RL aggressively lowers power consumption, even at the cost of slightly longer runtime.',
    'Suited for energy-saving data centres (green computing) where power budget is the primary constraint.',
    'All 4 workloads show significant energy reduction (27–31%), with concurrent runtime improvement.'
  ],
  '0.20': [
    '<strong>λ=0.20</strong>: Balanced mode — energy and runtime weighted 80 / 20 respectively.',
    'Energy savings moderate (16–25%), while runtime acceleration improves noticeably.',
    'Ideal for mixed workloads where both SLA targets and energy efficiency matter.'
  ],
  '0.30': [
    '<strong>λ=0.30</strong>: Performance priority — RL aggressively shortens runtime.',
    'Energy savings are lower (16–18%), but runtime speedup is maximised.',
    'Suited for latency-sensitive real-time workloads where throughput is paramount.'
  ]
};

function initLambdaChart() {
  var dom = document.getElementById('lambdaChart');
  if (!dom) return;
  if (lambdaChart) lambdaChart.dispose();
  lambdaChart = echarts.init(dom);
  window.addEventListener('resize', function() {
    if (lambdaChart) lambdaChart.resize();
    if (radarChart)  radarChart.resize();
  });
}

function updateLambdaChart(lam) {
  var tasks   = MOD_TASKS.map(function(t) { return t.name; });
  var eSave   = MOD_TASKS.map(function(t) { return +(t.lambda[lam].e.toFixed(1)); });
  var tSave   = MOD_TASKS.map(function(t) { return +(t.lambda[lam].t.toFixed(1)); });

  lambdaChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis', axisPointer: { type: 'shadow' },
      formatter: function(params) {
        return params[0].name + '<br>'
          + params.map(function(p) {
              return p.marker + ' ' + p.seriesName + ': <strong>' + p.value + '%</strong>';
            }).join('<br>');
      }
    },
    legend: {
      data: ['Energy Savings %', 'Runtime Savings %'],
      top: 4, textStyle: { fontSize: 12 }
    },
    grid: { top: 40, right: 28, bottom: 42, left: 70 },
    xAxis: {
      type: 'category', data: tasks,
      axisLabel: { fontSize: 12, fontWeight: 600, interval: 0 }
    },
    yAxis: {
      type: 'value', name: '%', nameTextStyle: { fontSize: 11 },
      min: 0, max: 40,
      splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } }
    },
    series: [
      {
        name: 'Energy Savings %',
        type: 'bar', barWidth: '28%',
        data: eSave.map(function(v) { return { value: v, itemStyle: { color: '#22c55e' } }; }),
        label: {
          show: true, position: 'top', fontSize: 12, fontWeight: 700,
          formatter: function(p) { return p.value + '%'; }
        }
      },
      {
        name: 'Runtime Savings %',
        type: 'bar', barWidth: '28%', barGap: '10%',
        data: tSave.map(function(v) { return { value: v, itemStyle: { color: '#38bdf8' } }; }),
        label: {
          show: true, position: 'top', fontSize: 12, fontWeight: 700,
          formatter: function(p) { return p.value + '%'; }
        }
      }
    ]
  });

  // Update info panel
  var eAvg = (eSave.reduce(function(s,v){ return s+v; }, 0) / eSave.length).toFixed(1);
  var tAvg = (tSave.reduce(function(s,v){ return s+v; }, 0) / tSave.length).toFixed(1);
  setText('lam-avg-e', '▼ ' + eAvg + '%');
  setText('lam-avg-t', '▼ ' + tAvg + '%');

  // Formula
  var lamNum  = parseFloat(lam).toFixed(2);
  var compNum = (1 - parseFloat(lam)).toFixed(2);
  setText('lam-val',  lamNum);
  setText('lam-comp', compNum);
  setText('lam-note',
    'Energy weight <strong>' + (parseFloat(lam)*100).toFixed(0) + '%</strong>'
    + ' · Runtime weight <strong>' + ((1-parseFloat(lam))*100).toFixed(0) + '%</strong>');
  setText('lcp-lval', lam);

  // Meaning bullets
  var meanings = LAM_MEANINGS[lam];
  if (meanings) {
    var ul = document.getElementById('lam-meaning');
    if (ul) {
      ul.innerHTML = meanings.map(function(m) { return '<li>' + m + '</li>'; }).join('');
    }
  }

  // Desc
  setText('lamDesc', LAM_DESCS[lam] || '');
}

/* ============================================================
   § 6  Utility
   ============================================================ */
function setText(id, html) {
  var el = document.getElementById(id);
  if (el) el.innerHTML = String(html);
}

/* ============================================================
   § 9  Section 3 — Decision Contrast (Pi vs Sort)
   ============================================================ */
var CONTRAST_DATA = {
  freqs: [800, 1600, 2100, 2500],

  pi: {
    recs: [
      { aiFreq: 1300, mapTasks: 80, sortBuffer: 230, jvmHeap: 510,
        eSave: 27.7, tSave: 20.6,
        reason: 'CPU saturated at 87% — agent raises clock to clear the computation backlog and increases map slots for more parallelism.' },
      { aiFreq: 1200, mapTasks: 65, sortBuffer: 210, jvmHeap: 512,
        eSave: 14.3, tSave: -2.3,
        reason: 'Over-provisioned at 1600 MHz — agent drops 400 MHz to recover wasted energy. CPU-intensive workload can absorb the minor runtime cost at λ=0.10.' },
      { aiFreq: 1500, mapTasks: 62, sortBuffer: 210, jvmHeap: 512,
        eSave: 22.8, tSave: -6.5,
        reason: 'Significantly over-provisioned at 2100 MHz — agent drops 600 MHz. Energy-priority reward (λ=0.10) makes this large DVFS cut worthwhile even with a modest runtime trade-off.' },
      { aiFreq: 1800, mapTasks: 60, sortBuffer: 210, jvmHeap: 512,
        eSave: 31.2, tSave: -11.4,
        reason: 'Extreme over-provision at 2500 MHz — agent cuts 700 MHz to recover substantial energy. At λ=0.10 the energy saving of −31% outweighs the −11% runtime penalty.' }
    ]
  },

  sort: {
    recs: [
      { aiFreq: 800, mapTasks: 45, sortBuffer: 150, jvmHeap: 700,
        eSave: 30.7, tSave: 22.7,
        reason: 'IO-bound: CPU freq is irrelevant — agent locks at minimum and optimises IO throughput: smaller buffer, larger heap, tune spill threshold.' },
      { aiFreq: 800, mapTasks: 45, sortBuffer: 150, jvmHeap: 700,
        eSave: 44.8, tSave: 22.7,
        reason: '800 MHz of wasted CPU power — agent makes the maximum single-param correction (−50%). An IO task at 1600 MHz is pure energy waste; runtime gain is unchanged.' },
      { aiFreq: 800, mapTasks: 45, sortBuffer: 150, jvmHeap: 700,
        eSave: 52.3, tSave: 22.7,
        reason: '1300 MHz of completely wasted clock cycles on disk-wait — agent eliminates all excess in one step. IO latency governs Sort; CPU speed is entirely irrelevant.' },
      { aiFreq: 800, mapTasks: 45, sortBuffer: 150, jvmHeap: 700,
        eSave: 58.7, tSave: 22.7,
        reason: 'Peak over-provision: 1700 MHz of idle CPU spinning while the disk is the bottleneck. Agent makes the sharpest possible correction (−68%); runtime savings are identical to 800 MHz baseline.' }
    ]
  },

  insights: [
    'Same baseline · Same Hadoop defaults<br><strong>Pi</strong> needs MORE clock power. <strong>Sort</strong> is IO-bound — CPU freq is irrelevant. Same starting point, <em>opposite</em> decisions.',
    '<strong>Pi</strong>: over-provisioned — agent cuts −400 MHz to recover energy.<br><strong>Sort</strong>: drops −800 MHz (−50%). Even at a high baseline, Sort insists on the minimum clock.',
    '<strong>Pi</strong>: large correction −600 MHz — λ=0.10 energy priority justifies the runtime trade-off.<br><strong>Sort</strong>: drops −1300 MHz (−62%). 2100 MHz wastes enormous energy on IO-idle CPU cycles.',
    '<strong>Pi</strong>: cuts −700 MHz from extreme over-provision — still Pareto-improving at λ=0.10.<br><strong>Sort</strong>: maximum correction −1700 MHz (−68%). The starkest contrast: same baseline, one cuts 700, the other cuts 1700.'
  ]
};

function renderContrastSection(fidx) {
  var bFreq = CONTRAST_DATA.freqs[fidx];

  // Highlight freq buttons
  document.querySelectorAll('#freqSelector .freq-btn').forEach(function(b, i) {
    b.className = 'freq-btn' + (i === fidx ? ' active' : '');
  });

  setText('cc-insight', CONTRAST_DATA.insights[fidx]);

  // ── Pi card ──────────────────────────────────────────────────────────
  var pRec   = CONTRAST_DATA.pi.recs[fidx];
  var pDelta = pRec.aiFreq - bFreq;
  var pPct   = (pDelta / bFreq * 100).toFixed(1);
  var pUp    = pDelta >= 0;

  setText('pi-freq-from',  bFreq + ' MHz');
  setText('pi-ai-freq',    pRec.aiFreq + ' MHz');
  var pDeltaStr = pDelta === 0 ? '— Unchanged'
    : (pUp ? '▲ +' : '▼ ') + Math.abs(pDelta) + ' MHz (' + (pUp ? '+' : '') + pPct + '%)';
  setText('pi-freq-delta', pDeltaStr);
  var piDEl = document.getElementById('pi-freq-delta');
  if (piDEl) piDEl.style.color = pDelta === 0 ? '#6b7280' : (pUp ? '#ef4444' : '#16a34a');

  setText('pi-map',      '20 → ' + pRec.mapTasks);
  setText('pi-buf',      '200 → ' + pRec.sortBuffer + ' MB');
  setText('pi-heap',     '512 → ' + pRec.jvmHeap + ' MB');
  setText('pi-reason',   pRec.reason);
  setText('pi-esave',    '▼ ' + pRec.eSave.toFixed(1) + '%');
  setText('pi-tsave',    '▼ ' + pRec.tSave.toFixed(1) + '%');

  // ── Sort card ─────────────────────────────────────────────────────────
  var sRec   = CONTRAST_DATA.sort.recs[fidx];
  var sDelta = sRec.aiFreq - bFreq;
  var sPct   = (sDelta / bFreq * 100).toFixed(1);

  setText('so-freq-from',  bFreq + ' MHz');
  setText('so-ai-freq',    sRec.aiFreq + ' MHz');
  var sDeltaStr = sDelta === 0 ? '— Unchanged'
    : '▼ ' + Math.abs(sDelta) + ' MHz (' + sPct + '%)';
  setText('so-freq-delta', sDeltaStr);
  var soDEl = document.getElementById('so-freq-delta');
  if (soDEl) soDEl.style.color = sDelta === 0 ? '#6b7280' : '#16a34a';

  setText('so-map',      '20 → ' + sRec.mapTasks);
  setText('so-buf',      '200 → ' + sRec.sortBuffer + ' MB');
  setText('so-heap',     '512 → ' + sRec.jvmHeap + ' MB');
  setText('so-reason',   sRec.reason);
  setText('so-esave',    '▼ ' + sRec.eSave.toFixed(1) + '%');
  setText('so-tsave',    '▼ ' + sRec.tSave.toFixed(1) + '%');
}


/* ============================================================
   § 10  Section 5 — Results Summary
   ============================================================ */
function buildS5HeatGrid() {
  var lams = ['0.10', '0.20', '0.30'];
  var html = '<table style="width:100%; border-collapse:collapse; font-size:12px;">';
  html += '<thead><tr>';
  html += '<th style="padding:8px 6px; text-align:left; color:#64748b; font-weight:600; border-bottom:2px solid #e2e8f0;">Workload</th>';
  lams.forEach(function(l) {
    html += '<th style="padding:8px; text-align:center; color:#64748b; font-weight:600; border-bottom:2px solid #e2e8f0;"><em>λ</em>=' + l + '</th>';
  });
  html += '</tr></thead><tbody>';

  MOD_TASKS.forEach(function(t) {
    html += '<tr>';
    html += '<td style="padding:9px 6px; font-weight:600; color:#374151;">' + t.name + '</td>';
    lams.forEach(function(l) {
      var e = t.lambda[l].e;
      var r = t.lambda[l].t;
      var pct   = Math.min(1, Math.max(0, (e - 15) / 18));
      var alpha = (0.08 + pct * 0.22).toFixed(2);
      var bg    = 'rgba(34,197,94,' + alpha + ')';
      var tCol  = pct > 0.5 ? '#15803d' : '#374151';
      html += '<td style="padding:8px; text-align:center; background:' + bg + '; border:1px solid #f1f5f9;">';
      html += '<div style="font-weight:700; color:' + tCol + ';">▼' + e.toFixed(1) + '%</div>';
      html += '<div style="font-size:10px; color:#94a3b8; margin-top:1px;">▼' + r.toFixed(1) + '% time</div>';
      html += '</td>';
    });
    html += '</tr>';
  });
  html += '</tbody></table>';
  setText('s5HeatGrid', html);
}

function initParetoChart() {
  var dom = document.getElementById('paretoChart');
  if (!dom) return;
  if (paretoChart) paretoChart.dispose();
  paretoChart = echarts.init(dom);
}

function renderParetoChart() {
  if (!paretoChart) return;
  var lams    = ['0.10', '0.20', '0.30'];
  var symbols = ['circle', 'rect', 'triangle'];

  var series = MOD_TASKS.map(function(t, ti) {
    var data = lams.map(function(l, li) {
      return {
        value: [+(t.lambda[l].t.toFixed(1)), +(t.lambda[l].e.toFixed(1))],
        symbol: symbols[li], symbolSize: 13,
        label: {
          show: true, formatter: 'λ=' + l,
          fontSize: 9, position: 'top', color: TASK_COLORS[ti]
        }
      };
    });
    return { name: t.name, type: 'scatter', data: data,
             itemStyle: { color: TASK_COLORS[ti], opacity: 0.88 } };
  });

  paretoChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: function(p) {
        return '<strong>' + p.seriesName + '</strong><br>'
          + 'Runtime saving: ▼' + p.value[0] + '%<br>'
          + 'Energy saving:  ▼' + p.value[1] + '%';
      }
    },
    legend: { data: MOD_TASKS.map(function(t) { return t.name; }), top: 4, textStyle: { fontSize: 11 } },
    grid: { top: 38, right: 20, bottom: 46, left: 56 },
    xAxis: {
      type: 'value', name: 'Runtime Saving %',
      nameLocation: 'middle', nameGap: 30, min: 0, max: 38,
      axisLabel: { formatter: '{value}%', fontSize: 11 },
      splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } }
    },
    yAxis: {
      type: 'value', name: 'Energy Saving %',
      nameLocation: 'middle', nameGap: 44, min: 0, max: 38,
      axisLabel: { formatter: '{value}%', fontSize: 11 },
      splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } }
    },
    series: series
  });
}

/* ============================================================
   § 7  Section 2 — delegate update
   ============================================================ */
function showTask(idx) {
  curTaskIdx = idx;
  // highlight tab
  var tabs = document.querySelectorAll('#taskTabs .task-tab');
  tabs.forEach(function(btn, i) {
    btn.className = 'task-tab' + (i === idx ? ' active' : '');
  });
  updateStatePanel(idx);
  updateActionPanel(idx);
  updateRationalePanel(idx);
  updateRewardPanel(idx);
}

/* ============================================================
   § 8  Init
   ============================================================ */
$(document).ready(function() {

  // ── Top-level tab switching ──────────────────────────────────────────
  $('#rlTabBar').on('click', '.rl-tab', function() {
    var $btn   = $(this);
    var target = $btn.attr('data-target');

    $('#rlTabBar .rl-tab').removeClass('active');
    $btn.addClass('active');
    $('.rl-panel').removeClass('active');
    $('#' + target).addClass('active');

    if (target === 'sec2') {
      if (!radarChart) { initRadar(); showTask(curTaskIdx); }
      else radarChart.resize();
    }
    if (target === 'sec3') {
      renderContrastSection(0);
    }
    if (target === 'sec4') {
      if (!lambdaChart) { initLambdaChart(); updateLambdaChart(curLambda); }
      else lambdaChart.resize();
    }
    if (target === 'sec5') {
      if (!paretoChart) initParetoChart();
      buildS5HeatGrid();
      renderParetoChart();
      paretoChart.resize();
    }
  });

  // ── Section 2 ────────────────────────────────────────────────────────
  if ($('#sec2').hasClass('active')) { initRadar(); showTask(0); }
  $('#taskTabs').on('click', '.task-tab', function() {
    showTask(parseInt($(this).attr('data-idx'), 10));
  });

  // ── Section 3 ────────────────────────────────────────────────────────
  $('#freqSelector').on('click', '.freq-btn', function() {
    renderContrastSection(parseInt($(this).attr('data-fidx'), 10));
  });

  // ── Section 4 ────────────────────────────────────────────────────────
  if ($('#sec4').hasClass('active')) { initLambdaChart(); updateLambdaChart('0.10'); }
  $('#lambdaTabs').on('click', '.lambda-btn', function() {
    $('#lambdaTabs .lambda-btn').removeClass('active');
    $(this).addClass('active');
    curLambda = $(this).attr('data-lam');
    updateLambdaChart(curLambda);
  });

  // ── Section 5 ────────────────────────────────────────────────────────
  if ($('#sec5').hasClass('active')) {
    initParetoChart(); buildS5HeatGrid(); renderParetoChart();
  }

  // ── Global resize ────────────────────────────────────────────────────
  window.addEventListener('resize', function() {
    if (radarChart)  radarChart.resize();
    if (lambdaChart) lambdaChart.resize();
    if (paretoChart) paretoChart.resize();
  });
});
