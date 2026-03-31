import pandas as pd
import numpy as np
from collections import Counter
from tqdm import tqdm
from datetime import datetime

# 文件路径
time_slots_path = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\time_slots.csv"
records_path = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\records.csv"
output_path = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\records_timeslots_merged.csv"  # run_1 ~ run_90 输出文件

# -------- Step 1: 读取 time_slots.csv --------
# === 步骤 1：读取文件 ===
df_records = pd.read_csv(records_path)
df_slots = pd.read_csv(time_slots_path)

# 转换为 datetime 类型
df_records["timestamp"] = pd.to_datetime(df_records["timestamp"])
df_slots["start_time"] = pd.to_datetime(df_slots["start_time"])
df_slots["end_time"] = pd.to_datetime(df_slots["end_time"])

# === 步骤 2：创建所有 cpu_usage × sda_usage 的组合（90种）===
cpu_slices = list(range(0, 101, 10))  # 0-100，每 10 一档
sda_slices = list(range(0, 53, 3))    # 0-52，每 3 一档
bins = [(c, s) for c in cpu_slices for s in sda_slices]  # 90 维组合

# === 步骤 3：对每个时间区间统计 run_1 ~ run_90 ===
rows = []
for _, slot in tqdm(df_slots.iterrows(), total=len(df_slots)):
    start, end = slot["start_time"], slot["end_time"]
    mask = (df_records["timestamp"] >= start) & (df_records["timestamp"] < end)
    df_slice = df_records[mask]

    # 初始化90维向量
    vec = [0] * len(bins)

    # 遍历该时间段内所有记录
    for _, row in df_slice.iterrows():
        cpu = row["cpu_usage"]
        sda = row["sda_usage"]

        # 找到匹配 bin 索引
        for idx, (c_bin, s_bin) in enumerate(bins):
            if c_bin <= cpu < c_bin + 10 and s_bin <= sda < s_bin + 3:
                vec[idx] += 1
                break  # 一条记录只计一次
    rows.append(vec)

# === 步骤 4：构建 DataFrame 并保存 ===
df_result = pd.DataFrame(rows, columns=[f"run_{i+1}" for i in range(len(bins))])
df_result.to_csv("records_timeslots_merged.csv", index=False)
print("✅ 已保存 records_timeslots_merged.csv，包含 run_1 ~ run_90")

