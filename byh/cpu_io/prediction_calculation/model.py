import sys
from torch import Tensor, nn
from typing import List, Tuple

sys.path.append("../..")
import default as cfg


class CPUIOModel(nn.Module):
    def __init__(
        self,
        in_size: int = cfg.INPUT_SIZE_2,
        out_size:int = cfg.OUTPUT_SIZE_2,
        in_channel: int = cfg.INPUT_CHANNEL_2,
        bottleneck_channels: List[int] = cfg.BOTTLENECK_CHANNELS_2,
        fc_sizes: List[int] = cfg.FC_SIZES_2,
    ) -> None:
        super().__init__()

        bottleneck_channels.insert(0, in_channel)

        self.convs = nn.ModuleList()
        for in_c, out_c in zip(bottleneck_channels[: -1], bottleneck_channels[1:]):
            self.convs.append(
                nn.Sequential(
                    nn.Conv1d(in_c, out_c, 3, padding=1),
                    nn.ReLU(inplace=True),
                )
            )
        self.pool = nn.Sequential(
            nn.AvgPool1d(in_size),
            nn.Flatten(),
        )

        fc_sizes.insert(0, bottleneck_channels[-1])
        self.necks1 = nn.ModuleList()
        self.necks2 = nn.ModuleList()
        for in_s, out_s in zip(fc_sizes[: -1], fc_sizes[1:]):
            self.necks1.append(
                nn.Sequential(
                    nn.Linear(in_s, out_s),
                    nn.ReLU(inplace=True),
                )
            )
            self.necks2.append(
                nn.Sequential(
                    nn.Linear(in_s, out_s),
                    nn.ReLU(inplace=True),
                )
            )

        self.fc1 = nn.Sequential(
            nn.Linear(fc_sizes[-1], 64),
            nn.ReLU(inplace=True),
            nn.Linear(64, 1),
            nn.Sigmoid(),
        )
        self.fc2 = nn.Sequential(
            nn.Linear(fc_sizes[-1], 128),
            nn.ReLU(inplace=True),
            nn.Linear(128, out_size),
            nn.ReLU(inplace=True),
        )

    def forward(self, x: Tensor) -> Tuple[Tensor, Tensor]:
        out = x.unsqueeze(1)
        for conv in self.convs:
            out = conv(out)
        out = self.pool(out)

        for i, (neck1, neck2) in enumerate(zip(self.necks1, self.necks2)):
            if i == 0:
                out1 = neck1(out)
                out2 = neck2(out)
            else:
                out1 = neck1(out1)
                out2 = neck2(out2)

        out1 = self.fc1(out1)
        out2 = self.fc2(out2)

        return out1, out2
