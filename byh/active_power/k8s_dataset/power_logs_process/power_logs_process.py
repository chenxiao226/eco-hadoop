import pandas as pd
from datetime import datetime

import torch
import default as cfg
from scaler import transform
from typing import Tuple
import sys
import pandas as pd
from torch.utils.data import Dataset
from torch import Tensor
import os
import torch





# === Step 1: 读取日志 CSV ===
log_df = pd.read_csv('../jenkins/log_jenkins_web0.txt', encoding='utf-8-sig', sep='\t')  # 替换为你的路径
log_df['timestamp'] = pd.to_datetime(log_df['timestamp'])


# === Step 2: 读取功率计 XLSX ===
power_df = pd.read_excel('../jenkins/power.xls')  # 替换为你的路径
# 将“采集时间”补齐为完整时间戳，假设日期为日志的日期
# 提取日志中第一条的日期（如 2025-06-13）
base_date = log_df['timestamp'].dt.date.iloc[0]
power_df['timestamp'] = power_df['接收时间'].apply(
    lambda t: pd.to_datetime(f"{base_date} {t}")
)

# === Step 3: 提取功率计需要的列并重命名 ===
power_df = power_df[['timestamp', '有功功率', '年用电量', '有功电能']]
power_df.columns = ['timestamp', 'active_power', 'annual_energy', 'active_energy']

power_df.columns = [col.strip().replace('\ufeff', '') for col in power_df.columns]
# print("列名实际为：", power_df.columns.tolist())
# print(power_df.head())
# print("表格尺寸：", power_df.shape)
# === Step 4: 合并两个表（以时间戳为键）===
# 统一两个时间戳为无时区
log_df['timestamp'] = pd.to_datetime(log_df['timestamp'])
power_df['timestamp'] = pd.to_datetime(power_df['timestamp']).dt.tz_localize(None)

merged_df = pd.merge(log_df, power_df, on='timestamp', how='left')

# === Step 5: 保存为新 CSV ===
merged_df.to_csv('merged_output.csv', index=False, encoding='utf-8-sig', mode='w')
df = pd.read_csv('merged_output.csv')  # 或合并后的 DataFrame
# 指定需要插值的目标列
cols_to_fix = ['active_power', 'annual_energy', 'active_energy']
# 步骤 1: 转换为 float 类型（避免 object 报错）
df[cols_to_fix] = df[cols_to_fix].apply(pd.to_numeric, errors='coerce')
# 步骤 2: 插值填补 NaN（线性）
df[cols_to_fix] = df[cols_to_fix].interpolate(method='linear')
# 步骤 3: 仍有 NaN？用前向填充作为兜底（也可用0、均值等）
df[cols_to_fix] = df[cols_to_fix].ffill()
# print("每列缺失值数量：\n", df[cols_to_fix].isna().sum())
df.to_csv('merged_output_filled.csv', index=False, encoding='utf-8-sig', float_format='%.4f')

df = pd.read_csv("merged_output_filled.csv")
print("合并后的数据保存在merged_output_filled.csv")
# 2. 重命名列，变成模型认识的名字
df = df.rename(columns={
    "cpu_freq_MHz": "cpu_frequency",
    "cpu_usage_percent": "cpu_usage",
    "disk_usage_percent": "sda_usage"
})

# 3. 把百分比变成 0~1 的小数
df["cpu_usage"] = df["cpu_usage"] / 100.0
df["sda_usage"] = df["sda_usage"] / 100.0

# 4. 按照 cfg 配置进行归一化
cpu_freq = torch.tensor(df[["cpu_frequency"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
cpu_usage = torch.tensor(df[["cpu_usage"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
sda_usage = torch.tensor(df[["sda_usage"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)

cpu_freq = transform(cpu_freq, min_data=cfg.MIN_CPU_FREQ, max_data=cfg.MAX_CPU_FREQ)
x = torch.cat((cpu_freq, cpu_usage, sda_usage), dim=1)  # [N, 3]


class ActivePowerDataset(Dataset):
    def __init__(self, data_frame: pd.DataFrame) -> None:
        cpu_frequency = torch.as_tensor(data_frame[["cpu_frequency"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        cpu_frequency = transform(cpu_frequency, min_data=cfg.MIN_CPU_FREQ, max_data=cfg.MAX_CPU_FREQ)

        cpu_usage = torch.as_tensor(data_frame[["cpu_usage"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        sda_usage = torch.as_tensor(data_frame[["sda_usage"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)

        active_power = torch.as_tensor(data_frame[["active_power"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        active_power = transform(active_power, min_data=cfg.MIN_ACTIVE_POWER, max_data=cfg.MAX_ACTIVE_POWER)

        self.train_x = torch.cat((cpu_frequency, cpu_usage, sda_usage), dim=1)
        self.train_y = active_power

    def __getitem__(self, index) -> Tuple[Tensor, Tensor]:
        return self.train_x[index], self.train_y[index]

    def __len__(self) -> int:
        return self.train_x.size(0)




# power_logs_process.py 中添加为了parse_k8s_conf
# 放在 power_logs_process.py 的最后或合适位置
def build_power_tensor(df: pd.DataFrame) -> torch.Tensor:
    """
    从 DataFrame 中提取 cpu_frequency、cpu_usage、sda_usage 构建归一化张量 [N, 3]
    """


    cpu_freq = torch.tensor(df[['cpu_frequency']].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
    cpu_freq = transform(cpu_freq, min_data=cfg.MIN_CPU_FREQ, max_data=cfg.MAX_CPU_FREQ)

    cpu_usage = torch.tensor(df[['cpu_usage']].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
    sda_usage = torch.tensor(df[['sda_usage']].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)

    x = torch.cat((cpu_freq, cpu_usage, sda_usage), dim=1)  # shape: [N, 3]
    return x




def build_power_tensor(df: pd.DataFrame) -> torch.Tensor:
    """
    从 DataFrame 中提取 cpu_frequency、cpu_usage、sda_usage 构建归一化张量 [N, 3]
    """
    cpu_freq = torch.tensor(df[['cpu_frequency']].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
    cpu_freq = transform(cpu_freq, min_data=cfg.MIN_CPU_FREQ, max_data=cfg.MAX_CPU_FREQ)

    cpu_usage = torch.tensor(df[['cpu_usage']].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
    sda_usage = torch.tensor(df[['sda_usage']].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)

    x = torch.cat((cpu_freq, cpu_usage, sda_usage), dim=1)
    return x



if __name__ == '__main__':
    # ✅ 直接加载已经处理好的 CSV
    df = pd.read_csv("merged_output_filled.csv")

    # ✅ 重命名列，统一格式
    df = df.rename(columns={
        "cpu_freq_MHz": "cpu_frequency",
        "cpu_usage_percent": "cpu_usage",
        "disk_usage_percent": "sda_usage"
    })

    # ✅ 百分比转小数
    df["cpu_usage"] = df["cpu_usage"] / 100.0
    df["sda_usage"] = df["sda_usage"] / 100.0
    # ✅ 🔥 添加这句，确保目标路径存在
    # os.makedirs(cfg.DATA_DIR, exist_ok=True)

    # ✅ 保存为模型读取标准格式（可选）
    df.to_csv( "sys_param.csv", index=False, encoding="utf-8-sig", float_format="%.4f")

    print("数据已保存为:",  "sys_param.csv")
