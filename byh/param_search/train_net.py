import math
import os
import pandas as pd
import sys
import torch
from argparse import Namespace
from io import TextIOWrapper
from torch import nn, optim, Tensor
from torch.nn import functional as F
from torch.nn.modules.loss import _Loss as Loss
from torch.optim.lr_scheduler import _LRScheduler
from torch.optim.optimizer import Optimizer
from torch.utils.data import DataLoader
from torch.utils.tensorboard import SummaryWriter

from ..active_power.build_dataset_from_preprocessed import ActivePowerDataset
from ..active_power.model import ActivePowerModel
from ..cpu_io.build_cpuio_dataset_from_generated import CPUIODataset
from ..cpu_io.model import CPUIOModel
from .model import ParamEffiRainbow

import default as cfg
from scaler import transform, inverse_transform
import csv
import os

@torch.no_grad()
def set_active_powers(active_power_model: ActivePowerModel) -> None:
    global cpu_freqs
    cpu_freqs = torch.tensor(cfg.CPU_FREQS, dtype=torch.float32, device=cfg.DEVICE)
    cpu_freqs = transform(cpu_freqs, cfg.MIN_CPU_FREQ, cfg.MAX_CPU_FREQ)
    global active_powers
    active_powers = cpu_freqs.new_zeros((cpu_freqs.size(0), len(cfg.CPU_SLICES) * len(cfg.SDA_SLICES)))
    grid_cpu, grid_sda = torch.meshgrid(
        torch.arange(cfg.CPU_SLICES[0], cfg.CPU_UPPER_BOUND, cfg.CPU_STEP, device=cpu_freqs.device),
        torch.arange(cfg.SDA_SLICES[0], cfg.SDA_UPPER_BOUND, cfg.SDA_STEP, device=cpu_freqs.device),
        indexing='ij',
    )
    grid_usage = torch.cat((grid_cpu.reshape(-1, 1), grid_sda.reshape(-1, 1)), dim=1) / 100.0
    index = torch.cartesian_prod(
        torch.arange(cpu_freqs.size(0), device=cpu_freqs.device),
        torch.arange(grid_usage.size(0), device=cpu_freqs.device),
    )
    active_powers_features = torch.cat((cpu_freqs.reshape(-1, 1)[index[:, 0]], grid_usage[index[:, 1]]), dim=1)
    active_powers_outputs = active_power_model(active_powers_features).reshape(cpu_freqs.size(0), grid_cpu.size(0), grid_sda.size(1), -1)
    for i, (cpu_low, cpu_high) in enumerate(zip(cfg.CPU_SLICES, cfg.CPU_SLICES[1:] + [cfg.CPU_UPPER_BOUND])):
        cpu_l = math.floor((cpu_low - cfg.CPU_SLICES[0]) / cfg.CPU_STEP)
        cpu_h = math.floor((cpu_high - cfg.CPU_SLICES[0]) / cfg.CPU_STEP)
        for j, (sda_low, sda_high) in enumerate(zip(cfg.SDA_SLICES, cfg.SDA_SLICES[1:] + [cfg.CPU_UPPER_BOUND])):
            sda_l = math.floor((sda_low - cfg.SDA_SLICES[0]) / cfg.SDA_STEP)
            sda_h = math.floor((sda_high - cfg.SDA_SLICES[0]) / cfg.SDA_STEP)
            active_powers[:, i * len(cfg.SDA_SLICES) + j] = active_powers_outputs[:, cpu_l: cpu_h, sda_l: sda_h, :].mean(dim=(1, 2)).squeeze(-1)



@torch.no_grad()
def energy(batched_params: Tensor, cpu_io_model: CPUIOModel) -> Tensor:
    idxs = (batched_params[:, [-1]] == cpu_freqs).nonzero()[:, 1]
    batched_active_power = active_powers[idxs].clone()
    batched_active_power = inverse_transform(batched_active_power, cfg.MIN_ACTIVE_POWER, cfg.MAX_ACTIVE_POWER)

    batched_run_time, batched_run_perc = cpu_io_model(batched_params)
    batched_run_time = inverse_transform(batched_run_time, cfg.MIN_RUN_TIME, cfg.MAX_RUN_TIME)
    batched_run_perc = batched_run_perc.softmax(1)

    batched_energy = (batched_active_power * batched_run_time.expand((-1, batched_run_perc.size(1))) * batched_run_perc).sum(dim=1, keepdim=True)
    return batched_energy


