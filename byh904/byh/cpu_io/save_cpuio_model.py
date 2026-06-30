import argparse
import os
import sys
import torch

sys.path.append(os.path.dirname(os.path.dirname(__file__)))
from model import CPUIOModel
import default as cfg

parser = argparse.ArgumentParser()
parser.add_argument("--TASK_NAME", type=str, required=True)
parser.add_argument("--NODE_NAME", type=str, required=True)
args = parser.parse_args()

# 1️⃣ 权重文件路径
weights_path = os.path.join(
    os.path.dirname(__file__),
    "output", args.TASK_NAME, args.NODE_NAME, "model_final.pth"
)
print("🔍 权重路径:", os.path.abspath(weights_path))

if not os.path.exists(weights_path):
    raise FileNotFoundError(f"❌ 找不到权重文件: {weights_path}")

# 2️⃣ 创建模型实例并加载权重
model = CPUIOModel().to(cfg.DEVICE)
model.load_state_dict(torch.load(weights_path, map_location=cfg.DEVICE))
print("✅ 成功加载权重。")

# 3️⃣ 保存完整模型 (结构 + 权重)
save_path = os.path.join(os.path.dirname(__file__), "CPUIOModel")
torch.save(model, save_path)
print("✅ 已生成完整模型：", save_path)

