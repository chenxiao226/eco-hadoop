import yaml
import numpy as np
import pandas as pd
import sys
import os
import pandas as pd
import torch
from torch.utils.data import Dataset
from typing import Tuple
from collections import Counter
import default as cfg
from scaler import transform

import bisect
# 原始日志文件路径（是文件，不是目录）
log_path = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\active_power\k8s_dataset\jenkins\log_jenkins_web0.txt"

# 输出路径（同目录，文件名替换）
output_path = os.path.join(os.path.dirname(log_path), "cpu_freq_log_jenkins_web0.txt")

# 加载原始日志
df = pd.read_csv(log_path, sep='\t')

# 匹配最接近的标准频率FREQ_LIST=(1600 2000 2500 3200)
standard_freqs = [800, 1000, 1200, 1600, 2000, 2300, 2500, 3000, 3200]
df["cpu_freq_nearest"] = df["cpu_freq_MHz"].apply(lambda x: min(standard_freqs, key=lambda s: abs(s - x)))

# 保存新日志
df.to_csv(output_path, sep='\t', index=False)


# 提取 run_time 最大最小值
df = pd.read_csv(r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\active_power\k8s_dataset\jenkins\time_slots.csv")
filtered_df = df[df["actual_duration_sec"] > 0]
MIN_RUN_TIME = filtered_df["actual_duration_sec"].min()
MAX_RUN_TIME = filtered_df["actual_duration_sec"].max()
# print(MIN_RUN_TIME, MAX_RUN_TIME)

# 读取带有最近频率列的日志（tab 分隔）
df = pd.read_csv(r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\active_power\k8s_dataset\jenkins\cpu_freq_log_jenkins_web0.txt", sep="\t")
unique_freqs = df.iloc[:, 4].unique()


MIN_PARAM_1 = df["cpu_usage_percent"].min()
MAX_PARAM_1 = df["cpu_usage_percent"].max()

MIN_PARAM_2 = df["disk_usage_percent"].min()
MAX_PARAM_2 = df["disk_usage_percent"].max()

MIN_CPU_FREQ = df["cpu_freq_nearest"].min()
MAX_CPU_FREQ = df["cpu_freq_nearest"].max()



# 路径配置（请根据你的实际情况修改）
log_path = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\active_power\k8s_dataset\jenkins\log_jenkins_web0.txt"
time_path = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\active_power\k8s_dataset\jenkins\time_slots.csv"
input_data_dir = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output"
output_path = os.path.join(os.path.dirname(input_data_dir), "k8s_dataset_input.csv")

# 读取原始日志（含 CPU/MHz、使用率）
log_df = pd.read_csv(log_path, sep='\t')

# 匹配最近标准频率
standard_freqs = [800, 1000, 1200, 1600, 2000, 2300, 2500, 3000, 3200]
log_df["cpu_frequency"] = log_df["cpu_freq_MHz"].apply(lambda x: min(standard_freqs, key=lambda s: abs(s - x)))

# 读取运行时间（只取非零）
time_df = pd.read_csv(time_path)
run_times = time_df[time_df["actual_duration_sec"] > 0]["actual_duration_sec"].tolist()

# 取前 N 条日志，确保一一对应（可调整规则）
N = min(len(run_times), len(log_df))
final_df = pd.DataFrame({
    "param_1": log_df.loc[:N-1, "cpu_usage_percent"].values,
    "param_2": log_df.loc[:N-1, "disk_usage_percent"].values,
    "cpu_frequency": log_df.loc[:N-1, "cpu_frequency"].values,
    "run_time": run_times[:N]
})

# 保存结果
final_df.to_csv(output_path, index=False)
# print(f"✅ 已保存至：{output_path}")

def get_mp_k8s(yamls_file_dir):
    """
    提取 web0_conf_*.yaml 中的 K8s 参数组合，返回列表，每行格式为：
    [cpu, memory, replica]
    """
    params_list = []
    yamls_file_dir = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\yaml_generation\generation_yamls"

    for i in range(1, 211):
        filename = f"web0_conf_{i:03}.yaml"
        yaml_path = os.path.join(yamls_file_dir, filename)
        # print("✅ 路径是否存在：", os.path.exists(yamls_file_dir))
        if not os.path.exists(yaml_path):
            print(f"⚠️ 文件不存在：{yaml_path}")
            continue

        # print("📂 正在读取文件：", filename)

        with open(yaml_path, 'r', encoding='utf-8') as f:
            try:
                doc = yaml.safe_load(f)
                container = doc['spec']['template']['spec']['containers'][0]
                requests_cpu = container['resources']['requests'].get('cpu', '0')
                requests_memory = container['resources']['requests'].get('memory', '0')
                limits_cpu = container['resources']['limits'].get('cpu', '0')
                limits_memory = container['resources']['limits'].get('memory', '0')
                # # volumeType 提取
                # volume_list = doc['spec']['template']['spec'].get('volumes', [])
                # volume_type = next((k for k in volume_list[0].keys() if k != "name"), "none") if volume_list else "none"

                # replica 提取
                replica = doc['spec'].get('replicas', 1)

                params_list.append([requests_cpu, requests_memory, limits_cpu,limits_memory, replica])
            except Exception as e:
                print(f"❌ YAML 解析失败: {yaml_path}, error: {e}")

    return params_list
# 时间片提取我
def parse_time_slots(timelog_dir_path: str):

    timelog_dir_path = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\k8s_dataset\jenkins"
    time_log_file = os.path.join(timelog_dir_path, "timelog_jenkins_web0.txt")
    df = pd.read_csv(time_log_file)
    df = df[df['success'] == True]
    time_slots = []

    for _, row in df.iterrows():
        try:
            start_time = pd.to_datetime(row['begin_time'])
            end_time = pd.to_datetime(row['end_time'])
            sleep_time = int(row['sleep_time'])  # 单位：秒

            actual_duration = int((end_time - start_time).total_seconds()) - sleep_time
            time_slots.append((start_time, end_time,  actual_duration))

        except Exception as e:
            print(f"⚠️ 时间解析失败：{row['begin_time']} ~ {row['end_time']}, 错误：{e}")

    time_slots_df = pd.DataFrame(time_slots, columns=[ "start_time", "end_time", "run_time"])
    time_slots_df.to_csv('D:/Chenxiao/20241211HADOOPauto/byh904/byh/active_power/k8s_dataset/power_logs_process/time_slots.csv', index=False, encoding='utf-8')
    # print(time_slots)
    return time_slots


# === 单位转换函数 ===

def parse_cpu(cpu_str):
    """
    转换CPU字符串，单位：'m' → 毫核，结果单位为 'core'（核心数）
    例：'250m' → 0.25
    """
    if isinstance(cpu_str, str) and cpu_str.endswith("m"):
        return float(cpu_str[:-1]) / 1000
    return float(cpu_str)  # 比如 '1' 就是 1 核

def parse_memory(mem_str):
    """
    转换内存字符串，单位为 'Mi' 或 'Gi'
    - 'Mi' = 1024^2 字节（Mebibytes）
    - 'Gi' = 1024^3 字节（Gibibytes）
    结果单位为：字节（Byte）
    """
    if isinstance(mem_str, str):
        if mem_str.endswith("Mi"):
            return float(mem_str[:-2]) * 1024 * 1024
        elif mem_str.endswith("Gi"):
            return float(mem_str[:-2]) * 1024 * 1024 * 1024
    return float(mem_str)


#每个时间片内资源（CPU 使用率 & 磁盘使用率）落入 90 个格子的次数，最终生成 run_1 ~ run_90
from typing import List

import pandas as pd
import os
import numpy as np
from sympy.codegen import Print
import csv

from urllib3.filepost import writer
log_file = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\active_power\k8s_dataset\jenkins\log_jenkins_web0.txt"
timelog_file = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\time_slots.csv"
output_dir = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\slots"
os.makedirs(output_dir, exist_ok=True)
result_dir=r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\result"
os.makedirs(output_dir, exist_ok=True)
param_file = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\params_list.csv"
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

for idx, (start_time, end_time) in enumerate(zip(time_df["begin_time"], time_df["end_time"])):
    print(f"\n================= 第 {idx} 个时间片 =================")
    print(f"开始时间: {begin}，结束时间: {end}")
    # （1）提取当前时间片的日志
    slot_logs = log_df[(log_df["timestamp"] >= start_time) & (log_df["timestamp"] <= end_time)]
    print(log_df["timestamp"].head(10))  # 打印前10个

    if slot_logs.empty:
        print(f"[{idx}] ❌ 无日志数据，跳过")
        continue

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

    # 遍历每条日志记录
    for _, row in slot_logs.iterrows():
        cpu_usage = row["cpu_usage_percent"]
        cpu_freq = row["cpu_freq_MHz"]

        # 查找 CPU 使用率和频率所在的格子
        c_idx = next((i for i in range(len(CPU_SLICES) - 1)
                      if CPU_SLICES[i] <= cpu_usage < CPU_SLICES[i + 1]), -1)

        f_idx = next((j for j in range(len(CPU_FREQS) - 1)
                      if CPU_FREQS[j] <= cpu_freq < CPU_FREQS[j + 1]), -1)
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
        if c_idx != -1 and f_idx != -1:
            index = c_idx * (len(CPU_FREQS) - 1) + f_idx
            # print(index)
            key = f"run_{index + 1}"
            # data_dict[key] = data_dict.get(key, 0) + 1
            # print(data_dict[key])
            if key in data_dict:
                data_dict[key] += 1
            else:
                data_dict[key] = 1

            all_data.append(data_dict)

    df = pd.DataFrame(all_data)
    df.to_csv("all_data.csv", index=False)
    # 1. 提取参数字段
    param_fields = [col for col in df.columns if col.startswith("param_")]

    run_fields = [col for col in df.columns if col.startswith("run_")]

    # 3. 构建列顺序（按你实际字段）
    columns_order = ["cpu_frequency", "run_time"] + param_fields + run_fields

    # 4. 重新排序并保存
    df = df[columns_order]


    # print("✅ 所有命中格子日志已保存到 run_hit_logs.csv")
    df.to_csv("result.csv", index=False)
    # non_zero_cols = df.loc[:, df.columns.str.startswith("run_")].sum()
    # print(non_zero_cols[non_zero_cols > 0])
class CPUIODataset(Dataset):
    def __init__(self, data_frame: pd.DataFrame) -> None:
        """
        精简版：仅使用 param_1, param_2, param_3,cpu_frequency 四个输入，
        输出包括 run_time（归一化） 和 151维资源分布（归一化）。
        """
        # 检查必要字段
        CPU_SLICES: List[float] = [1, 6, 6.5, 7, 7.5, 8, 8.5, 9, 9.5, 10, 12]
        CPU_FREQS: List[int] = [1000, 1200, 1400, 1500, 1600, 1800, 2000, 2500, 2800, 3000, 3300]
        required_columns = ["param_1", "param_2","param_3","param_4", "param_5","cpu_freq", "run_time"] + \
                           ["run_{}".format(r + 1) for r in range(90)]
        missing_columns = [col for col in required_columns if col not in data_frame.columns]
        if missing_columns:
            raise ValueError(f"缺失列：{missing_columns}")

        # 转 float32 类型
        data_frame = data_frame.astype({col: "float32" for col in required_columns})

        def normalize_by_range(tensor, min_val, max_val):
            return (tensor - min_val) / (max_val - min_val + 1e-8)

        p1 = normalize_by_range(torch.tensor(data_frame[["param_1"]].values, dtype=torch.float32),
                                min_val=0.25, max_val=1.50, device=cfg.DEVICE)

        p2 = normalize_by_range(torch.tensor(data_frame[["param_2"]].values, dtype=torch.float32),
                                min_val=536870912.0, max_val=6442450944, device=cfg.DEVICE)

        p3 = normalize_by_range(torch.tensor(data_frame[["param_3"]].values, dtype=torch.float32),
                                min_val=0.25, max_val=1.50, device=cfg.DEVICE)

        p4 = normalize_by_range(torch.tensor(data_frame[["param_4"]].values, dtype=torch.float32),
                                min_val=536870912.0, max_val=6442450944, device=cfg.DEVICE)

        p5 = normalize_by_range(torch.tensor(data_frame[["param_5"]].values, dtype=torch.float32),
                                min_val=1.0, max_val=2.0, device=cfg.DEVICE)
        cpu_freq = normalize_by_range(torch.tensor(data_frame[["cpu_freq"]].values, dtype=torch.float32, device=cfg.DEVICE), 1000,3300.0)
        self.train_x = torch.cat([p1, p2, p3, p4, p5, cpu_freq], dim=1)
        run_time = torch.as_tensor(data_frame[["run_time"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)

        self.train_y1 = transform(run_time, min_val=1, max_val=36)

        import pdb
        print("len(CPU_SLICES) * len(CPU_FREQS):",len(CPU_SLICES) * len(CPU_FREQS))
        for r in range(len(CPU_SLICES) * len(CPU_FREQS)):
            print("r:",r)
        pdb.set_trace()


        run_ratio = torch.as_tensor(
            data_frame[["run_{}".format(r + 1) for r in range(len(cfg.CPU_SLICES) * len(cfg.SDA_SLICES))]].to_numpy(),
            dtype=torch.float32, device=cfg.DEVICE)
        self.train_y2 = run_ratio / run_ratio.sum(dim=1, keepdim=True).expand(run_ratio.size(0),
                                                                              len(cfg.CPU_SLICES) * len(cfg.SDA_SLICES))


    def __getitem__(self, index) -> Tuple[torch.Tensor, torch.Tensor, torch.Tensor]:
        return self.train_x[index], self.train_y1[index], self.train_y2[index]

    def __len__(self) -> int:
        return self.train_x.size(0)



if __name__ == "__main__":
    from torch.utils.data import DataLoader

    # 1. 加载预处理好的 CSV 数据
    csv_path = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\7param_dataset.csv"
    df = pd.read_csv(csv_path)

    # 2. 实例化 CPUIODataset（你的类已经定义了）
    dataset = CPUIODataset(df)

    # 3. 用 DataLoader 查看一批样本（可选调试）
    loader = DataLoader(dataset, batch_size=2, shuffle=False)

    for x, y1, y2 in loader:
        print("📦 输入向量 shape:", x.shape)
        print("🎯 预测目标1 run_time shape:", y1.shape)
        print("🎯 预测目标2 资源分布 shape:", y2.shape)
        break  # 只看一批
    # #
    # 4. （可选）保存为中间张量，便于模型单独调试
    torch.save(dataset.train_x, os.path.join(os.path.dirname(csv_path), "train_x.pt"))
    torch.save(dataset.train_y1, os.path.join(os.path.dirname(csv_path), "train_y1.pt"))
    torch.save(dataset.train_y2, os.path.join(os.path.dirname(csv_path), "train_y2.pt"))

    print("✅ 数据集对象已创建并保存到 .pt 文件")
    save_dir = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\modeloutput"
    os.makedirs(save_dir, exist_ok=True)
    # with open(os.path.join(save_dir, "CPUIODataset.py"), "w", encoding="utf-8") as f:
    #     f.write(dataset_code)

    print("✅ 已成功生成 CPUIODataset.py")




