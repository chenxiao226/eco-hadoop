import torch
from model import ActivePowerModel
import default as cfg


# 步骤1：实例化模型
model = ActivePowerModel()

# 步骤2：把模型放到 GPU 或 CPU
model = model.to(cfg.DEVICE)

# 步骤3：切换为评估模式（推理用）
model.eval()

# （可选）打印模型结构
print(model)