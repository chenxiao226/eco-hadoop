import re
import os
from typing import List
import pandas as pd
from tqdm import tqdm
from datetime import datetime, timedelta

CPU_SLICES: List[float] = [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 200.0]
SDA_SLICES: List[float] = [0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 7.0, 9.0, 15.0, 200.0]

# ── 任务名固定为 pi，本次只处理 slave1 ──────────────────────────────────────
TASK_NAME = 'grep'
FILE_NAME_LIST = ['slave1']

# 读取本地CSV文件（与 preprocess_cpuio.py 保持一致）
file_path = "init-hadoop.csv"
Init_hadoop = pd.read_csv(file_path, sep=",")
print("列标签:", Init_hadoop.columns.tolist())


# ── 核心差异：从 frelog.log 构建 {node: [(time_int, freq), ...]} ─────────────
def load_freq_from_frelog(task_name):
    """
    解析 frelog.log，按节点名称建立有序的 (时间戳int, 频率int) 列表。
    frelog.log 格式示例：
        *******SET 800MHz*******
        2026年03月12日 11时01分58秒 SET slave1 cpufreq To 800MHz
    返回：
        { 'slave1': [(20260312110158, 800), ...],
          'slave2': [...], ... }
    """
    frelog_path = '../original_data/' + task_name + '/frelog.log'
    node_freq_timeline = {}

    with open(frelog_path, 'r', encoding='utf-8') as f:
        for line in f:
            node_match = re.search(
                r'(\d{4})年(\d{2})月(\d{2})日\s+(\d{2})时(\d{2})分(\d{2})秒'
                r'\s+SET\s+(\w+)\s+cpufreq\s+To\s+(\d+)MHz',
                line
            )
            if node_match:
                ts = int(
                    f"{node_match.group(1)}{node_match.group(2)}{node_match.group(3)}"
                    f"{node_match.group(4)}{node_match.group(5)}{node_match.group(6)}"
                )
                freq = int(node_match.group(8))
                node_name = node_match.group(7)
                node_freq_timeline.setdefault(node_name, []).append((ts, freq))

    return node_freq_timeline


def get_freq_at_time(ts_int, freq_timeline):
    """
    在有序的 [(ts, freq), ...] 列表中找到 <= ts_int 的最近一次频率设置。
    若时间戳早于所有设置记录则返回 -1。
    """
    freq = -1
    for set_ts, set_freq in freq_timeline:
        if set_ts <= ts_int:
            freq = set_freq
        else:
            break
    return freq


# ── process_time：与 preprocess_cpuio.py 完全一致 ────────────────────────────
def process_time(task_name):
    with open('../original_data/' + task_name + '/time.txt', 'r', encoding='utf-8') as f:
        lines = f.readlines()
        match = re.search(
            r'(\d{4})年\s*(\d{2})月\s*(\d{2})日\s*(?:星期\w+\s*)?(\d{2})时(\d{2})分(\d{2})秒',
            lines[0]
        )
        TASK_START_TIME = int(
            f"{match.group(1)}{match.group(2)}{match.group(3)}"
            f"{match.group(4)}{match.group(5)}{match.group(6)}"
        ) if match else -1

        match = re.search(
            r'(\d{4})年\s*(\d{2})月\s*(\d{2})日\s*(?:星期\w+\s*)?(\d{2})时(\d{2})分(\d{2})秒',
            lines[-1]
        )
        TASK_END_TIME = int(
            f"{match.group(1)}{match.group(2)}{match.group(3)}"
            f"{match.group(4)}{match.group(5)}{match.group(6)}"
        ) if match else -1

    return TASK_START_TIME, TASK_END_TIME


# ── preprocess_log：与 preprocess_cpuio.py 逻辑相同，但频率来自 frelog ────────
def preprocess_log(task_name, file_name, TASK_END_TIME, Hadoop_df, freq_timeline):
    preline = ''
    time = ''
    cpu_idle = ''
    sda_idle = ''
    data_list = []

    print("读取", file_name + "log.log...")
    log_path = '../original_data/' + task_name + '/system_log/' + file_name + '/log.log'
    with open(log_path, 'r', encoding='utf-8') as f:
        total_lines = len(f.readlines())
        f.seek(0)
        for i, line in enumerate(tqdm(f, total=total_lines, desc="1." + file_name + " log.log 预处理进度")):
            # 时间戳行
            if '秒' in line:
                match = re.search(
                    r'(\d{4})年(\d{2})月(\d{2})日\s+(\d{2})时(\d{2})分(\d{2})秒', line
                )
                if match:
                    year, month, day, hour, minute, second = map(int, match.groups())
                    time = int(f"{year:04}{month:02}{day:02}{hour:02}{minute:02}{second:02}")

            # CPU idle 行
            if 'idle' in preline:
                values = line.split()
                if len(values) >= 6:
                    cpu_idle = round(float(values[5]) / 100, 4)

            # sda 行 → 触发数据写入
            if line.startswith('sda'):
                values = line.split()
                try:
                    sda_idle = round(float(values[-1]) / 100, 4)
                except (ValueError, IndexError):
                    preline = line
                    continue

                if time != '' and time < TASK_END_TIME:
                    # 从 frelog 时间线查找当前频率
                    cpu_freq = get_freq_at_time(time, freq_timeline)
                    if cpu_freq != -1:
                        hadoop_idx = Hadoop_df[
                            (Hadoop_df["start_time"] <= time) & (Hadoop_df["end_time"] >= time)
                        ]
                        if not hadoop_idx.empty:
                            row_index = hadoop_idx.index[0]
                            data_list.append({
                                "time": time,
                                "cpu_freq": cpu_freq,
                                "hadoop_idx": row_index,
                                "cpu_idle": cpu_idle,
                                "sda_idle": sda_idle
                            })

            preline = line

    cpufreq_cpu_sda_df = pd.DataFrame(data_list)
    save_path = './preprocessed_data/' + task_name + '/' + file_name
    if not os.path.exists(save_path):
        os.makedirs(save_path)
    cpufreq_cpu_sda_df.to_csv(save_path + "/cpufreq_cpu_sda.csv", index=False, sep=",")
    return cpufreq_cpu_sda_df