@torch.no_grad()
def transformed_energy(batched_params: Tensor, cpu_io_model: CPUIOModel) -> Tensor:
    idxs = (batched_params[:, [-1]] == cpu_freqs).nonzero()[:, 1]
    batched_active_power = active_powers[idxs].clone()

    batched_run_time, batched_run_perc = cpu_io_model(batched_params)
    batched_run_perc = batched_run_perc.softmax(1)

    batched_energy = (batched_active_power * batched_run_time.expand((-1, batched_run_perc.size(1))) * batched_run_perc).sum(dim=1, keepdim=True)
    return batched_energy


def get_log_file(args: Namespace) -> TextIOWrapper:
    log_file = open(args.output_dir + "/log.txt", 'a', encoding='utf-8')
    log_file.write("DEVICE: \"" + cfg.DEVICE + "\"\n")
    if args.eval_only:
        log_file.write("EVAL_ONLY: True\n")
    else:
        log_file.write("EVAL_ONLY: False\n")
    log_file.write("SCALER:\n")
    log_file.write("  MIN_PARAM_1: {:.1f}\n".format(cfg.MIN_PARAM_1))
    log_file.write("  MAX_PARAM_1: {:.1f}\n".format(cfg.MAX_PARAM_1))
    log_file.write("  MIN_PARAM_2: {:.1f}\n".format(cfg.MIN_PARAM_2))
    log_file.write("  MAX_PARAM_2: {:.1f}\n".format(cfg.MAX_PARAM_2))
    log_file.write("  MIN_PARAM_3: {:.1f}\n".format(cfg.MIN_PARAM_3))
    log_file.write("  MAX_PARAM_3: {:.1f}\n".format(cfg.MAX_PARAM_3))
    log_file.write("  MIN_PARAM_4: {:.2f}\n".format(cfg.MIN_PARAM_4))
    log_file.write("  MAX_PARAM_4: {:.2f}\n".format(cfg.MAX_PARAM_4))
    log_file.write("  MIN_PARAM_5: {:.1f}\n".format(cfg.MIN_PARAM_5))
    log_file.write("  MAX_PARAM_5: {:.1f}\n".format(cfg.MAX_PARAM_5))
    log_file.write("  MIN_PARAM_6: {:.1f}\n".format(cfg.MIN_PARAM_6))
    log_file.write("  MAX_PARAM_6: {:.1f}\n".format(cfg.MAX_PARAM_6))
    log_file.write("  MIN_PARAM_7: {:.1f}\n".format(cfg.MIN_PARAM_7))
    log_file.write("  MAX_PARAM_7: {:.1f}\n".format(cfg.MAX_PARAM_7))
    log_file.write("  MIN_PARAM_8: {:.1f}\n".format(cfg.MIN_PARAM_8))
    log_file.write("  MAX_PARAM_8: {:.1f}\n".format(cfg.MAX_PARAM_8))
    log_file.write("  MIN_CPU_FREQ: {:.1f}\n".format(cfg.MIN_CPU_FREQ))
    log_file.write("  MAX_CPU_FREQ: {:.1f}\n".format(cfg.MAX_CPU_FREQ))
    log_file.write("  MIN_ACTIVE_POWER: {:.1f}\n".format(cfg.MIN_ACTIVE_POWER))
    log_file.write("  MAX_ACTIVE_POWER: {:.1f}\n".format(cfg.MAX_ACTIVE_POWER))
    log_file.write("  MIN_RUN_TIME: {:.1f}\n".format(cfg.MIN_RUN_TIME))
    log_file.write("  MAX_RUN_TIME: {:.1f}\n".format(cfg.MAX_RUN_TIME))
    log_file.write("DATASET:\n")
    log_file.write("  DATA_DIR: \"../cpu_io/" + cfg.DATA_DIR + "\"\n")
    log_file.write("  CPU_FREQ: " + str(cfg.CPU_FREQS) + "\n")
    log_file.write("  CPU_SLICES: " + str(cfg.CPU_SLICES) + "\n")
    log_file.write("  SDA_SLICES: " + str(cfg.SDA_SLICES) + "\n")
    log_file.write("  CPU_STEP: " + str(cfg.CPU_STEP) + "\n")
    log_file.write("  SDA_STEP: " + str(cfg.SDA_STEP) + "\n")
    log_file.write("  CPU_UPPER_BOUND: " + str(cfg.CPU_UPPER_BOUND) + "\n")
    log_file.write("  SDA_UPPER_BOUND: " + str(cfg.SDA_UPPER_BOUND) + "\n")
    log_file.write("  NUM_REMAIN: " + str(cfg.NUM_REMAIN_3) + "\n")
    log_file.write("MODEL:\n")
    log_file.write("  INPUT_SIZE: " + str(cfg.INPUT_SIZE_2) + "\n")
    log_file.write("  HIDDEN_SIZE: " + str(cfg.HIDDEN_SIZE_3) + "\n")
    log_file.write("  NUM_LAYERS: " + str(cfg.NUM_LAYERS_3) + "\n")
    if args.eval_only:
        log_file.write("  WEIGHTS: \"" + args.model_weights + "\"\n")
    log_file.write("ACTIVE_POWER_MODEL:\n")
    log_file.write("  WEIGHTS: \"" + args.active_power_model_weights + "\"\n")
    log_file.write("CPU_IO_MODEL:\n")
    log_file.write("  WEIGHTS: \"" + args.cpu_io_model_weights + "\"\n")
    log_file.write("SOLVER:\n")
    log_file.write("  BATCH_SIZE: " + str(cfg.BATCH_SIZE_3) + "\n")
    log_file.write("  BASE_LR: " + str(cfg.BASE_LR_3) + "\n")
    log_file.write("  WEIGHT_DECAY: " + str(cfg.WEIGHT_DECAY_3) + "\n")
    log_file.write("  STEP_SIZE: " + str(cfg.STEP_SIZE_3) + "\n")
    log_file.write("  GAMMA: " + str(cfg.GAMMA_3) + "\n")
    log_file.write("  LAMBDA: " + str(cfg.LAMBDA_3) + "\n")
    log_file.write("  NUM_EPOCHS: " + str(cfg.NUM_EPOCHS_3) + "\n")
    log_file.write("  T: " + str(cfg.T_3) + "\n")
    log_file.write("  MAX_BUFFER_SIZE: " + str(cfg.MAX_BUFFER_SIZE_3) + "\n")
    log_file.write("  REFINE_STEPS: " + str(cfg.REFINE_STEPS_3) + "\n")
    log_file.write("  WRITE_PERIOD: " + str(cfg.WRITE_PERIOD_3) + "\n")
    log_file.write("TEST:\n")
    log_file.write("  EVAL_PERIOD: " + str(cfg.EVAL_PERIOD_3) + "\n")
    return log_file


