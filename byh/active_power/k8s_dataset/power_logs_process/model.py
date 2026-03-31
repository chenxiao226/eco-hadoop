import sys
import torch.nn as nn
from torch import Tensor
from typing import List
import default as cfg



class ActivePowerModel(nn.Module):
    def __init__(
        self,
        input_size: int = cfg.INPUT_SIZE_1,
        output_size: int = cfg.OUTPUT_SIZE_1,
        bottleneck_sizes: List[int] = cfg.BOTTLENECK_SIZES_1,
    ) -> None:
        super().__init__()

        bottleneck_sizes.insert(0, input_size)

        self.layers = nn.ModuleList()
        for in_features, out_features in zip(bottleneck_sizes[: -1], bottleneck_sizes[1:]):
            self.layers.append(
                nn.Sequential(
                    nn.Linear(in_features, out_features),
                    nn.BatchNorm1d(out_features),
                    nn.ReLU(inplace=True),
                )
            )
        self.layers.append(
            nn.Sequential(
                nn.Linear(bottleneck_sizes[-1], output_size),
                nn.Sigmoid(),
            )
        )

    def forward(self, x: Tensor) -> Tensor:
        for layer in self.layers:
            x = layer(x)
        return x
