import os
import pandas as pd
import torch
from torch.utils.data import Dataset
from typing import Tuple
import sys
sys.path.append("..")
import default as cfg
from scaler import transform


def load_processed_data(preprocessed_dir: str, task_name: str) -> pd.DataFrame:
    """
    从预处理输出的 ./preprocessed_data/<task_name>/log-power-*.csv 文件读取数据，
    返回统一格式的 DataFrame
    """
    path = os.path.join(preprocessed_dir, task_name)
    all_files = [f for f in os.listdir(path) if f.startswith('log-power') and f.endswith('.csv')]

    if not all_files:
        raise FileNotFoundError(f"⚠️ 未找到任何 log-power CSV 文件，请确认 {path} 下文件存在。")

    df_all = pd.DataFrame(columns=["datatime", "cpu_frequency", "cpu_usage", "sda_usage", "active_power"])

    for f in all_files:
        csv_path = os.path.join(path, f)
        df = pd.read_csv(csv_path, encoding='utf-8')
        # 重命名 'time' 列
        if 'time' in df.columns:
            df.rename(columns={'time': 'datatime'}, inplace=True)
        # 保留必要列
        for col in ["datatime", "cpu_frequency", "cpu_usage", "sda_usage", "active_power"]:
            if col not in df.columns:
                df[col] = None
        df = df[["datatime", "cpu_frequency", "cpu_usage", "sda_usage", "active_power"]]
        df_all = pd.concat([df_all, df], ignore_index=True)

    df_all.dropna(subset=["active_power"], inplace=True)
    print(f"✅ 已载入 {len(df_all)} 条数据。")
    return df_all


class ActivePowerDataset(Dataset):
    """
    根据新的CSV数据生成 PyTorch Dataset
    """
    def __init__(self, data_frame: pd.DataFrame) -> None:
        # 把需要的数值列转成 float
        for col in ["cpu_frequency", "cpu_usage", "sda_usage", "active_power"]:
            data_frame[col] = pd.to_numeric(data_frame[col], errors='coerce')

        # 过滤掉任何 NaN 行
        data_frame.dropna(subset=["cpu_frequency", "cpu_usage", "sda_usage", "active_power"], inplace=True)

        cpu_frequency = torch.as_tensor(
            data_frame[["cpu_frequency"]].to_numpy(dtype=float),
            dtype=torch.float32, device=cfg.DEVICE
        )
        cpu_frequency = transform(cpu_frequency, min_data=cfg.MIN_CPU_FREQ, max_data=cfg.MAX_CPU_FREQ)

        cpu_usage = torch.as_tensor(
            data_frame[["cpu_usage"]].to_numpy(dtype=float),
            dtype=torch.float32, device=cfg.DEVICE
        )
        sda_usage = torch.as_tensor(
            data_frame[["sda_usage"]].to_numpy(dtype=float),
            dtype=torch.float32, device=cfg.DEVICE
        )

        active_power = torch.as_tensor(
            data_frame[["active_power"]].to_numpy(dtype=float),
            dtype=torch.float32, device=cfg.DEVICE
        )
        active_power = transform(active_power, min_data=cfg.MIN_ACTIVE_POWER, max_data=cfg.MAX_ACTIVE_POWER)

        self.train_x = torch.cat((cpu_frequency, cpu_usage, sda_usage), dim=1)
        self.train_y = active_power

    def __getitem__(self, index) -> Tuple[torch.Tensor, torch.Tensor]:
        return self.train_x[index], self.train_y[index]

    def __len__(self) -> int:
        return self.train_x.size(0)




if __name__ == "__main__":
    # 设置任务名
    task_name = 'pagerank'
    # 处理过的数据目录（改成回到项目根 byh904）
    preprocessed_dir = os.path.join(
        os.path.dirname(os.path.dirname(os.path.dirname(__file__))),
        "hadoop_running_data_process",
        "active_power",
        "preprocessed_data"
    )

    print("📂 正在尝试读取数据目录：", os.path.join(preprocessed_dir, task_name))

    df = load_processed_data(preprocessed_dir, task_name)

    dataset = ActivePowerDataset(df)


    print("✅ ActivePowerDataset 构建完成")
    print("样本数:", len(dataset))
    print("示例 X:", dataset[0][0].cpu().numpy())
    print("示例 Y:", dataset[0][1].cpu().numpy())
    save_path = os.path.join(os.path.dirname(__file__), "ActivePowerDataset")

    torch.save({
        "train_x": dataset.train_x.cpu(),
        "train_y": dataset.train_y.cpu(),
        "count": len(dataset),
        "columns": ["cpu_frequency", "cpu_usage", "sda_usage", "active_power"]
    }, save_path)

    print(f"💾 数据集已保存到: {save_path}")
