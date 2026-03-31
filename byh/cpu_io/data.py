import bisect
import math
import matplotlib.pyplot as plt
import pandas as pd
import sys
import torch
import pdb
from pandas import DataFrame
from torch import Tensor
from torch.utils.data import Dataset
from typing import List, Tuple
from xml.etree import ElementTree as ET

import default as cfg
from scaler import transform


def get_mp(file_dir: str) -> List[List[str]]:
    mapred_parameters = []
    for i in range(1, 101):
        mapred_params = []
        root = ET.parse(file_dir + "/mapred-site-{:03d}.xml".format(i))
        for property in root.iter("property"):
            if property.find('name').text in [
                "mapreduce.task.io.sort.factor",
                "mapreduce.map.sort.spill.percent",
                "mapreduce.task.io.sort.mb",
                "mapreduce.reduce.shuffle.merge.percent",
                "mapreduce.reduce.shuffle.input.buffer.percent",
                "mapreduce.reduce.merge.inmem.threshold",
                "mapreduce.output.fileoutputformat.compress",
                "mapreduce.map.output.compress",
            ]:
                mapred_params.append(property.find('value').text)
        mapred_parameters.append(mapred_params)
        # print(mapred_params)
    return mapred_parameters


def draw_usage(file_dir: str) -> None:
    min_cpu_usage, max_cpu_usage = 101.0, -1.0
    min_sda_usage, max_sda_usage = 101.0, -1.0
    cpu_static = [0] * 101
    sda_static = [0] * 53

    for cpu_freq in cfg.CPU_FREQS:
        freq_dir = file_dir + "/" + str(cpu_freq)

        time_slots = []
        with open(freq_dir + "/hibench.report", 'r', encoding='utf-8') as f:
            lines = f.readlines()
            for line in lines[1:]:
                time_line = line.split()
                end_time = pd.Timestamp(time_line[1] + " " + time_line[2])
                time_span = pd.Timedelta(seconds=round(float(time_line[4])))
                start_time = end_time - time_span
                time_slots.append([start_time, end_time, time_span])

        slave0_usages = []
        with open(freq_dir + "/slave0log.log", 'r', encoding='utf-8') as f:
            lines = f.readlines()
            i = 2
            idx = 0
            while (i + 32) < len(lines):
                if idx == len(time_slots):
                    break
                cnt_time = pd.Timestamp("{}-{}-{} {}:{}:{}".format(
                    lines[i][0: 4], lines[i][5: 7], lines[i][8: 10],
                    lines[i][12: 14], lines[i][15: 17], lines[i][18: 20],
                ))
                if cnt_time < time_slots[idx][0]:
                    i += 33
                elif cnt_time >= time_slots[idx][0] and cnt_time < time_slots[idx][1]:
                    cpu_usage = 100.0 - float(lines[i + 2].split()[-1])
                    min_cpu_usage = min(min_cpu_usage, cpu_usage)
                    max_cpu_usage = max(max_cpu_usage, cpu_usage)
                    sda_usage = float(lines[i + 13].split()[-1])
                    min_sda_usage = min(min_sda_usage, sda_usage)
                    max_sda_usage = max(max_sda_usage, sda_usage)
                    cpu_static[math.floor(cpu_usage)] += 1
                    sda_static[math.floor(sda_usage)] += 1
                    slave0_usages.append([cnt_time, cpu_usage, sda_usage])
                    i += 33
                else:
                    idx += 1
        plt.figure()
        plt.plot(
            [s[0] for s in slave0_usages],
            [s[1] for s in slave0_usages],
            color='#1f77b4', linewidth=1, label="cpu usage",
        )
        plt.plot(
            [s[0] for s in slave0_usages],
            [s[2] for s in slave0_usages],
            color='#ff7f0e', linewidth=1, label="sda usage",
        )
        plt.legend()
        plt.savefig(freq_dir + "/slave0_usages.jpg")
        plt.close()

        slave1_usages = []
        with open(freq_dir + "/slave1log.log", 'r', encoding='utf-8') as f:
            lines = f.readlines()
            i = 2
            idx = 0
            while (i + 31) < len(lines):
                if idx == len(time_slots):
                    break
                cnt_time = pd.Timestamp("{}-{}-{} {}:{}:{}".format(
                    lines[i][0: 4], lines[i][5: 7], lines[i][8: 10],
                    lines[i][12: 14], lines[i][15: 17], lines[i][18: 20],
                ))
                if cnt_time < time_slots[idx][0]:
                    i += 32
                elif cnt_time >= time_slots[idx][0] and cnt_time < time_slots[idx][1]:
                    cpu_usage = 100.0 - float(lines[i + 2].split()[-1])
                    min_cpu_usage = min(min_cpu_usage, cpu_usage)
                    max_cpu_usage = max(max_cpu_usage, cpu_usage)
                    sda_usage = float(lines[i + 13].split()[-1])
                    min_sda_usage = min(min_sda_usage, sda_usage)
                    max_sda_usage = max(max_sda_usage, sda_usage)
                    cpu_static[math.floor(cpu_usage)] += 1
                    sda_static[math.floor(sda_usage)] += 1
                    slave1_usages.append([cnt_time, cpu_usage, sda_usage])
                    i += 32
                else:
                    idx += 1
        plt.figure()
        plt.plot(
            [s[0] for s in slave1_usages],
            [s[1] for s in slave1_usages],
            color='#1f77b4', linewidth=1, label="cpu usage",
        )
        plt.plot(
            [s[0] for s in slave1_usages],
            [s[2] for s in slave1_usages],
            color='#ff7f0e', linewidth=1, label="sda usage"
        )
        plt.legend()
        plt.savefig(freq_dir + "/slave1_usages.jpg")
        plt.close()

        slave2_usages = []
        with open(freq_dir + "/slave2log.log", 'r', encoding='utf-8') as f:
            lines = f.readlines()
            i = 2
            idx = 0
            while (i + 33) < len(lines):
                if idx == len(time_slots):
                    break
                cnt_time = pd.Timestamp("{}-{}-{} {}:{}:{}".format(
                    lines[i][0: 4], lines[i][5: 7], lines[i][8: 10],
                    lines[i][12: 14], lines[i][15: 17], lines[i][18: 20],
                ))
                if cnt_time < time_slots[idx][0]:
                    i += 34
                elif cnt_time >= time_slots[idx][0] and cnt_time < time_slots[idx][1]:
                    cpu_usage = 100.0 - float(lines[i + 2].split()[-1])
                    min_cpu_usage = min(min_cpu_usage, cpu_usage)
                    max_cpu_usage = max(max_cpu_usage, cpu_usage)
                    sda_usage = float(lines[i + 13].split()[-1])
                    min_sda_usage = min(min_sda_usage, sda_usage)
                    max_sda_usage = max(max_sda_usage, sda_usage)
                    cpu_static[math.floor(cpu_usage)] += 1
                    sda_static[math.floor(sda_usage)] += 1
                    slave2_usages.append([cnt_time, cpu_usage, sda_usage])
                    i += 34
                else:
                    idx += 1
        plt.figure()
        plt.plot(
            [s[0] for s in slave2_usages],
            [s[1] for s in slave2_usages],
            color='#1f77b4', linewidth=1, label="cpu usage",
        )
        plt.plot(
            [s[0] for s in slave2_usages],
            [s[2] for s in slave2_usages],
            color='#ff7f0e', linewidth=1, label="sda usage"
        )
        plt.legend()
        plt.savefig(freq_dir + "/slave2_usages.jpg")
        plt.close()

    print(min_cpu_usage)
    print(max_cpu_usage)
    print(min_sda_usage)
    print(max_sda_usage)
    print(cpu_static)
    print(sda_static)

    plt.figure()
    plt.bar(range(len(cpu_static)), cpu_static, facecolor='#1f77b4')
    plt.xlabel("CPU usage (%)")
    plt.ylabel("Sample Numbers per CPU usage")
    plt.savefig(file_dir + "/cpu_static.jpg")
    plt.close()

    plt.figure()
    plt.bar(range(len(sda_static)), sda_static, facecolor='#ff7f0e')
    plt.xlabel("SDA usage (%)")
    plt.ylabel("Sample Numbers per SDA usage")
    plt.savefig(file_dir + "/sda_static.jpg")
    plt.close()


