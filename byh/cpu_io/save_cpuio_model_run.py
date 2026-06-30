import os, sys
os.environ['PYTHONIOENCODING'] = 'utf-8'
sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

import argparse
import torch
sys.path.append(os.path.dirname(os.path.dirname(__file__)))
from model import CPUIOModel
import default as cfg

parser = argparse.ArgumentParser()
parser.add_argument("--TASK_NAME", type=str, required=True)
parser.add_argument("--NODE_NAME", type=str, required=True)
args = parser.parse_args()

weights_path = os.path.join(
    os.path.dirname(__file__),
    "output", args.TASK_NAME, args.NODE_NAME, "model_final.pth"
)
print("weights_path:", os.path.abspath(weights_path))

if not os.path.exists(weights_path):
    raise FileNotFoundError("Cannot find: " + weights_path)

model = CPUIOModel().to(cfg.DEVICE)
model.load_state_dict(torch.load(weights_path, map_location=cfg.DEVICE))
print("Loaded weights successfully.")

save_path = os.path.join(os.path.dirname(__file__), "CPUIOModel")
torch.save(model, save_path)
print("Saved model to:", save_path)
