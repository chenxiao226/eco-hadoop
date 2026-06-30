"""
build_cpuio_dataset_from_generated.py
---------------------------------------
作用：
直接读取 file_1 生成的 Init_hadoop_runtime_run0_90.csv，
并依据文件2中 CPUIODataset 类的逻辑构建数据集。
构建完成后保存为 D:/Chenxiao/.../byh904/byh/cpu_io/CPUIODataset （无扩展名）。
"""

import os
import pandas as pd
import torch
from torch.utils.data import Dataset
from torch import Tensor
from typing import Tuple
import sys, os
sys.path.append(os.path.dirname(os.path.dirname(__file__)))  # 添加上一级目录(byh)到路径
import default as cfg
from scaler import transform


class CPUIODataset(Dataset):
    """保持文件2中的定义完全一致"""
    def __init__(self, data_frame: pd.DataFrame) -> None:
        required_columns = [
            "param_1", "param_2", "param_3", "param_4",
            "param_5", "param_6", "param_7", "param_8",
            "cpu_frequency", "run_time"
        ] + ["run_{}".format(r + 1) for r in range(90)]

        # 检查缺失列并补齐
        for col in required_columns:
            if col not in data_frame.columns:
                data_frame[col] = 0.0

        # 类型转换
        data_frame = data_frame.astype({col: "float32" for col in required_columns})

        # ---------- 构建输入 ----------
        param_1 = torch.as_tensor(data_frame[["param_1"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        param_1 = transform(param_1, min_data=cfg.MIN_PARAM_1, max_data=cfg.MAX_PARAM_1)

        param_2 = torch.as_tensor(data_frame[["param_2"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        param_2 = transform(param_2, min_data=cfg.MIN_PARAM_2, max_data=cfg.MAX_PARAM_2)

        param_3 = torch.as_tensor(data_frame[["param_3"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        param_3 = transform(param_3, min_data=cfg.MIN_PARAM_3, max_data=cfg.MAX_PARAM_3)

        param_4 = torch.as_tensor(data_frame[["param_4"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        param_4 = transform(param_4, min_data=cfg.MIN_PARAM_4, max_data=cfg.MAX_PARAM_4)

        param_5 = torch.as_tensor(data_frame[["param_5"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        param_5 = transform(param_5, min_data=cfg.MIN_PARAM_5, max_data=cfg.MAX_PARAM_5)

        param_6 = torch.as_tensor(data_frame[["param_6"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        param_6 = transform(param_6, min_data=cfg.MIN_PARAM_6, max_data=cfg.MAX_PARAM_6)

        param_7 = torch.as_tensor(data_frame[["param_7"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        param_8 = torch.as_tensor(data_frame[["param_8"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)

        cpu_freq = torch.as_tensor(data_frame[["cpu_frequency"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        cpu_freq = transform(cpu_freq, min_data=cfg.MIN_CPU_FREQ, max_data=cfg.MAX_CPU_FREQ)

        self.train_x = torch.cat((param_1, param_2, param_3, param_4,
                                  param_5, param_6, param_7, param_8, cpu_freq), dim=1)

        # ---------- 构建输出 ----------
        run_time = torch.as_tensor(data_frame[["run_time"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        run_time_max_value = torch.max(run_time)
        run_time_min_value = torch.min(run_time)
        self.train_y1 = transform(run_time, min_data=run_time_max_value, max_data=run_time_min_value)

        run_ratio = torch.as_tensor(data_frame[
            ["run_{}".format(r + 1) for r in range(len(cfg.CPU_SLICES) * len(cfg.SDA_SLICES))]
        ].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)

        self.train_y2 = run_ratio / run_ratio.sum(dim=1, keepdim=True).expand(
            run_ratio.size(0), len(cfg.CPU_SLICES) * len(cfg.SDA_SLICES)
        )

    def __getitem__(self, index) -> Tuple[Tensor, Tensor, Tensor]:
        return self.train_x[index], self.train_y1[index], self.train_y2[index]

    def __len__(self) -> int:
        return self.train_x.size(0)


def load_generated_csv(task_name: str, node_name: str) -> pd.DataFrame:
    """读取文件1生成的 Init_hadoop_runtime_run0_90.csv"""
    csv_path = os.path.join(
        os.path.dirname(__file__),
        "dataset", task_name, node_name, "Init_hadoop_runtime_run0_90.csv"
    )

    if not os.path.exists(csv_path):
        raise FileNotFoundError(f"❌ 未找到文件: {csv_path}")
    df = pd.read_csv(csv_path, encoding="utf-8")
    print(f"✅ 已载入 {csv_path}，共 {len(df)} 条记录。")
    return df


if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--TASK_NAME", type=str, required=True)
    parser.add_argument("--NODE_NAME", type=str, required=True)
    p = parser.parse_args()
    task_name = p.TASK_NAME
    node_name = p.NODE_NAME

    # 1️⃣ 读取数据
    df = load_generated_csv(task_name, node_name)

    # 2️⃣ 构建 Dataset
    dataset = CPUIODataset(df)
    print("✅ CPUIODataset 构建完成")
    print("样本数:", len(dataset))

    # 3️⃣ 保存 Dataset（不带扩展名、不建文件夹）
    save_path = os.path.join(os.path.dirname(__file__), "CPUIODataset")
    torch.save({
        "train_x": dataset.train_x.cpu(),
        "train_y1": dataset.train_y1.cpu(),
        "train_y2": dataset.train_y2.cpu(),
        "count": len(dataset),
    }, save_path)

    print(f"💾 数据集已保存：{save_path}")