def get_data(file_dir: str, mapred_parameters: List[List[str]]) -> DataFrame:
    data_frame = pd.DataFrame(columns=[
        "param_1", "param_2", "param_3", "param_4", "param_5", "param_6", "param_7", "param_8",
        "cpu_frequency", "run_time",
    ] + ["run_{}".format(r) for r in range(1, len(cfg.CPU_SLICES) * len(cfg.SDA_SLICES) + 1)])

    for cpu_freq in cfg.CPU_FREQS:
        freq_dir = file_dir + "/" + str(cpu_freq)

        time_slots = []
        with open(freq_dir + "/hibench.report", 'r', encoding='utf-8') as f:
            lines = f.readlines()
            for line in lines[1:]:
                time_line = line.split()
                end_time = pd.Timestamp(time_line[1] + " " + time_line[2])
                time_span = pd.Timedelta(seconds=round(float(time_line[4])))
                start_time = end_time - time_span
                time_slots.append([start_time, end_time, time_span])

        slave0_usages = []
        with open(freq_dir + "/slave0log.log", 'r', encoding='utf-8') as f:
            lines = f.readlines()
            i = 2
            idx = 0
            slave0_usages_per_slot = []
            while (i + 32) < len(lines):
                if idx == len(time_slots):
                    break
                cnt_time = pd.Timestamp("{}-{}-{} {}:{}:{}".format(
                    lines[i][0: 4], lines[i][5: 7], lines[i][8: 10],
                    lines[i][12: 14], lines[i][15: 17], lines[i][18: 20],
                ))
                if cnt_time < time_slots[idx][0]:
                    i += 33
                elif cnt_time >= time_slots[idx][0] and cnt_time < time_slots[idx][1]:
                    cpu_usage = 100.0 - float(lines[i + 2].split()[-1])
                    sda_usage = float(lines[i + 13].split()[-1])
                    slave0_usages_per_slot.append([cnt_time, cpu_usage, sda_usage])
                    i += 33
                else:
                    idx += 1
                    slave0_usages.append(slave0_usages_per_slot)
                    slave0_usages_per_slot = []
            del slave0_usages_per_slot

        slave1_usages = []


        slave2_usages = []
        with open(freq_dir + "/slave2log.log", 'r', encoding='utf-8') as f:
            lines = f.readlines()
            i = 2
            idx = 0
            slave2_usages_per_slot = []
            while (i + 33) < len(lines):
                if idx == len(time_slots):
                    break
                cnt_time = pd.Timestamp("{}-{}-{} {}:{}:{}".format(
                    lines[i][0: 4], lines[i][5: 7], lines[i][8: 10],
                    lines[i][12: 14], lines[i][15: 17], lines[i][18: 20],
                ))
                if cnt_time < time_slots[idx][0]:
                    i += 34
                elif cnt_time >= time_slots[idx][0] and cnt_time < time_slots[idx][1]:
                    cpu_usage = 100.0 - float(lines[i + 2].split()[-1])
                    sda_usage = float(lines[i + 13].split()[-1])
                    slave2_usages_per_slot.append([cnt_time, cpu_usage, sda_usage])
                    i += 34
                else:
                    idx += 1
                    slave2_usages.append(slave2_usages_per_slot)
                    slave2_usages_per_slot = []
            del slave2_usages_per_slot

        for (
            idx, (time, slave0_usages_per_slot, slave1_usages_per_slot, slave2_usages_per_slot)
        ) in enumerate(zip(time_slots, slave0_usages, slave1_usages, slave2_usages)):
            data_dict = {"cpu_frequency": cpu_freq, "run_time": int(time[2].total_seconds())}
            for i in range(len(mapred_parameters[idx])):
                data_dict["param_{}".format(i + 1)] = mapred_parameters[idx][i]
            run_statics = [0] * (len(cfg.CPU_SLICES) * len(cfg.SDA_SLICES))
            for slave_usage in slave0_usages_per_slot:
                c = bisect.bisect_right(cfg.CPU_SLICES, slave_usage[1]) - 1
                s = bisect.bisect_right(cfg.SDA_SLICES, slave_usage[2]) - 1
                run_statics[c * len(cfg.SDA_SLICES) + s] += 1
            for slave_usage in slave1_usages_per_slot:
                c = bisect.bisect_right(cfg.CPU_SLICES, slave_usage[1]) - 1
                s = bisect.bisect_right(cfg.SDA_SLICES, slave_usage[2]) - 1
                run_statics[c * len(cfg.SDA_SLICES) + s] += 1
            for slave_usage in slave2_usages_per_slot:
                c = bisect.bisect_right(cfg.CPU_SLICES, slave_usage[1]) - 1
                s = bisect.bisect_right(cfg.SDA_SLICES, slave_usage[2]) - 1
                run_statics[c * len(cfg.SDA_SLICES) + s] += 1
            for r, run_static in enumerate(run_statics):
                data_dict["run_{}".format(r + 1)] = run_static
            data_frame.loc[len(data_frame)] = data_dict
    return data_frame


