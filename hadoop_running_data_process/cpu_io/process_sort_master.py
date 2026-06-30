# -*- coding: utf-8 -*-
"""
Sort 任务 cpu_io 预处理脚本 —— master 节点版本
- 没有 frelog.log，频率从 system_log/master/log.log 的 SET XXXMHz 行读取
- 没有 hibench.txt，任务区间从 time.txt 相邻时间戳构建
- 只处理 master 节点
"""
import re
import os
from typing import List
import pandas as pd
from tqdm import tqdm
from datetime import datetime

CPU_SLICES: List[float] = [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 200.0]
SDA_SLICES: List[float] = [0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 7.0, 9.0, 15.0, 200.0]

TASK_NAME = 'sort'
FILE_NAME_LIST = ['master']

file_path = "init-hadoop.csv"
Init_hadoop = pd.read_csv(file_path, sep=",")
print("列标签:", Init_hadoop.columns.tolist())


def process_time(task_name):
    """从 time.txt 获取任务总起止时间（首行/末行）"""
    with open('../original_data/' + task_name + '/time.txt', 'r', encoding='utf-8') as f:
        lines = [l for l in f.readlines() if l.strip()]
    def parse_ts(line):
        match = re.search(
            r'(\d{4})年\s*(\d{2})月\s*(\d{2})日\s*(?:星期\w+\s*)?(\d{2}):(\d{2}):(\d{2})',
            line
        )
        if match:
            return int(f"{match.group(1)}{match.group(2)}{match.group(3)}"
                       f"{match.group(4)}{match.group(5)}{match.group(6)}")
        return -1
    return parse_ts(lines[0]), parse_ts(lines[-1])


def preprocess_hadoop(task_name, file_name):
    """从 time.txt 相邻时间戳构建任务区间（与 process2_terasort.py 一致）"""
    timestamps = []
    with open('../original_data/' + task_name + '/time.txt', 'r', encoding='utf-8') as f:
        for line in f:
            match = re.search(
                r'(\d{4})年\s*(\d{2})月\s*(\d{2})日\s*(?:星期\w+\s*)?(\d{2}):(\d{2}):(\d{2})',
                line
            )
            if match:
                ts_str = (
                    f"{match.group(1)}{match.group(2)}{match.group(3)}"
                    f"{match.group(4)}{match.group(5)}{match.group(6)}"
                )
                timestamps.append(int(ts_str))

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

    n = min(len(durations), len(Init_hadoop))
    Hadoop_df = Hadoop_df.iloc[:n].reset_index(drop=True)
    durations  = durations[:n]
    Init_hadoop_sort = Init_hadoop.iloc[:n].copy().reset_index(drop=True)
    Init_hadoop_sort['run_time'] = durations

    save_path = './preprocessed_data/' + task_name + '/' + file_name
    os.makedirs(save_path, exist_ok=True)
    Init_hadoop_sort.to_csv(save_path + "/Init_hadoop_runtime.csv", index=False, sep=",")
    return Hadoop_df, Init_hadoop_sort


def build_freq_timeline_from_master_log(task_name):
    """
    从 system_log/master/log.log 的 SET XXXMHz 行提取频率时间线。
    """
    log_path = '../original_data/' + task_name + '/system_log/master/log.log'
    master_CpuFreq_Time = {}
    current_freq = -1
    current_time = -1

    with open(log_path, 'r', encoding='utf-8') as f:
        for line in f:
            if 'SET' in line:
                m = re.search(r'(\d+)MHz', line)
                if m:
                    current_freq = int(m.group(1))

            if '秒' in line:
                m = re.search(r'(\d{4})年(\d{2})月(\d{2})日\s+(\d{2})时(\d{2})分(\d{2})秒', line)
                if m:
                    current_time = int(
                        f"{m.group(1)}{m.group(2)}{m.group(3)}"
                        f"{m.group(4)}{m.group(5)}{m.group(6)}"
                    )
                    if current_freq != -1:
                        master_CpuFreq_Time[current_time] = current_freq

    print(f"  master 频率时间线共 {len(master_CpuFreq_Time)} 条记录")
    return master_CpuFreq_Time


