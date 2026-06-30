import argparse
import os
import sys
import torch
from model import ActivePowerModel
sys.path.append("..")
import default as cfg

parser = argparse.ArgumentParser()
parser.add_argument("--TASK_NAME", type=str, required=True)
parser.add_argument("--NODE_NAME", type=str, required=True)
args = parser.parse_args()

# 创建模型实例
model = ActivePowerModel().to(cfg.DEVICE)

# 加载训练好的权重文件
weights_path = os.path.join(".", "output", args.TASK_NAME, args.NODE_NAME, "model_final.pth")
print("🔍 权重路径:", os.path.abspath(weights_path))
if not os.path.exists(weights_path):
    raise FileNotFoundError(f"❌ 找不到权重文件: {weights_path}")
model.load_state_dict(torch.load(weights_path, map_location=cfg.DEVICE))

# 保存整个模型（结构 + 权重）
save_path = os.path.join(".", "ActivePowerModel")
torch.save(model, save_path)
print(f"✅ 已生成 ActivePowerModel，路径： {save_path}")