class Trainer:
    def __init__(
        self,
        args: Namespace,
        model: ParamEffiRainbow,
        criterion: Loss,
        optimizer: Optimizer,
        lr_scheduler: _LRScheduler,
        cpu_io_model: CPUIOModel,
    ) -> None:
        self.data_loader = self.build_loader()

        self.model = model
        self.criterion = criterion
        self.optimizer = optimizer
        self.lr_scheduler = lr_scheduler

        self.output_dir = args.output_dir
        self.log_file = get_log_file(args)

        self.cpu_io_model = cpu_io_model

    @classmethod
    def build_loader(self) -> DataLoader:
        run_time_file_path = os.path.join(
            os.path.dirname(os.path.dirname(os.path.dirname(__file__))),
            'byh', 'cpu_io', 'dataset', args.TASK_NAME, args.NODE_NAME,
            'Init_hadoop_runtime_run0_90.csv'
        )
        data_frame = pd.read_csv(run_time_file_path, encoding='utf-8')
        dataset = CPUIODataset(data_frame)
        return DataLoader(dataset=dataset, batch_size=cfg.BATCH_SIZE_3, shuffle=False)

    @classmethod
    @torch.no_grad()
    def get_init_states(self, data_loader: DataLoader, cpu_io_model: CPUIOModel) -> Tensor:
        energys = torch.cat([energy(batched_features, cpu_io_model) for batched_features, _, _ in data_loader], dim=0)
        _, indices = energys.topk(cfg.NUM_REMAIN_3, dim=0, largest=True, sorted=True)

        init_states = torch.cat([batched_features for batched_features, _, _ in data_loader], dim=0)
        return init_states[indices[:, 0]]

    @classmethod
    @torch.no_grad()
    def update_best_states(self, states: Tensor, best_states: Tensor, cpu_io_model: CPUIOModel) -> Tensor:
        all_states = torch.cat((states, best_states), dim=0).unique(dim=0)
        all_energys = energy(all_states, cpu_io_model)
        _, indices = all_energys.topk(cfg.NUM_REMAIN_3, dim=0, largest=False, sorted=True)
        return all_states[indices[:, 0]]

    def train(self) -> None:
        # Add TensorBoard
        writer = SummaryWriter(self.output_dir)
        self.log_file.write("Starting training from iteration 0.\n")

        best_states = self.get_init_states(self.data_loader, self.cpu_io_model)
        # +++ 新增代码开始：记录“优化前”的基准能耗 +++
        with torch.no_grad():
            initial_energys = energy(best_states, self.cpu_io_model)
            self.log_file.write("[Initial] Pre-optimization energys: {}\n".format(initial_energys))
            print("[Initial] Pre-optimization energys: {}".format(initial_energys))
            # 可以选择保存平均值或其他统计量以便对比
            initial_energy_avg = initial_energys.mean().item()
        # +++ 新增代码结束 +++

        # ... 原有代码 ...
        replay_buffer = best_states.new_zeros((0, best_states.size(1)))

        # +++ 新增：初始化收敛监控和训练历史 +++
        best_energy_history = []  # 记录每个Epoch的最佳平均能耗
        best_energy_avg = float('inf')
        no_improve_epochs = 0
        converged = False

        # 计算并记录初始能耗（作为“优化前”基准）
        with torch.no_grad():
            initial_energys = energy(best_states, self.cpu_io_model)
            initial_energy_avg = initial_energys.mean().item()
            self.log_file.write(f"[Baseline] Avg energy: {initial_energy_avg:.4f}, values: {initial_energys.tolist()}\n")
            print(f"[Baseline] Avg energy: {initial_energy_avg:.4f}, values: {initial_energys.tolist()}")
            initial_run_times, _ = self.cpu_io_model(self.get_init_states(self.data_loader, self.cpu_io_model))
            initial_run_times = inverse_transform(initial_run_times, cfg.MIN_RUN_TIME, cfg.MAX_RUN_TIME)
            initial_run_time_avg = initial_run_times.mean().item()
        # +++ 新增结束 +++
        # 定义任务名 - 通过命令行参数传入
        task_name = args.TASK_NAME

        # 确保目标目录存在
        output_dir = r"D:\Chenxiao\20260302VLDBDEMO\byh904\byh\output"
        os.makedirs(output_dir, exist_ok=True)

        # 生成CSV文件名（包含任务名、Lambda值和可选时间戳）
        import time
        timestamp = time.strftime("%Y%m%d_%H%M%S")
        csv_filename = f"{task_name}_Lambda{cfg.LAMBDA_PERF:.2f}_{timestamp}.csv"
        # 如果不需要时间戳，可以使用：csv_filename = f"{task_name}_Lambda{cfg.LAMBDA_PERF:.2f}.csv"

        csv_path = os.path.join(output_dir, csv_filename)
        csv_file = open(csv_path, 'w', newline='', encoding='utf-8')
        csv_writer = csv.writer(csv_file)

        # 写入表头
        header = [
            "Task_Name",  # 新增：任务名称
            "Lambda_Perf",  # 性能权衡系数
            "Epoch",
            "Best_Energy_Avg",
            "Energy_Improvement_Percent",
            "Best_RunTime_Avg",
            "RunTime_Change_Percent",
            "Energy_Time_Ratio",
            "No_Improve_Epochs",
            "Converged",
            "Training_Time"  # 新增：记录时间
        ]
        csv_writer.writerow(header)

        # 记录训练开始时间
        training_start_time = time.time()

        # 写入初始状态（Epoch=0）
        initial_energy_time_ratio = initial_energy_avg / initial_run_time_avg if initial_run_time_avg > 0 else float(
            'inf')
        current_time = time.strftime("%Y-%m-%d %H:%M:%S")

        csv_writer.writerow([
            task_name,  # Task_Name
            cfg.LAMBDA_PERF,  # Lambda_Perf
            0,  # Epoch
            initial_energy_avg,  # Best_Energy_Avg
            0.0,  # Energy_Improvement_Percent
            initial_run_time_avg,  # Best_RunTime_Avg
            0.0,  # RunTime_Change_Percent
            initial_energy_time_ratio,  # Energy_Time_Ratio
            0,  # No_Improve_Epochs
            False,  # Converged
            current_time  # Training_Time
        ])
        csv_file.flush()  # 立即写入磁盘
        print(f"CSV log file created: {csv_path}")
        print(f"  Task: {task_name}, Lambda_Perf: {cfg.LAMBDA_PERF}")
        # +++ 新增结束 +++
        cur_iter = 0
        for epoch in range(1, cfg.NUM_EPOCHS_3 + 1):
            states = best_states.clone()
            for t in range(cfg.T_3):
                with torch.no_grad():
                    replay_buffer = torch.cat((replay_buffer, states), dim=0)
                    if replay_buffer.size(0) > cfg.MAX_BUFFER_SIZE_3:
                        replay_buffer = replay_buffer[-1 - cfg.MAX_BUFFER_SIZE_3: -1]
                    sampled_idxs = torch.multinomial(replay_buffer.new_ones((replay_buffer.size(0),)) / replay_buffer.size(0), num_samples=cfg.BATCH_SIZE_3, replacement=True)
                    batched_states = replay_buffer[sampled_idxs]

                    states_new, _ = self.model(states)
                    states = states_new.clone()

                # 以下是更改为权衡低能耗和完成时间的奖励函数
                batched_states_new, batched_q = self.model(batched_states)
                with torch.no_grad():
                    best_states = self.update_best_states(batched_states, best_states, self.cpu_io_model)

                    # --- 修改开始：获取旧状态的能耗与运行时间 ---
                    # 1. 获取旧状态的能耗和运行时间（仅一次，不需要循环）
                    idxs_old = (batched_states[:, [-1]] == cpu_freqs).nonzero()[:, 1]
                    batched_active_power_old = active_powers[idxs_old].clone()
                    batched_run_time_old, batched_run_perc_old = self.cpu_io_model(batched_states)
                    batched_run_perc_old = batched_run_perc_old.softmax(1)
                    batched_trans_energys = (batched_active_power_old * batched_run_time_old.expand(
                        (-1, batched_run_perc_old.size(1))) * batched_run_perc_old).sum(dim=1, keepdim=True)

                    # 2. REFINE_STEPS循环中只更新状态和Q值，不重复计算能耗
                    for _ in range(cfg.REFINE_STEPS_3):
                        batched_states_new, batched_q_new = self.model(batched_states_new)  # 这行必须保留！
                        best_states = self.update_best_states(batched_states_new, best_states, self.cpu_io_model)

                    # 3. 循环结束后，获取新状态的能耗与运行时间
                    idxs_new = (batched_states_new[:, [-1]] == cpu_freqs).nonzero()[:, 1]
                    batched_active_power_new = active_powers[idxs_new].clone()
                    batched_run_time_new, batched_run_perc_new = self.cpu_io_model(batched_states_new)
                    batched_run_perc_new = batched_run_perc_new.softmax(1)
                    batched_trans_energys_new = (batched_active_power_new * batched_run_time_new.expand(
                        (-1, batched_run_perc_new.size(1))) * batched_run_perc_new).sum(dim=1, keepdim=True)
                    # --- 修改结束 ---

                    # 4. 计算新的权衡奖励
                    lambda_perf = cfg.LAMBDA_PERF  # 需要在 default.py 中定义这个新参数
                    batched_reward = (batched_trans_energys - batched_trans_energys_new) - lambda_perf * (
                                batched_run_time_new - batched_run_time_old)

                    batched_target = batched_reward + batched_q_new  # 现在batched_q_new已经在循环中被正确更新了

                loss = self.criterion(batched_q, batched_target) * 10000
                self.optimizer.zero_grad()
                loss.backward()
                self.optimizer.step()

                cur_iter += 1
                if cur_iter % cfg.WRITE_PERIOD_3 == 0:
                    self.log_file.write("iter: {}, loss: {}, q: {}\n".format(
                        str(cur_iter).rjust(6), loss.item(), batched_q.mean().item()))
                    self.log_file.flush()
                    print("iter: {}, loss: {}, q: {}".format(
                        str(cur_iter).rjust(6), loss.item(), batched_q.mean().item()))
                writer.add_scalar("train_loss", loss.item(), cur_iter)
            self.lr_scheduler.step()

            if epoch % cfg.EVAL_PERIOD_3 == 0:
                torch.save(self.model.state_dict(), self.output_dir + "/model_{:05}.pth".format(epoch))
                self.log_file.write("Saving checkpoint to " + self.output_dir + "/model_{:05}.pth.\n".format(epoch))
                self.log_file.flush()
                print("Saving checkpoint to " + self.output_dir + "/model_{:05}.pth.".format(epoch))
                best_energys = energy(best_states, self.cpu_io_model)
                current_best_energy_avg = best_energys.mean().item()
                best_energy_history.append(current_best_energy_avg)
                best_run_times, _ = self.cpu_io_model(best_states)
                best_run_times = inverse_transform(best_run_times, cfg.MIN_RUN_TIME, cfg.MAX_RUN_TIME)
                best_run_times_avg = best_run_times.mean().item()
                # 计算相对于初始基准的改进
                improvement_vs_initial = (initial_energy_avg - current_best_energy_avg) / initial_energy_avg * 100
                runtime_change_pct = (best_run_times_avg - initial_run_time_avg) / initial_run_time_avg * 100
                # 计算能效比
                energy_time_ratio = current_best_energy_avg / best_run_times_avg if best_run_times_avg > 0 else float(
                    'inf')

                # 判断是否收敛
                is_converged = (no_improve_epochs >= cfg.PATIENCE)

                # 写入一行数据
                csv_writer.writerow([
                    epoch,  # Epoch
                    current_best_energy_avg,  # Best_Energy_Avg
                    improvement_vs_initial,  # Energy_Improvement_Percent
                    best_run_times_avg,  # Best_RunTime_Avg
                    runtime_change_pct,  # RunTime_Change_Percent
                    energy_time_ratio,  # Energy_Time_Ratio
                    no_improve_epochs,  # No_Improve_Epochs
                    is_converged,  # Converged
                    cfg.LAMBDA_PERF  # Lambda_Perf
                ])
                csv_file.flush()  # 立即写入磁盘，防止数据丢失
                # +++ 新增结束 +++

                # ... 原有的日志输出代码 ...

                # 如果收敛，提前结束
                if is_converged:
                    break


                # 收敛判断：检查是否连续多个周期无明显改善
                improvement = best_energy_avg - current_best_energy_avg
                if improvement > cfg.CONVERGENCE_THRESH:  # 在default.py中定义，如0.001
                    best_energy_avg = current_best_energy_avg
                    no_improve_epochs = 0
                    self.log_file.write(
                        f"Epoch {epoch}: Better solution found, avg energy -> {current_best_energy_avg:.4f} (improvement {improvement_vs_initial:.2f}%)\n")
                    print(
                        f"Epoch {epoch}: Better solution found, avg energy -> {current_best_energy_avg:.4f} (improvement {improvement_vs_initial:.2f}%)")
                else:
                    no_improve_epochs += 1
                    self.log_file.write(
                        f"Epoch {epoch}: No significant improvement for {no_improve_epochs} epochs (<{cfg.CONVERGENCE_THRESH})\n")
                    print(f"Epoch {epoch}: No significant improvement for {no_improve_epochs} epochs (<{cfg.CONVERGENCE_THRESH})")

                # 早停条件
                if no_improve_epochs >= cfg.PATIENCE:  # 在default.py中定义，如5
                    self.log_file.write(f"Model converged! Early stopping at Epoch {epoch}.\n")
                    print(f"Model converged! Early stopping at Epoch {epoch}.")
                    converged = True
                    break  # 跳出训练循环
                # +++ 新增结束 +++

                best_states_tmp = torch.cat((
                    inverse_transform(best_states[:, [0]], cfg.MIN_PARAM_1, cfg.MAX_PARAM_1).round(decimals=2),
                    inverse_transform(best_states[:, [1]], cfg.MIN_PARAM_2, cfg.MAX_PARAM_2).round(decimals=2),
                    inverse_transform(best_states[:, [2]], cfg.MIN_PARAM_3, cfg.MAX_PARAM_3).round(decimals=2),
                    inverse_transform(best_states[:, [3]], cfg.MIN_PARAM_4, cfg.MAX_PARAM_4).round(decimals=2),
                    inverse_transform(best_states[:, [4]], cfg.MIN_PARAM_5, cfg.MAX_PARAM_5).round(decimals=2),
                    inverse_transform(best_states[:, [5]], cfg.MIN_PARAM_6, cfg.MAX_PARAM_6).round(decimals=2),
                    inverse_transform(best_states[:, [6]], cfg.MIN_PARAM_7, cfg.MAX_PARAM_7).round(decimals=2),
                    inverse_transform(best_states[:, [7]], cfg.MIN_PARAM_8, cfg.MAX_PARAM_8).round(decimals=2),
                    inverse_transform(best_states[:, [8]], cfg.MIN_CPU_FREQ, cfg.MAX_CPU_FREQ).round(decimals=2),
                ), dim=1)
                self.log_file.write("The best states: {}\n".format(best_states_tmp))
                print("The best states: {}".format(best_states_tmp))
                self.log_file.write("The best energys: {}\n".format(best_energys))
                print("The best energys: {}".format(best_energys))
        #         新增
                current_best_energy_avg = best_energys.mean().item()
                improvement_ratio = (initial_energy_avg - current_best_energy_avg) / initial_energy_avg * 100
                self.log_file.write("[Optimized] energys: {}\n".format(best_energys))
                self.log_file.write(
                    "[Result] Initial Avg: {:.4f}, Optimized Avg: {:.4f}, Improvement: {:.2f}%\n".format(
                        initial_energy_avg, current_best_energy_avg, improvement_ratio))

                print("[Optimized] energys: {}".format(best_energys))
                print("[Result] Initial Avg: {:.4f}, Optimized Avg: {:.4f}, Improvement: {:.2f}%".format(
                    initial_energy_avg, current_best_energy_avg, improvement_ratio))
        torch.save(self.model.state_dict(), self.output_dir + "/model_final.pth")
        self.log_file.write("Saving checkpoint to " + self.output_dir + "/model_final.pth.\n")
        print("Saving checkpoint to " + self.output_dir + "/model_final.pth.")
        best_energys = energy(best_states, self.cpu_io_model)
        best_states_tmp = torch.cat((
            inverse_transform(best_states[:, [0]], cfg.MIN_PARAM_1, cfg.MAX_PARAM_1).round(decimals=2),
            inverse_transform(best_states[:, [1]], cfg.MIN_PARAM_2, cfg.MAX_PARAM_2).round(decimals=2),
            inverse_transform(best_states[:, [2]], cfg.MIN_PARAM_3, cfg.MAX_PARAM_3).round(decimals=2),
            inverse_transform(best_states[:, [3]], cfg.MIN_PARAM_4, cfg.MAX_PARAM_4).round(decimals=2),
            inverse_transform(best_states[:, [4]], cfg.MIN_PARAM_5, cfg.MAX_PARAM_5).round(decimals=2),
            inverse_transform(best_states[:, [5]], cfg.MIN_PARAM_6, cfg.MAX_PARAM_6).round(decimals=2),
            inverse_transform(best_states[:, [6]], cfg.MIN_PARAM_7, cfg.MAX_PARAM_7).round(decimals=2),
            inverse_transform(best_states[:, [7]], cfg.MIN_PARAM_8, cfg.MAX_PARAM_8).round(decimals=2),
            inverse_transform(best_states[:, [8]], cfg.MIN_CPU_FREQ, cfg.MAX_CPU_FREQ).round(decimals=2),
        ), dim=1)
        self.log_file.write("The best states: {}\n".format(best_states_tmp))
        print("The best states: {}".format(best_states_tmp))
        self.log_file.write("The best energys: {}\n".format(best_energys))
        print("The best energys: {}".format(best_energys))

    @classmethod
    @torch.no_grad()
    def test(self, model: CPUIOModel, log_file: TextIOWrapper, cpu_io_model: CPUIOModel,
    ) -> None:
        data_loader = self.build_loader()
        log_file.write("Start inference on {} batches:\n".format(len(data_loader)))
        print("Start inference on {} batches:".format(len(data_loader)))

        total_num = 0
        best_states = self.get_init_states(data_loader, cpu_io_model)
        for i, (batched_features, _, _) in enumerate(data_loader):
            batched_features_cp = batched_features.clone()
            total_num += batched_features.size(0)
            for t in range(cfg.T_3):
                batched_features_cp, _ = model(batched_features_cp)
                best_states = self.update_best_states(batched_features_cp, best_states, cpu_io_model)
            log_file.write("Inference done {}/{}.\n".format(i + 1, len(data_loader)))
            print("Inference done {}/{}.".format(i + 1, len(data_loader)))
        log_file.write("Finish inference on {} data.\n".format(total_num))
        print("Finish inference on {} data.".format(total_num))

        best_energys = energy(best_states, self.cpu_io_model)
        best_states_tmp = torch.cat((
            inverse_transform(best_states[:, [0]], cfg.MIN_PARAM_1, cfg.MAX_PARAM_1),
            inverse_transform(best_states[:, [1]], cfg.MIN_PARAM_2, cfg.MAX_PARAM_2),
            inverse_transform(best_states[:, [2]], cfg.MIN_PARAM_3, cfg.MAX_PARAM_3),
            inverse_transform(best_states[:, [3]], cfg.MIN_PARAM_4, cfg.MAX_PARAM_4),
            inverse_transform(best_states[:, [4]], cfg.MIN_PARAM_5, cfg.MAX_PARAM_5),
            inverse_transform(best_states[:, [5]], cfg.MIN_PARAM_6, cfg.MAX_PARAM_6),
            inverse_transform(best_states[:, [6]], cfg.MIN_PARAM_7, cfg.MAX_PARAM_7),
            inverse_transform(best_states[:, [7]], cfg.MIN_PARAM_8, cfg.MAX_PARAM_8),
            inverse_transform(best_states[:, [8]], cfg.MIN_CPU_FREQ, cfg.MAX_CPU_FREQ),
        ), dim=1)
        log_file.write("The best states: {}\n".format(best_states_tmp))
        print("The best states: {}".format(best_states_tmp))
        log_file.write("The best energys: {}\n".format(best_energys))
        print("The best energys: {}".format(best_energys))


