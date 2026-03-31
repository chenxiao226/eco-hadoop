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

from model import ParamEffiRainbow
sys.path.append("..")
import default as cfg
from scaler import transform, inverse_transform
from active_power import ActivePowerModel
from cpu_io import CPUIODataset, CPUIOModel


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
    log_file = open(args.output_dir + "/log.txt", 'a')
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
        run_time_file_path = '../cpu_io/dataset/pagerank/master/Init_hadoop_runtime_run0_90.csv'
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
        # best_energys = energy(best_states, cpu_io_model)
        # best_states_tmp = torch.cat((
        #     inverse_transform(best_states[:, [0]], cfg.MIN_PARAM_1, cfg.MAX_PARAM_1).round(decimals=2),
        #     inverse_transform(best_states[:, [1]], cfg.MIN_PARAM_2, cfg.MAX_PARAM_2).round(decimals=2),
        #     inverse_transform(best_states[:, [2]], cfg.MIN_PARAM_3, cfg.MAX_PARAM_3).round(decimals=2),
        #     inverse_transform(best_states[:, [3]], cfg.MIN_PARAM_4, cfg.MAX_PARAM_4).round(decimals=2),
        #     inverse_transform(best_states[:, [4]], cfg.MIN_PARAM_5, cfg.MAX_PARAM_5).round(decimals=2),
        #     inverse_transform(best_states[:, [5]], cfg.MIN_PARAM_6, cfg.MAX_PARAM_6).round(decimals=2),
        #     inverse_transform(best_states[:, [6]], cfg.MIN_PARAM_7, cfg.MAX_PARAM_7).round(decimals=2),
        #     inverse_transform(best_states[:, [7]], cfg.MIN_PARAM_8, cfg.MAX_PARAM_8).round(decimals=2),
        #     inverse_transform(best_states[:, [8]], cfg.MIN_CPU_FREQ, cfg.MAX_CPU_FREQ).round(decimals=2),
        # ), dim=1)
        # self.log_file.write("The best states: {}\n".format(best_states_tmp))
        # print("The best states: {}".format(best_states_tmp))
        # self.log_file.write("The best energys: {}\n".format(best_energys))
        # print("The best energys: {}".format(best_energys))
        replay_buffer = best_states.new_zeros((0, best_states.size(1)))
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
                batched_states_new, batched_q = self.model(batched_states)
                with torch.no_grad():
                    best_states = self.update_best_states(batched_states, best_states, self.cpu_io_model)
                    batched_trans_energys = transformed_energy(batched_states, self.cpu_io_model)
                    for _ in range(cfg.REFINE_STEPS_3):
                        batched_states_new, batched_q_new = self.model(batched_states_new)
                        best_states = self.update_best_states(batched_states_new, best_states, self.cpu_io_model)
                    batched_trans_energys_new = transformed_energy(batched_states_new, self.cpu_io_model)
                    batched_reward = batched_trans_energys - batched_trans_energys_new
                    batched_target = batched_reward + batched_q_new
                loss = self.criterion(batched_q, batched_target) * 10000
                self.optimizer.zero_grad()
                loss.backward()
                self.optimizer.step()

                cur_iter += 1
                if cur_iter % cfg.WRITE_PERIOD_3 == 0:
                    self.log_file.write("iter: {}, loss: {}\n".format(str(cur_iter).rjust(6), loss.item()))
                    print("iter: {}, loss: {}".format(str(cur_iter).rjust(6), loss.item()))
                writer.add_scalar("train_loss", loss.item(), cur_iter)
            self.lr_scheduler.step()

            if epoch % cfg.EVAL_PERIOD_3 == 0:
                torch.save(self.model.state_dict(), self.output_dir + "/model_{:05}.pth".format(epoch))
                self.log_file.write("Saving checkpoint to " + self.output_dir + "/model_{:05}.pth.\n".format(epoch))
                print("Saving checkpoint to " + self.output_dir + "/model_{:05}.pth.".format(epoch))
                best_energys = energy(best_states, cpu_io_model)
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
        torch.save(self.model.state_dict(), self.output_dir + "/model_final.pth")
        self.log_file.write("Saving checkpoint to " + self.output_dir + "/model_final.pth.\n")
        print("Saving checkpoint to " + self.output_dir + "/model_final.pth.")
        best_energys = energy(best_states, cpu_io_model)
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

        best_energys = energy(best_states, cpu_io_model)
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
    args = cfg.default_argument_parser("param_search").parse_args()
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