# ── preprocess_hadoop：pi 无 hibench.txt，改用 time.txt 相邻时间戳划分任务区间 ──
def preprocess_hadoop(task_name, file_name):
    # 从 time.txt 解析所有时间戳
    timestamps = []
    with open('../original_data/' + task_name + '/time.txt', 'r', encoding='utf-8') as f:
        for line in f:
            match = re.search(
                r'(\d{4})年\s*(\d{2})月\s*(\d{2})日\s*(?:星期\w+\s*)?(\d{2})时(\d{2})分(\d{2})秒',
                line
            )
            if match:
                ts_str = (
                    f"{match.group(1)}{match.group(2)}{match.group(3)}"
                    f"{match.group(4)}{match.group(5)}{match.group(6)}"
                )
                timestamps.append(int(ts_str))

    # 相邻两个时间戳构成一次任务区间
    start_times, end_times, durations = [], [], []
    for i in range(len(timestamps) - 1):
        start_ts = timestamps[i]
        end_ts   = timestamps[i + 1]
        start_dt = datetime.strptime(str(start_ts), "%Y%m%d%H%M%S")
        end_dt   = datetime.strptime(str(end_ts),   "%Y%m%d%H%M%S")
        duration_seconds = int((end_dt - start_dt).total_seconds())
        start_times.append(start_ts)
        end_times.append(end_ts)
        durations.append(duration_seconds)

    Hadoop_df = pd.DataFrame({"start_time": start_times, "end_time": end_times, "duration": durations})

    # pi 的任务数（499个区间）可能与 init-hadoop.csv 行数不同，截取匹配行数
    n = len(durations)
    Init_hadoop_pi = Init_hadoop.iloc[:n].copy().reset_index(drop=True)
    Init_hadoop_pi['run_time'] = durations

    save_path = './preprocessed_data/' + task_name + '/' + file_name
    if not os.path.exists(save_path):
        os.makedirs(save_path)
    Init_hadoop_pi.to_csv(save_path + "/Init_hadoop_runtime.csv", index=False, sep=",")
    return Hadoop_df, Init_hadoop_pi


# ── Calculate_Hadoop_Statistics：与 preprocess_cpuio.py 完全一致 ──────────────
def Calculate_Hadoop_Statistics(Init_hadoop_runtime_df, cpufreq_cpu_sda_df, CPU_SLICES, SDA_SLICES, file_name, task_name):
    new_columns = [f"run_{i}" for i in range(1, 91)]
    for col in new_columns:
        Init_hadoop_runtime_df[col] = 0

    def find_range(value, slices):
        for i in range(len(slices) - 1):
            if slices[i] <= value < slices[i + 1]:
                return i
        return None

    pre_hadoop_idx, run_list = -1, [0] * 90
    for index, row in tqdm(
        cpufreq_cpu_sda_df.iterrows(),
        total=len(cpufreq_cpu_sda_df),
        desc="2." + file_name + " 统计"
    ):
        now_hadoop_idx = row["hadoop_idx"]
        run_idx = (
            find_range(row["cpu_idle"] * 100, CPU_SLICES)
            + find_range(row["sda_idle"] * 100, SDA_SLICES) * 10
        )
        run_list[run_idx] += 1
        if pre_hadoop_idx != now_hadoop_idx:
            column_range = Init_hadoop_runtime_df.columns[10:100]
            Init_hadoop_runtime_df.loc[pre_hadoop_idx, column_range] = run_list
            run_list = [0] * 90
        pre_hadoop_idx = now_hadoop_idx

    save_path = './preprocessed_data/' + task_name + '/' + file_name
    Init_hadoop_runtime_df.to_csv(save_path + "/Init_hadoop_runtime_run0_90.csv", index=False, sep=",")


# ── 主入口 ───────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    # 从 frelog.log 加载各节点频率时间线（一次性解析）
    node_freq_timeline = load_freq_from_frelog(TASK_NAME)
    print("已加载 frelog 频率时间线：", {k: v for k, v in node_freq_timeline.items()})

    # frelog 中只记录了 slave 节点；master 与 slave1 频率设置同步，复用其时间线
    slave1_tl = node_freq_timeline.get('slave1', [])
    for file_name in FILE_NAME_LIST:
        Hadoop_df, Init_hadoop_runtime_df = preprocess_hadoop(TASK_NAME, file_name)
        TASK_START_TIME, TASK_END_TIME = process_time(TASK_NAME)
        freq_tl = node_freq_timeline.get(file_name, slave1_tl)
        cpufreq_cpu_sda_df = preprocess_log(TASK_NAME, file_name, TASK_END_TIME, Hadoop_df, freq_tl)
        Calculate_Hadoop_Statistics(Init_hadoop_runtime_df, cpufreq_cpu_sda_df, CPU_SLICES, SDA_SLICES, file_name, TASK_NAME)
