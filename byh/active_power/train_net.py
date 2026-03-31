import os
import pandas as pd
import sys
import torch
import pdb
import argparse
from argparse import Namespace
from io import TextIOWrapper
from matplotlib import pyplot as plt
from torch import nn, optim
from torch.nn import functional as F
from torch.nn.modules.loss import _Loss as Loss
from torch.optim.lr_scheduler import _LRScheduler
from torch.optim.optimizer import Optimizer
from torch.utils.data import DataLoader
from torch.utils.tensorboard import SummaryWriter
from sklearn.model_selection import train_test_split

from data import ActivePowerDataset
from model import ActivePowerModel
# naw
import default as cfg
from scaler import inverse_transform
#------------------------------------------------------------------------------
print("✅ 脚本文件被加载了")


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
    log_file.write("  DATA_DIR: \"" + cfg.DATA_DIR + "\"\n")
    log_file.write("  CPU_FREQ: " + str(cfg.CPU_FREQS) + "\n")
    log_file.write("MODEL:\n")
    log_file.write("  INPUT_SIZE: " + str(cfg.INPUT_SIZE_1) + "\n")
    log_file.write("  OUTPUT_SIZE: " + str(cfg.OUTPUT_SIZE_1) + "\n")
    log_file.write("  BOTTLENECK_SIZES: " + str(cfg.BOTTLENECK_SIZES_1) + "\n")
    if args.eval_only:
        log_file.write("  WEIGHTS: \"" + args.model_weights + "\"\n")
    log_file.write("SOLVER:\n")
    log_file.write("  BATCH_SIZE: " + str(cfg.BATCH_SIZE_1) + "\n")
    log_file.write("  BASE_LR: " + str(cfg.BASE_LR_1) + "\n")
    log_file.write("  WEIGHT_DECAY: " + str(cfg.WEIGHT_DECAY_1) + "\n")
    log_file.write("  STEP_SIZE: " + str(cfg.STEP_SIZE_1) + "\n")
    log_file.write("  GAMMA: " + str(cfg.GAMMA_1) + "\n")
    log_file.write("  NUM_EPOCHS: " + str(cfg.NUM_EPOCHS_1) + "\n")
    log_file.write("  WRITE_PERIOD: " + str(cfg.WRITE_PERIOD_1) + "\n")
    log_file.write("TEST:\n")
    log_file.write("  EVAL_PERIOD: " + str(cfg.EVAL_PERIOD_1) + "\n")
    return log_file


