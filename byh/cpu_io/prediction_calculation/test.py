from typing import List

import pandas as pd
import os
import numpy as np
from sympy.codegen import Print
import csv
import pdb

from urllib3.filepost import writer


_BASE = os.path.dirname(os.path.abspath(__file__))
_BYH_ROOT = os.path.join(_BASE, '..', '..', '..')  # byh/ 根目录
log_file = os.path.join(_BYH_ROOT, 'active_power', 'k8s_dataset', 'jenkins', 'log_jenkins_web0.txt')
timelog_file = os.path.join(_BASE, 'output', 'time_slots.csv')
output_dir = os.path.join(_BASE, 'output', 'slots')
os.makedirs(output_dir, exist_ok=True)
result_dir = os.path.join(_BASE, 'output', 'result')
os.makedirs(output_dir, exist_ok=True)
param_file = os.path.join(_BASE, 'output', 'params_list.csv')
# === 加载日志数据 ===
log_df = pd.read_csv(log_file, sep="\t")
log_df["timestamp"] = pd.to_datetime(log_df["timestamp"])

# === 加载时间片数据 ===
time_df = pd.read_csv(timelog_file)
time_df.columns = time_df.columns.str.strip().str.replace("\ufeff", "")
time_df["start_time"] = pd.to_datetime(time_df["start_time"])
time_df["end_time"] = pd.to_datetime(time_df["end_time"])
time_df = time_df.rename(columns={"start_time": "begin_time"})

# === 初始化大列表保存所有 usage 记录 ===
all_usages = []
hit_logs = []
# === 遍历每个时间片 ===
for idx, row in time_df.iterrows():
    begin = row["begin_time"]
    end = row["end_time"]

    slot_logs = log_df[(log_df["timestamp"] >= begin) & (log_df["timestamp"] <= end)]
    selected = slot_logs[["timestamp", "cpu_freq_MHz", "cpu_usage_percent"]].copy()
    # print(selected)
    # ✅ 保存当前时间片单独文件
    output_file = os.path.join(output_dir, f"slot_{idx:03d}.csv")
    selected.to_csv(output_file, index=False)

    # ✅ 添加时间片编号，供合并使用
    selected["slot_id"] = idx
    all_usages.append(selected)

# === 保存整合后的大文件 ===
df_all = pd.concat(all_usages, ignore_index=True)
df_all.to_csv(os.path.join(output_dir, "all_usages.csv"), index=False)
# print("✅ 所有时间片数据已保存到 all_usages.csv")

df_slot = pd.DataFrame(slot_logs, columns=["timestamp", "cpu_freq_MHz", "cpu_usage_percent"])
df_slot["slot_id"] = idx  #标记是哪个时间片
all_usages.append(df_slot)

CPU_SLICES: List[float]=[1,6,6.5,7,7.5,8,8.5,9,9.5,10,12]
CPU_FREQS: List[int] =[1000,1200,1400,1500,1600,1800,2000,2500,2800,3000,3300]  # [800.0, 1022.2, ..., 3200.0]

# # 打印检查（可选）
print("CPU_SLICES:", CPU_SLICES)
print("FREQ_SLICES:", CPU_FREQS)
param_df = pd.read_csv(param_file)
# 构建所有数据点
all_data = []


