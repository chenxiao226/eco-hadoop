import sys
import torch
from torch import Tensor, nn

sys.path.append("..")
import default as cfg
from scaler import transform, inverse_transform


class ParamEffiRainbow(nn.Module):
    # 设置基本结构参数：输入大小、隐藏层大小、层数
    def __init__(
        self,
        input_size: int = cfg.INPUT_SIZE_2,
        hidden_size: int = cfg.HIDDEN_SIZE_3,
        num_layers: int = cfg.NUM_LAYERS_3,
    ) -> None:
        super().__init__()

        self.hidden_size = hidden_size
        self.num_layers = num_layers
        # embd是状态编码器 输入的状态是九个参数 把每一个参数
        # 看成为独立数字用一个小型网络 （全连接层+激活函数）将其翻译为一个高纬度的向量表示
        self.embed = nn.Sequential(
            nn.Linear(1, hidden_size),
            nn.ReLU(inplace=True),
        )
        # gru是核心处理模块 gru是一种循环神经网络，擅长处理序列数据 在这里，他接受了九个参数编码后的序列，
        # 并且输出一个编码后的序列（包含参数之间的关系）
        self.gru = nn.GRU(hidden_size, hidden_size, num_layers, batch_first=True)
        self.inverse_embed = nn.Sequential(
            nn.Linear(hidden_size, 8),
            nn.ReLU(inplace=True),
            nn.Linear(8, 1),
            nn.Sigmoid(),
            nn.Flatten(),
        )

        self.fc = nn.Sequential(
            nn.Linear(input_size, 8),
            nn.ReLU(inplace=True),
            nn.Linear(8, 1),
            nn.Tanh(),
        )

    def forward(self, x: Tensor) -> Tensor:
        h0 = x.new_zeros((self.num_layers, x.size(0), self.hidden_size))

        out = x.unsqueeze(-1)
        out = self.embed(out)
        out, _ = self.gru(out, h0)
        out = self.inverse_embed(out)
        return self.modify_out(out), self.fc(out)

    @torch.no_grad()
    def modify_out(self, out: Tensor):
        modified_out1 = inverse_transform(out[:, [0]], cfg.MIN_PARAM_1, cfg.MAX_PARAM_1).round(decimals=0)
        modified_out1 = transform(modified_out1, cfg.MIN_PARAM_1, cfg.MAX_PARAM_1)
        modified_out2 = inverse_transform(out[:, [1]], cfg.MIN_PARAM_2, cfg.MAX_PARAM_2).round(decimals=2)
        modified_out2 = transform(modified_out2, cfg.MIN_PARAM_2, cfg.MAX_PARAM_2)
        modified_out3 = inverse_transform(out[:, [2]], cfg.MIN_PARAM_3, cfg.MAX_PARAM_3).round(decimals=0)
        modified_out3 = transform(modified_out3, cfg.MIN_PARAM_3, cfg.MAX_PARAM_3)
        modified_out4 = inverse_transform(out[:, [3]], cfg.MIN_PARAM_4, cfg.MAX_PARAM_4).round(decimals=2)
        modified_out4 = transform(modified_out4, cfg.MIN_PARAM_4, cfg.MAX_PARAM_4)
        modified_out5 = inverse_transform(out[:, [4]], cfg.MIN_PARAM_5, cfg.MAX_PARAM_5).round(decimals=2)
        modified_out5 = transform(modified_out5, cfg.MIN_PARAM_5, cfg.MAX_PARAM_5)
        modified_out6 = inverse_transform(out[:, [5]], cfg.MIN_PARAM_6, cfg.MAX_PARAM_6).round(decimals=0)
        modified_out6 = transform(modified_out6, cfg.MIN_PARAM_6, cfg.MAX_PARAM_6)
        modified_out7 = inverse_transform(out[:, [6]], cfg.MIN_PARAM_7, cfg.MAX_PARAM_7).round(decimals=0)
        modified_out7 = transform(modified_out7, cfg.MIN_PARAM_7, cfg.MAX_PARAM_7)
        modified_out8 = inverse_transform(out[:, [7]], cfg.MIN_PARAM_8, cfg.MAX_PARAM_8).round(decimals=0)
        modified_out8 = transform(modified_out8, cfg.MIN_PARAM_8, cfg.MAX_PARAM_8)

        modified_out9 = inverse_transform(out[:, 8], cfg.MIN_CPU_FREQ, cfg.MAX_CPU_FREQ)
        cpu_freqs_ = torch.tensor(cfg.CPU_FREQS, dtype=torch.float32, device=cfg.DEVICE)
        insert_indices = torch.searchsorted(cpu_freqs_, modified_out9)
        modified_out9 = (torch.where(modified_out9 - cpu_freqs_[insert_indices - 1] < cpu_freqs_[insert_indices] - modified_out9, cpu_freqs_[insert_indices - 1], cpu_freqs_[insert_indices])).reshape(-1, 1)
        modified_out9 = transform(modified_out9, cfg.MIN_CPU_FREQ, cfg.MAX_CPU_FREQ)

        modified_out = torch.cat((modified_out1, modified_out2, modified_out3, modified_out4, modified_out5, modified_out6, modified_out7, modified_out8, modified_out9), dim=1)
        return modified_out
