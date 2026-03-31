import argparse
import torch
from argparse import ArgumentParser
from typing import List

DEVICE: str = "cuda:0" if torch.cuda.is_available() else "cpu"
# DATA_DIR: str = "dataset"
CPU_FREQS: List[int] = [800, 1000, 1100, 1300, 1500, 1700, 1800, 2000, 2200, 2300, 2500, 2700, 2900, 3000, 3200]
# cpu_static: 10%
# sda_static: [0, 1), [1, 2), [2, 3), [3, 4), [4, 5), [5, 7), [7, 9), [9, 15), [15, 55)
CPU_SLICES: List[float] = [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0]
SDA_SLICES: List[float] = [0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 7.0, 9.0, 15.0]
CPU_STEP: float = 1.0
SDA_STEP: float = 0.1
CPU_UPPER_BOUND: float = 101.0
SDA_UPPER_BOUND: float = 55.0

MIN_PARAM_1, MAX_PARAM_1 = 10.0, 100.0
MIN_PARAM_2, MAX_PARAM_2 = 0.5, 0.9
MIN_PARAM_3, MAX_PARAM_3 = 100.0, 300.0
MIN_PARAM_4, MAX_PARAM_4 = 0.30, 1.00
MIN_PARAM_5, MAX_PARAM_5 = 0.3, 0.9
MIN_PARAM_6, MAX_PARAM_6 = 100.0, 1000.0
MIN_PARAM_7, MAX_PARAM_7 = 0.0, 1.0
MIN_PARAM_8, MAX_PARAM_8 = 0.0, 1.0
MIN_CPU_FREQ, MAX_CPU_FREQ = 800.0, 3200.0
MIN_ACTIVE_POWER, MAX_ACTIVE_POWER = 10.0, 70.0
MIN_RUN_TIME, MAX_RUN_TIME = 15.0, 300

INPUT_SIZE_1: int = 3
OUTPUT_SIZE_1: int = 1
BOTTLENECK_SIZES_1: List[int] = [64, 512, 512, 64, 8]
BATCH_SIZE_1: int = 2048
BASE_LR_1: float = 0.001
WEIGHT_DECAY_1: float = 0.0002
STEP_SIZE_1: int = 3000
GAMMA_1: float = 0.5
NUM_EPOCHS_1: int = 10000
WRITE_PERIOD_1: int = 500
EVAL_PERIOD_1: int = 1000

INPUT_SIZE_2: int = 9
OUTPUT_SIZE_2: int = len(CPU_SLICES) * len(SDA_SLICES)
INPUT_CHANNEL_2: int = 1
BOTTLENECK_CHANNELS_2: List[int] = [64, 256, 1024, 1024, 512]
FC_SIZES_2 = [512, 512]
BATCH_SIZE_2: int = 8192
BASE_LR_2: float = 0.001
WEIGHT_DECAY_2: float = 0.0002
STEP_SIZE_2: int = 3000
GAMMA_2: float = 0.5
LAMBDA_2: float = 1.0
NUM_EPOCHS_2: int = 10000
WRITE_PERIOD_2: int = 500
EVAL_PERIOD_2: int = 1000

HIDDEN_SIZE_3: int = 64
NUM_LAYERS_3: int = 6
NUM_REMAIN_3: int = 5
BATCH_SIZE_3: int = 512
BASE_LR_3: float = 0.0001
WEIGHT_DECAY_3: float = 0.0002
STEP_SIZE_3: int = 50
GAMMA_3: float = 0.5
LAMBDA_3: float = 1.0
NUM_EPOCHS_3: int = 10
T_3: int = 100
MAX_BUFFER_SIZE_3: int = 5000
REFINE_STEPS_3: int = 3
WRITE_PERIOD_3: int = 10
EVAL_PERIOD_3: int = 1


def default_argument_parser(description: str) -> ArgumentParser:
    parser = argparse.ArgumentParser(description=description)
    parser.add_argument("--eval-only", action="store_true", help="perform evaluation only")
    parser.add_argument("--output-dir", type=str, default="", help="path to output dir")
    parser.add_argument("--model-weights", type=str, default="", help="path to model weights")
    parser.add_argument("--active_power_model-weights", type=str, default="", help="path to active power model weights")
    parser.add_argument("--cpu_io_model-weights", type=str, default="", help="path to model weights")
    return parser
