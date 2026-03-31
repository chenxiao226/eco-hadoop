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

def get_k8s_usage(param_file_dir):
    # 读取 CSV 文件
    param_file_dir = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\active_power\k8s_dataset\power_logs_process"
    df = pd.read_csv(os.path.join(param_file_dir, "sys_param.csv"))
    # 提取 cpu_usage 和 cpu_frequency 的最小值和最大值
    cpu_usage_min = df["cpu_usage"].min()
    cpu_usage_max = df["cpu_usage"].max()
    cpu_freq_min = df["cpu_frequency"].min()
    cpu_freq_max = df["cpu_frequency"].max()
    # print(cpu_usage_min,cpu_usage_max,cpu_freq_min,cpu_freq_max)
    # 获取范围
    usage_bins = usage_min, usage_max = df['cpu_usage'].min(), df['cpu_usage'].max()
    #分桶
    usage_bins = np.linspace(df['cpu_usage'].min(), df['cpu_usage'].max(), 20)
    usage_bins = np.round(usage_bins, 3)
    df['usage_bins'] = df['cpu_usage'].round(3)
    ## 生成标签，小数点后三位
    labels = [f"{round(usage_bins[i], 3)}-{round(usage_bins[i + 1], 3)}" for i in range(len(usage_bins) - 1)]
    # 按分组划分
    df['usage_bin'] = pd.cut(df['cpu_usage'], bins=usage_bins, labels=labels, include_lowest=True)
    # 统计每个频率区间出现次数
    usage_counts = df['usage_bin'].value_counts().sort_index()

    freq_min, freq_max = df['cpu_frequency'].min(), df['cpu_frequency'].max()
    freq_bins = np.linspace(df['cpu_frequency'].min(), df['cpu_frequency'].max(), 10).astype(int)
    freq_labels = [f"{int(freq_bins[i])}-{int(freq_bins[i + 1])}" for i in range(len(freq_bins) - 1)]
    df['freq_bin'] = pd.cut(df['cpu_frequency'], bins=freq_bins, labels=freq_labels, include_lowest=True)
    freq_counts = df['freq_bin'].value_counts().sort_index()
    # print(freq_counts.head(1))

    # # 创建一个 DataFrame 存储结果
    usage_stats_df = pd.DataFrame({
        'cpu_usage_percent': usage_counts.index,
        'cpu_usage_count': usage_counts.values
    })

    freq_stats_df = pd.DataFrame({
        'cpu_frequency_mhz': freq_counts.index,
        'cpu_frequency_count': freq_counts.values
    })

    usage_stats_df.to_csv('D:/Chenxiao/20241211HADOOPauto/byh904/byh/active_power/k8s_dataset/power_logs_process/cpu_usage_stats.csv', index=False)

    return
# parse_time_slots
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