class CPUIODataset(Dataset):
    def __init__(self, data_frame: DataFrame) -> None:
        # 确保列的类型为 float32
        required_columns = ["param_1", "param_2", "param_3", "param_4", "param_5",
                            "param_6", "param_7", "param_8", "cpu_frequency", "run_time"] + \
                           ["run_{}".format(r + 1) for r in range(90)]

        # 检查列是否缺失
        missing_columns = [col for col in required_columns if col not in data_frame.columns]
        if missing_columns:
            raise ValueError(f"缺失的列: {missing_columns}")

        # 转换列类型为 float32
        data_frame = data_frame.astype({col: "float32" for col in required_columns})


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
        self.train_x = torch.cat((param_1, param_2, param_3, param_4, param_5, param_6, param_7, param_8, cpu_freq), dim=1)

        run_time = torch.as_tensor(data_frame[["run_time"]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        # 获取最大值、最小值
        run_time_max_value = torch.max(run_time)
        run_time_min_value = torch.min(run_time)
        self.train_y1 = transform(run_time, min_data=run_time_max_value, max_data=run_time_min_value)

        run_ratio = torch.as_tensor(data_frame[["run_{}".format(r + 1) for r in range(len(cfg.CPU_SLICES) * len(cfg.SDA_SLICES))]].to_numpy(), dtype=torch.float32, device=cfg.DEVICE)
        #print("run_ratio:",run_ratio)
        #pdb.set_trace()


        self.train_y2 = run_ratio / run_ratio.sum(dim=1, keepdim=True).expand(run_ratio.size(0), len(cfg.CPU_SLICES) * len(cfg.SDA_SLICES))

    def __getitem__(self, index) -> Tuple[Tensor, Tensor, Tensor]:
        return self.train_x[index], self.train_y1[index], self.train_y2[index]

    def __len__(self) -> int:
        return self.train_x.size(0)


if __name__ == '__main__':
    mapred_parameters = get_mp(cfg.DATA_DIR + "/mapred-site")

    draw_usage(cfg.DATA_DIR + "/run_time")               

    data_frame = get_data(cfg.DATA_DIR + "/run_time", mapred_parameters)
    data_frame.to_csv(cfg.DATA_DIR + "/run_time.csv", index=False, encoding='utf-8')