if __name__ == '__main__':
    args, _unknown = cfg.default_argument_parser("param_search").parse_known_args()
    # 追加任务名和节点名参数（复用同一 parser 前先 parse_known_args 再追加）
    import argparse as _ap
    _extra = _ap.ArgumentParser(add_help=False)
    _extra.add_argument("--TASK_NAME", type=str, required=True)
    _extra.add_argument("--NODE_NAME", type=str, required=True)
    _extra.add_argument("--epsilon", type=float, default=None, help="Convergence threshold (overrides cfg.CONVERGENCE_THRESH)")
    _extra.add_argument("--theta",   type=int,   default=None, help="Patience period (overrides cfg.PATIENCE)")
    _extra.add_argument("--lambda",  type=float, default=None, dest="lambda_perf", help="Lambda penalty coefficient (overrides cfg.LAMBDA_PERF)")
    _extra_args, _ = _extra.parse_known_args()
    args.TASK_NAME = _extra_args.TASK_NAME
    args.NODE_NAME = _extra_args.NODE_NAME
    # Override convergence params if provided via CLI
    if _extra_args.epsilon is not None:
        cfg.CONVERGENCE_THRESH = _extra_args.epsilon
    if _extra_args.theta is not None:
        cfg.PATIENCE = _extra_args.theta
    if _extra_args.lambda_perf is not None:
        cfg.LAMBDA_PERF = _extra_args.lambda_perf
    assert args.output_dir != "", "The output dir is empty. Please set up output dir."

    # Initialize output path
    os.makedirs(args.output_dir, exist_ok=True)

    # Load the trained model at phase 1 and 2
    active_power_model = ActivePowerModel().to(device=cfg.DEVICE)
    assert args.active_power_model_weights != "", "The model weights of active power is empty. Please set up path of model weights."
    assert os.path.exists(args.active_power_model_weights), "The model weights path of active power does not exist."
    active_power_model.load_state_dict(torch.load(args.active_power_model_weights, map_location=cfg.DEVICE))
    cpu_io_model = CPUIOModel().to(device=cfg.DEVICE)
    assert args.cpu_io_model_weights != "", "The model weights of cpu io is empty. Please set up path of model weights."
    print("args.cpu_io_model_weights:",args.cpu_io_model_weights)

    assert os.path.exists(args.cpu_io_model_weights), "The model weights path of cpu io does not exist."
    cpu_io_model.load_state_dict(torch.load(args.cpu_io_model_weights, map_location=cfg.DEVICE))

    # Prapare for calculate energy
    set_active_powers(active_power_model)

    # Train and test the model
    model = ParamEffiRainbow().to(device=cfg.DEVICE)
    if args.eval_only:
        assert args.model_weights != "", "The model weights is empty. Please set up path of model weights."
        assert os.path.exists(args.model_weights), "The model weights path does not exist."
        model.load_state_dict(torch.load(args.model_weights, map_location=cfg.DEVICE))
        Trainer.test(model, get_log_file(args), cpu_io_model)
    else:
        if args.model_weights != "":
            assert os.path.exists(args.model_weights), "The model weights path does not exist."
            model.load_state_dict(torch.load(args.model_weights, map_location=cfg.DEVICE))
        criterion = nn.MSELoss().to(device=cfg.DEVICE)
        optimizer = optim.Adam(model.parameters(), lr=cfg.BASE_LR_3, weight_decay=cfg.WEIGHT_DECAY_3)
        lr_scheduler = optim.lr_scheduler.StepLR(optimizer, cfg.STEP_SIZE_3, gamma=cfg.GAMMA_3)
        trainer = Trainer(args, model, criterion, optimizer, lr_scheduler, cpu_io_model)
        trainer.train()