class Trainer:
    def __init__(
        self,
        args: Namespace,
        model: ActivePowerModel,
        criterion: Loss,
        optimizer: Optimizer,
        lr_scheduler: _LRScheduler,
    ) -> None:
        #self.data_loader,self = self.build_train_loader()
        self.data_loader,_ = self.build_data_loader()
        self.model = model
        self.criterion = criterion
        self.optimizer = optimizer
        self.lr_scheduler = lr_scheduler

        self.output_dir = args.output_dir
        self.log_file = get_log_file(args)

    def build_data_loader(self):
        # 假设你已经读取了 CSV 数据
        #csv_path = cfg.DATA_DIR + "/" + TASK_NAME + "/log-power-" + FILE_NAME + ".csv"
        csv_path = args.CSV_PATH

        data_frame = pd.read_csv(csv_path, encoding='utf-8')

        # 分割数据：将数据分为 80% 训练集和 20% 测试集
        train_data, test_data = train_test_split(data_frame, test_size=0.2, random_state=42)

        # 打印数据大小
        print(f"训练集大小: {train_data.shape}")
        print(f"测试集大小: {test_data.shape}")

        # 如果需要传递给 DataLoader，可以进一步处理为自定义 Dataset
        train_dataset = ActivePowerDataset(train_data)
        test_dataset = ActivePowerDataset(test_data)

        # 生成 DataLoader
        train_loader = DataLoader(dataset=train_dataset, batch_size=cfg.BATCH_SIZE_1, shuffle=True)
        test_loader = DataLoader(dataset=test_dataset, batch_size=cfg.BATCH_SIZE_1, shuffle=False)
        return train_loader,test_loader

    def train(self) -> None:
        # Add TensorBoard
        writer = SummaryWriter(self.output_dir)
        self.log_file.write("Starting training from iteration 0.\n")

        cur_iter = 0
        for epoch in range(1, args.NUM_EPOCHS_1 + 1):
            self.model.train()

            self.optimizer.zero_grad()
            for batch_features, batch_targets in self.data_loader:
                batch_outputs = self.model(batch_features)
                #print("batch_outputs:",batch_outputs)
                #print("batch_targets:",batch_targets)

                #pdb.set_trace()  # 设置断点，执行到这里时会暂停

                loss = self.criterion(batch_outputs, batch_targets)
                loss.backward()

                cur_iter += 1
                if cur_iter % cfg.WRITE_PERIOD_1 == 0:
                    self.log_file.write("epoch: {} , iter: {}, loss: {}\n".format(str(epoch).rjust(6),str(cur_iter).rjust(6), loss.item()))
                    print("epoch: {}, iter: {}, loss: {}".format(str(epoch).rjust(6),str(cur_iter).rjust(6), loss.item()))
                writer.add_scalar("train_loss", loss.item(), cur_iter)
            self.optimizer.step()
            self.lr_scheduler.step()


            if epoch % cfg.EVAL_PERIOD_1 == 0:
                #print("self.log_file:",self.log_file)
                temp_save_path = self.output_dir
                if not os.path.exists(temp_save_path):
                    os.makedirs(temp_save_path)
                torch.save(self.model.state_dict(), temp_save_path+"/model_{:05}.pth".format(epoch))
                self.log_file.write("Saving checkpoint to " + temp_save_path+ "/model_{:05}.pth.\n".format(epoch))
                print("Saving checkpoint to " + temp_save_path + "/model_{:05}.pth.".format(epoch))
                self.test(args,self.model, self.log_file)
        final_save_path = self.output_dir
        torch.save(self.model.state_dict(), final_save_path + "/model_final.pth")
        self.log_file.write("Saving checkpoint to " + final_save_path + "/model_final.pth.\n")
        print("Saving checkpoint to " + final_save_path + "/model_final.pth.")
        self.test(args,self.model, self.log_file)

    #@classmethod
    @torch.no_grad()
    def test(
        self,
        args: Namespace,
        model: ActivePowerModel,
        log_file: TextIOWrapper,
    ) -> None:
        #data_loader = self.build_test_loader()
        _,data_loader = self.build_data_loader()
        log_file.write("Start inference on {} batches:\n".format(len(data_loader)))
        print("Start inference on {} batches:".format(len(data_loader)))

        model.eval()

        total_num = 0
        all_inverse_outputs = torch.zeros((0, 1), device=cfg.DEVICE)
        all_inverse_targets = torch.zeros((0, 1), device=cfg.DEVICE)
        total_mse_loss = torch.tensor(0.0, device=cfg.DEVICE)
        total_bce_loss = torch.tensor(0.0, device=cfg.DEVICE)
        total_relative_error = torch.tensor(0.0, device=cfg.DEVICE)
        for i, (batch_features, batch_targets) in enumerate(data_loader):
            batch_outputs = model(batch_features)
            total_num += batch_features.size(0)
            total_mse_loss += F.mse_loss(batch_outputs, batch_targets, reduction="sum")
            total_bce_loss += F.binary_cross_entropy(batch_outputs, batch_targets, reduction="sum")

            inverse_batch_outputs = inverse_transform(batch_outputs, cfg.MIN_ACTIVE_POWER, cfg.MAX_ACTIVE_POWER)
            inverse_batch_targets = inverse_transform(batch_targets, cfg.MIN_ACTIVE_POWER, cfg.MAX_ACTIVE_POWER)
            all_inverse_outputs = torch.cat((all_inverse_outputs, inverse_batch_outputs), dim=0)
            all_inverse_targets = torch.cat((all_inverse_targets, inverse_batch_targets), dim=0)
            total_relative_error += ((inverse_batch_outputs - inverse_batch_targets) / inverse_batch_targets).abs().sum()
            if (i + 1) % 5 == 0:
                log_file.write("Inference done {}/{}.\n".format(i + 1, len(data_loader)))
                print("Inference done {}/{}.".format(i + 1, len(data_loader)))
        log_file.write("Finish inference on {} data.\n".format(total_num))
        print("Finish inference on {} data.".format(total_num))

        log_file.write("mse_loss: {}, bce_loss: {}, relative_error: {}\n".format(
            total_mse_loss.item() / total_num, total_bce_loss / total_num, total_relative_error / total_num
        ))
        print("mse_loss: {}, bce_loss: {}, relative_error: {}".format(
            total_mse_loss.item() / total_num, total_bce_loss / total_num, total_relative_error / total_num
        ))

        all_inverse_outputs = all_inverse_outputs.flatten().cpu().numpy()[426: 626]
        all_inverse_targets = all_inverse_targets.flatten().cpu().numpy()[426: 626]
        plt.figure(figsize=(15, 6))
        plt.plot(range(len(all_inverse_outputs)), all_inverse_outputs, linewidth=1, c='red', label="Predict Power")
        plt.plot(range(len(all_inverse_targets)), all_inverse_targets, linewidth=1, c='green', label="Actual Power")
        plt.legend()
        plt.xticks([])
        plt.xlabel("Samples")
        plt.ylabel("Power (W)")
        plt.savefig(args.output_dir + "/vis_model_final.jpg")
        plt.close()


if __name__ == '__main__':
    # 初始化 ArgumentParser
    print("主程序")
    parser = argparse.ArgumentParser(description="Example script to read file_name argument")
    # 添加参数
    parser.add_argument("--CSV_PATH",       type=str, required=True)
    parser.add_argument("--NUM_EPOCHS_1",   type=int, required=True)
    parser.add_argument("--TASK_NAME",      type=str, required=True)
    parser.add_argument("--FILE_NAME",      type=str, required=True)
    # 解析参数
    args = parser.parse_args()

    args.output_dir = "./output/"+   args.TASK_NAME + "/" +   args.FILE_NAME


    # Initialize output path
    os.makedirs(args.output_dir, exist_ok=True)

    # Train and test the model
    model = ActivePowerModel().to(device=cfg.DEVICE)

    args.eval_only = False
    if args.eval_only:
        print("进入test模式...")
        assert args.model_weights != "", "The model weights is empty. Please set up path of model weights."
        assert os.path.exists(args.model_weights), "The model weights path does not exist."
        model.load_state_dict(torch.load(args.model_weights, map_location=cfg.DEVICE))
        Trainer.test(args, model, get_log_file(args))
    else:
        print("进入train模式...")
        #criterion = nn.BCELoss().to(device=cfg.DEVICE)

        criterion = nn.MSELoss().to(device=cfg.DEVICE)    # 使用均方误差作为损失函数
        optimizer = optim.Adam(model.parameters(), lr=cfg.BASE_LR_1, weight_decay=cfg.WEIGHT_DECAY_1)
        lr_scheduler = optim.lr_scheduler.StepLR(optimizer, cfg.STEP_SIZE_1, gamma=cfg.GAMMA_1)
        trainer = Trainer(args, model, criterion, optimizer, lr_scheduler)
        trainer.train()