time_ranges = []  # 用来存所有的开始和结束时间
for idx, (start_time, end_time) in enumerate(zip(time_df["begin_time"], time_df["end_time"])):
    print(f"\n================= 第 {idx} 个时间片 =================")
    print(f"开始时间: {begin}，结束时间: {end}")
    # （1）提取当前时间片的日志
    '''
    slot_logs = log_df[(log_df["timestamp"] >= start_time) & (log_df["timestamp"] <= end_time)]
    print(slot_logs)
    if slot_logs.empty:
        print(f"[{idx}] ❌ 无日志数据，跳过")
        continue
    '''

    time_ranges.append({
        "slot_id": idx,
        "begin": start_time,
        "end": end_time
    })

    # # ===（2）构建一个数据点的基本信息 ===


    # ② 运行时间
    run_time = int(time_df.loc[idx, "actual_duration_sec"])
    # print(run_time)
    cpu_frequency = int(log_df.loc[idx, "cpu_freq_MHz"])
    # print(cpu_frequency)
    NUM_CELLS = (len(CPU_SLICES) - 1) * (len(CPU_FREQS) - 1)
    # print(NUM_CELLS)
    # ③ 构建特征字典
    data_dict = {
        "cpu_frequency": cpu_frequency,
        "run_time": run_time,
    }

    # 添加参数 param_1 ~ param_n
    for i, param_col in enumerate(param_df.columns):
        data_dict[f"param_{i + 1}"] = param_df.iloc[idx, i]
    # slot_hit_logs = []
    all_data.append(data_dict)

df = pd.DataFrame(all_data)
# 保留前8列（索引 0~7）
fixed_df = df.iloc[:, :8]  # 前8列保留
# 构造 run_0 ~ run_100 的空列（初始值为 0）
run_columns = [f"run_{i}" for i in range(NUM_CELLS)]
run_df = pd.DataFrame(0, index=df.index, columns=run_columns)
# 拼接新 DataFrame
df = pd.concat([fixed_df, run_df], axis=1)
df.to_csv("all_data.csv", index=False)


print("time_ranges:",time_ranges)
#-------------------------------------------------------------------------------
# 遍历每条日志记录
for _, row in log_df.iterrows():
    #print("row:\n",row)

    cpu_usage = row["cpu_usage_percent"]
    cpu_freq = row["cpu_freq_MHz"]

    # 查找 CPU 使用率和频率所在的格子
    c_idx = next((i for i in range(len(CPU_SLICES) - 1)
                  if CPU_SLICES[i] <= cpu_usage < CPU_SLICES[i + 1]), -1)

    f_idx = next((j for j in range(len(CPU_FREQS) - 1)
                  if CPU_FREQS[j] <= cpu_freq < CPU_FREQS[j + 1]), -1)


    def find_slot_id(timestamp, time_ranges):
        for slot in time_ranges:
            if slot["begin"] <= timestamp < slot["end"]:
                return slot["slot_id"]
        return -1  # 如果不在任何时间段内


    time_idx = find_slot_id(row["timestamp"],time_ranges)


    '''
    hit_logs.append({
        "timestamp": row["timestamp"],
        "cpu_usage": cpu_usage,
        "cpu_freq": cpu_freq,
        "c_idx": c_idx+1,
        "f_idx": f_idx+1

    })
    # df_slot_hit = pd.DataFrame(slot_hit_logs)
    pd.DataFrame(hit_logs,
                 columns=["timestamp", "cpu_usage", "cpu_freq", "c_idx", "f_idx", ]).to_csv(
        "run_hit_logs.csv", index=False)
    with open("run_hit_logs.csv", "a", newline='') as f:
        writer = csv.writer(f)
        writer.writerow(["-" * 80])  # 分割
    # print("✅ 所有命中格子日志已保存到 run_hit_logs.csv")
    '''


    if c_idx != -1 and f_idx != -1:
        index = c_idx * (len(CPU_FREQS) - 1) + f_idx

        key = f"run_{index + 1}"
        if key in data_dict:
            data_dict[key] += 1
        else:
            data_dict[key] = 1
        '''
        print("data_dict[key]:", data_dict[key])
        print("row[timestamp]:",row["timestamp"])
        print("time_idx:",time_idx)
        print("cpu_freq:",cpu_freq)
        print("c_idx:",c_idx)
        print("len(CPU_FREQS):",len(CPU_FREQS))
        print("f_idx:",f_idx)
        print("index:",index)
        print("key:",key)
        print("data_dict[key]:",data_dict[key])
        print("all_data:",all_data)
        '''
        df.iloc[time_idx, index + 8] += 1
        df.to_csv("all_data.csv", index=False)

        #pdb.set_trace()

df.to_csv("all_data.csv", index=False)


