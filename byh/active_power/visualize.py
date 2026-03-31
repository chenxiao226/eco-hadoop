import numpy as np
import pandas as pd
import sys
from matplotlib import pyplot as plt
from pandas import DataFrame

sys.path.append("..")
import default as cfg


def vis1(data_frame: DataFrame) -> None:
    for cpu_freq in cfg.CPU_FREQS:
        fig, ax1 = plt.subplots()
        ax2 = ax1.twinx()
        ax1.plot(
            np.arange(len(data_frame[data_frame["cpu_frequency"] == cpu_freq]["active_power"])),
            data_frame[data_frame["cpu_frequency"] == cpu_freq]["active_power"],
            color='#d62728', linewidth=1, label="power"
        )
        ax1.set_xticks([])
        ax1.set_yticks(np.arange(20, 61, 10))
        ax1.set_ylim(19, 61)
        ax1.set_ylabel("Power (W)")
        ax1.legend()
        ax2.plot(
            np.arange(len(data_frame[data_frame["cpu_frequency"] == cpu_freq]["cpu_usage"])),
            data_frame[data_frame["cpu_frequency"] == cpu_freq]["cpu_usage"] * 100,
            color='#2ca02c', linewidth=1, label="cpu usage",
        )
        ax2.set_xticks([])
        ax2.set_yticks(np.arange(0, 101, 20))
        ax2.set_ylim(-2, 102)
        ax2.set_ylabel("CPU Usage (%)")
        ax2.legend()
        plt.title("CPU usage and Power under {} CPU frequency".format(cpu_freq))
        plt.savefig(cfg.DATA_DIR + "/cpu_power_{}.jpg".format(cpu_freq))
        plt.close()

        fig, ax1 = plt.subplots()
        ax2 = ax1.twinx()
        ax1.plot(
            np.arange(len(data_frame[data_frame["cpu_frequency"] == cpu_freq]["active_power"])),
            data_frame[data_frame["cpu_frequency"] == cpu_freq]["active_power"],
            color='#d62728', linewidth=1, label="power"
        )
        ax1.set_xticks([])
        ax1.set_yticks(np.arange(20, 61, 10))
        ax1.set_ylim(19, 61)
        ax1.set_ylabel("Power (W)")
        ax1.legend()
        ax2.plot(
            np.arange(len(data_frame[data_frame["cpu_frequency"] == cpu_freq]["sda_usage"])),
            data_frame[data_frame["cpu_frequency"] == cpu_freq]["sda_usage"] * 100,
            color='#2ca02c', linewidth=1, label="sda usage",
        )
        ax2.set_xticks([])
        ax2.set_yticks(np.arange(0, 101, 20))
        ax2.set_ylim(-2, 102)
        ax2.set_ylabel("SDA Usage (%)")
        ax2.legend()
        plt.title("SDA usage and Power under {} CPU frequency".format(cpu_freq))
        plt.savefig(cfg.DATA_DIR + "/sda_power_{}.jpg".format(cpu_freq))
        plt.close()


def vis2(data_frame: DataFrame) -> None:
    for cpu_freq in cfg.CPU_FREQS:
        plt.figure()
        plt.scatter(
            data_frame[data_frame["cpu_frequency"] == cpu_freq]["cpu_usage"] * 100,
            data_frame[data_frame["cpu_frequency"] == cpu_freq]["active_power"],
            s=15, c='#1f77b4'
        )
        plt.xlabel("CPU usage (%)")
        plt.ylabel("Power (W)")
        plt.title("CPU usage and Power under {} CPU frequency".format(cpu_freq))
        plt.savefig(cfg.DATA_DIR + "/cpu_power_relation_{}.jpg".format(cpu_freq))
        plt.close()


def vis3(data_frame: DataFrame) -> None:
    plt.figure()
    plt.scatter(
        data_frame["cpu_usage"] * 100,
        data_frame["active_power"],
        s=15, c='#1f77b4'
    )
    plt.xlabel("CPU usage (%)")
    plt.ylabel("Power (W)")
    plt.title("CPU usage and Power under all CPU frequency")
    plt.savefig(cfg.DATA_DIR + "/cpu_power_relation.jpg")
    plt.close()


def vis4(data_frame: DataFrame) -> None:
    cpu_freqs = []
    activate_powers = []
    for cpu_freq in cfg.CPU_FREQS:
        cpu_freqs += data_frame[data_frame["cpu_frequency"] == cpu_freq]["cpu_frequency"].to_list()
        activate_powers += data_frame[data_frame["cpu_frequency"] == cpu_freq]["active_power"].to_list()
    fig, ax1 = plt.subplots()
    ax2 = ax1.twinx()
    lin1 = ax1.plot(
        np.arange(len(cpu_freqs)),
        cpu_freqs,
        color='#2ca02c', linewidth=1, label="CPU frequency"
    )
    ax1.set_xticks([])
    ax1.set_yticks(cfg.CPU_FREQS)
    ax1.set_ylabel("CPU frequency (MHz)")
    lin2 = ax2.plot(
        np.arange(len(activate_powers)),
        activate_powers,
        color='#d62728', linewidth=1, label="Power",
    )
    ax2.set_xticks([])
    ax2.set_yticks(np.arange(20, 61, 10))
    ax2.set_ylim(17, 63)
    ax2.set_ylabel("Power (W)")
    lins = lin1 + lin2
    plt.legend(lins, [l.get_label() for l in lins], fontsize='small', loc='upper left')
    plt.title("CPU frequency and Power")
    plt.savefig(cfg.DATA_DIR + "/cpu_freq_power.jpg")
    plt.close()


def vis5(data_frame: DataFrame) -> None:
    plt.figure(figsize=(15, 6))
    plt.scatter(
        data_frame["cpu_frequency"],
        data_frame["active_power"],
        s=15, c='#1f77b4'
    )
    plt.xticks(cfg.CPU_FREQS)
    plt.xlabel("CPU frequency (MHz)")
    plt.ylabel("Power (W)")
    plt.title("Relation of CPU frequency and Power")
    plt.savefig(cfg.DATA_DIR + "/cpu_freq_power_relation.jpg")
    plt.close()


if __name__ == '__main__':
    data_frame = pd.read_csv(cfg.DATA_DIR + "/sys_param.csv", encoding='utf-8')
    # vis1(data_frame)
    # vis2(data_frame)
    # vis3(data_frame)
    # vis4(data_frame)
    vis5(data_frame)