def ataual_log_count(ataual_freq_dir):
    ataual_freq_dir=r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\active_power\k8s_dataset\jenkins"
    log_path = os.path.join(ataual_freq_dir, "log_jenkins_web0.txt")
    records = []

    with open(log_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    # ✅ 跳过第一行（表头）
    lines = lines[1:]

    for line in lines:
        line = line.strip()
        if not line:
            continue
        try:

            timestamp_str, cpu_freq, cpu_usage, disk_usage = line.split('\t')
            record = {
                "timestamp": pd.to_datetime(timestamp_str),
                "cpu_freq": float(cpu_freq),
                "cpu_usage": float(cpu_usage),
                "sda_usage": float(disk_usage)
            }
            records.append(record)
        except Exception as e:
            print(f"❌ 解析失败: {line}, 错误: {e}")

    return records

#     return grouped_stats
def group_and_count_by_timeslots(records, time_slots):
    """
    按任务时间time_slots段将日志记录分组，并统计每组中 cpu_freq、cpu_usage 和 sda_usage 的出现次数
    :param records: List[dict], 每个 dict 包含 timestamp, cpu_freq, cpu_usage, sda_usage
    :param time_slots: List[Tuple[start_time, end_time, sleep_time]]
    :return: List[dict], 每个 dict 包含 'cpu_freq_counts', 'cpu_usage_counts', 'sda_usage_counts'
    """
    ataual_freq_dir = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\active_power\k8s_dataset\jenkins"
    log_path = os.path.join(ataual_freq_dir, "log_jenkins_web0.txt")
    grouped_stats = []
    idx =1 # 应该从 0 开始
    cpu_freq_counter = Counter()
    cpu_usage_counter = Counter()
    sda_usage_counter = Counter()

    for record in records:
        ts = record['timestamp']

        # 跳过不匹配的时间段，进入下一个 time_slot
        while idx < len(time_slots) and ts > time_slots[idx][1]:
            if cpu_freq_counter or cpu_usage_counter or sda_usage_counter:
                grouped_stats.append({
                    'cpu_freq_counts': dict(cpu_freq_counter),
                    'cpu_usage_counts': dict(cpu_usage_counter),
                    'sda_usage_counts': dict(sda_usage_counter)
                })
                cpu_freq_counter = Counter()
                cpu_usage_counter = Counter()
                sda_usage_counter = Counter()

            idx += 1

        if idx < len(time_slots):
            start_time, end_time, _ = time_slots[idx]
            if start_time <= ts <= end_time:
                cpu_freq = int(record['cpu_freq'])
                cpu_usage = int(round(float(record['cpu_usage'])))
                sda_usage = int(round(float(record['sda_usage'])))  # 添加对 sda 使用率的统计


                cpu_freq_counter[cpu_freq] += 1
                cpu_usage_counter[cpu_usage] += 1
                sda_usage_counter[sda_usage] += 1

    # 最后一段别漏了
    if cpu_freq_counter or cpu_usage_counter or sda_usage_counter:
        grouped_stats.append({
            'cpu_freq_counts': dict(cpu_freq_counter),
            'cpu_usage_counts': dict(cpu_usage_counter),
            'sda_usage_counts': dict(sda_usage_counter)
        })
    # 保存结果
    output_path = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\grouped_stats.csv"
    final_df.to_csv(output_path, index=False)

    return grouped_stats

#


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



# === 加载 CSV ===

df = pd.read_csv(r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\group_stats_by_time.csv")

# === 应用转换 ===

df["requests_cpu_num"] = df["requests_cpu"].apply(parse_cpu)        # 单位：核心数（core）
df["limits_cpu_num"]   = df["limits_cpu"].apply(parse_cpu)          # 单位：核心数（core）
df["requests_mem_byte"] = df["requests_memory"].apply(parse_memory) # 单位：字节（Byte）
df["limits_mem_byte"]   = df["limits_memory"].apply(parse_memory)   # 单位：字节（Byte）

# === 保存结果 ===
df_stats = pd.read_csv(r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\convert_group_stats_by_time.csv")
df_time = pd.read_csv(r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\time_slots.csv")

# 检查行数一致
if len(df_stats) != len(df_time):
    raise ValueError("两个文件的行数不一致，不能直接合并！")

# 添加 actual_duration_sec 列到 df_stats，并重命名为 run_time
df_stats["run_time"] = df_time["actual_duration_sec"]

# 保存为新文件或覆盖原文件
df_stats.to_csv(r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\final_merge_data.csv", index=False)

# print("✅ 已成功将 run_time 添加到 final_merge_data.csv 中！")
# # === 加载 CSV ===
#
# df = pd.read_csv(r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\final_merge_data.csv")
# # print(f"run_time: min={df['run_time'].min()} s, max={df['run_time'].max()} s")

class CPUIODataset(Dataset):
    def __init__(self, data_frame: pd.DataFrame) -> None:
        """
        精简版：仅使用 param_1, param_2, param_3,cpu_frequency 四个输入，
        输出包括 run_time（归一化） 和 151维资源分布（归一化）。
        """
        # 检查必要字段
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
        cpu_freq = normalize_by_range(torch.tensor(data_frame[["cpu_freq"]].values, dtype=torch.float32, device=cfg.DEVICE), 800.0,3200.0)
        self.train_x = torch.cat([p1, p2, p3, p4, p5, cpu_freq], dim=1)
        run_time = torch.as_tensor(data_frame[["run_time"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)

        self.train_y1 = transform(run_time, min_val=1, max_val=36)

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
    #
    # # 1. 加载预处理好的 CSV 数据
    # csv_path = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\7param_dataset.csv"
    # df = pd.read_csv(csv_path)
    #
    # # 2. 实例化 CPUIODataset（你的类已经定义了）
    # dataset = CPUIODataset(df)
    #
    # # 3. 用 DataLoader 查看一批样本（可选调试）
    # loader = DataLoader(dataset, batch_size=2, shuffle=False)
    #
    # for x, y1, y2 in loader:
    #     print("📦 输入向量 shape:", x.shape)
    #     print("🎯 预测目标1 run_time shape:", y1.shape)
    #     print("🎯 预测目标2 资源分布 shape:", y2.shape)
    #     break  # 只看一批
    #
    # # 4. （可选）保存为中间张量，便于模型单独调试
    # torch.save(dataset.train_x, os.path.join(os.path.dirname(csv_path), "train_x.pt"))
    # torch.save(dataset.train_y1, os.path.join(os.path.dirname(csv_path), "train_y1.pt"))
    # torch.save(dataset.train_y2, os.path.join(os.path.dirname(csv_path), "train_y2.pt"))
    #
    # print("✅ 数据集对象已创建并保存到 .pt 文件")
    # save_dir = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\modeloutput"
    # os.makedirs(save_dir, exist_ok=True)
    # # with open(os.path.join(save_dir, "CPUIODataset.py"), "w", encoding="utf-8") as f:
    # #     f.write(dataset_code)
    #
    # print("✅ 已成功生成 CPUIODataset.py")

    # 调用get_mp_k8s生成param_lists
    # yamls_file_dir= r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\yaml_generation\generation_yamls"
    # params_list=get_mp_k8s(yamls_file_dir)
    # # 列名
    # columns = ['requests_cpu', 'requests_memory', 'limits_cpu', 'limits_memory', 'replica']
    # # 转为 DataFrame
    # df = pd.DataFrame(params_list, columns=columns)
    # # 保存为 CSV 文件
    # params_list_dir=r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output"
    # os.makedirs(params_list_dir, exist_ok=True)
    # output_path = os.path.join(params_list_dir, "params_list.csv")
    # df.to_csv(output_path, index=False)
    # print("✅ 成功保存参数列表至：", output_path)
# =========================================================================================================
#  调用函数parse_time_slots
#     timelog_dir_path=r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\k8s_dataset\jenkins"
#     time_slots=parse_time_slots(timelog_dir_path)
#     ataual_freq_dir=r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\active_power\k8s_dataset\jenkins"
#     records=ataual_log_count(ataual_freq_dir)
#     print(records)
#     columns = ['timestamp', 'cpu_freq', 'cpu_usage', 'sda_usage']
#         # 转为 DataFrame
#     df = pd.DataFrame(records, columns=columns)
#     standard_freqs = [800, 1000, 1200, 1600, 2000, 2300, 2500, 3000, 3200]
#     df["cpu_freq"] = df["cpu_freq"].apply(lambda x: min(standard_freqs, key=lambda s: abs(s - x)))
#     # 保存为 CSV 文件
#     params_list_dir=r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output"
#     output_path = os.path.join(params_list_dir, "records.csv")
#     df.to_csv(output_path, index=False, encoding='utf-8-sig')

#     # 列名
#     columns = ['requests_cpu', 'requests_memory', 'limits_cpu', 'limits_memory', 'replica']
#     # 转为 DataFrame
#     df = pd.DataFrame(params_list, columns=columns)
#     # df = df.drop_duplicates()
#     # 保存为 CSV 文件
#     params_list_dir=r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output"
#     output_path = os.path.join(params_list_dir, "k8s_params_output.csv")
#     grouped_stats=group_and_count_by_timeslots(records, time_slots)
#     # grouped_stats_list = 'D:/Chenxiao/20241211HADOOPauto/byh904/byh/active_power/k8s_dataset/power_logs_process/output/grouped_stats.csv'