def preprocess_log(task_name, file_name, TASK_END_TIME, Hadoop_df, master_CpuFreq_Time):
    """
    解析 system_log/master/log.log：master 节点自身的 cpu/io 数据，
    频率直接从 master 日志中读取。
    """
    preline = ''
    cpu_freq = -1
    time = ''
    cpu_idle = ''
    sda_idle = ''
    data_list = []

    use_master_timeline = len(master_CpuFreq_Time) > 0

    print("读取", file_name + "log.log...")
    log_path = '../original_data/' + task_name + '/system_log/' + file_name + '/log.log'
    with open(log_path, 'r', encoding='utf-8') as f:
        total_lines = len(f.readlines())
        f.seek(0)
        for i, line in enumerate(tqdm(f, total=total_lines, desc="1." + file_name + " log.log 预处理进度")):
            if 'SET' in line:
                m = re.search(r'(\d+)MHz', line)
                if m:
                    cpu_freq = int(m.group(1))

            if '秒' in line:
                m = re.search(r'(\d{4})年(\d{2})月(\d{2})日\s+(\d{2})时(\d{2})分(\d{2})秒', line)
                if m:
                    year, month, day, hour, minute, second = map(int, m.groups())
                    time = int(f"{year:04}{month:02}{day:02}{hour:02}{minute:02}{second:02}")
                    if use_master_timeline and time in master_CpuFreq_Time:
                        cpu_freq = master_CpuFreq_Time[time]

            if 'idle' in preline:
                values = line.split()
                if len(values) >= 6:
                    cpu_idle = round(float(values[5]) / 100, 4)

            if line.startswith('sda'):
                values = line.split()
                try:
                    sda_idle = round(float(values[-1]) / 100, 4)
                except (ValueError, IndexError):
                    preline = line
                    continue

                if time != '' and time < TASK_END_TIME and cpu_freq != -1:
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
    os.makedirs(save_path, exist_ok=True)
    cpufreq_cpu_sda_df.to_csv(save_path + "/cpufreq_cpu_sda.csv", index=False, sep=",")
    print(f"  cpufreq_cpu_sda_df: {len(cpufreq_cpu_sda_df)} 行")
    print(f"  频率分布:\n{cpufreq_cpu_sda_df['cpu_freq'].value_counts().sort_index()}")
    return cpufreq_cpu_sda_df


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
        cpu_idx = find_range(row["cpu_idle"] * 100, CPU_SLICES)
        sda_idx = find_range(row["sda_idle"] * 100, SDA_SLICES)
        if cpu_idx is None or sda_idx is None:
            continue
        run_idx = cpu_idx + sda_idx * 10
        run_list[run_idx] += 1
        if pre_hadoop_idx != now_hadoop_idx:
            column_range = Init_hadoop_runtime_df.columns[10:100]
            Init_hadoop_runtime_df.loc[pre_hadoop_idx, column_range] = run_list
            run_list = [0] * 90
        pre_hadoop_idx = now_hadoop_idx

    save_path = './preprocessed_data/' + task_name + '/' + file_name
    Init_hadoop_runtime_df.to_csv(save_path + "/Init_hadoop_runtime_run0_90.csv", index=False, sep=",")
    print(f"  已保存 {save_path}/Init_hadoop_runtime_run0_90.csv")


if __name__ == "__main__":
    master_CpuFreq_Time = build_freq_timeline_from_master_log(TASK_NAME)
    TASK_START_TIME, TASK_END_TIME = process_time(TASK_NAME)
    print(f"任务时间范围: {TASK_START_TIME} ~ {TASK_END_TIME}")

    for file_name in FILE_NAME_LIST:
        print(f"\n=== 处理 {TASK_NAME} - {file_name} ===")
        Hadoop_df, Init_hadoop_runtime_df = preprocess_hadoop(TASK_NAME, file_name)
        print(f"  任务区间数: {len(Hadoop_df)}")
        cpufreq_cpu_sda_df = preprocess_log(TASK_NAME, file_name, TASK_END_TIME, Hadoop_df, master_CpuFreq_Time)

        Calculate_Hadoop_Statistics(Init_hadoop_runtime_df, cpufreq_cpu_sda_df, CPU_SLICES, SDA_SLICES, file_name, TASK_NAME)

        out_path = f'./preprocessed_data/{TASK_NAME}/{file_name}/Init_hadoop_runtime_run0_90.csv'
        df_final = pd.read_csv(out_path)
        run_cols = [f"run_{i}" for i in range(1, 91)]
        before = len(df_final)
        df_final = df_final.dropna(subset=['param_1', 'run_time'])
        row_sums = df_final[run_cols].sum(axis=1)
        df_final = df_final[row_sums > 0].reset_index(drop=True)
        after = len(df_final)
        df_final.to_csv(out_path, index=False)
        print(f"  清洗: {before} → {after} 行（去掉 NaN 和全零行）")
