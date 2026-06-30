import pandas as pd
import torch

df = pd.read_csv('byh/cpu_io/dataset/pi/master/Init_hadoop_runtime_run0_90.csv', encoding='utf-8')
print('Shape:', df.shape)
print('NaN run_time:', df['run_time'].isna().sum())

rt = torch.tensor(df['run_time'].values, dtype=torch.float32)
rt_max = rt.max()
rt_min = rt.min()
print('rt_max:', rt_max.item(), 'rt_min:', rt_min.item())
# inverted: min_data=max, max_data=min -> (rt - max)/(min - max)
train_y1 = (rt - rt_max) / (rt_min - rt_max)
print('train_y1 min:', train_y1.min().item(), 'max:', train_y1.max().item())
print('Out of range:', ((train_y1 < 0) | (train_y1 > 1)).sum().item())

# Also check run_ columns for any issues
run_cols = ['run_{}'.format(i) for i in range(1, 91)]
run_data = torch.tensor(df[run_cols].values, dtype=torch.float32)
row_sums = run_data.sum(dim=1)
print('run_ row sums: min={:.2f}, max={:.2f}'.format(row_sums.min().item(), row_sums.max().item()))
# check for zero sums
zero_sum_count = (row_sums == 0).sum().item()
print('Rows with zero sum in run_:', zero_sum_count)
if zero_sum_count > 0:
    print('Zero sum rows:', (row_sums == 0).nonzero().flatten().tolist()[:10])
non_zero_mask = row_sums > 0
ratios = run_data[non_zero_mask] / row_sums[non_zero_mask].unsqueeze(1)
print('ratios (non-zero rows) min:', ratios.min().item(), 'max:', ratios.max().item())
print('Any NaN in ratios:', ratios.isnan().any().item())
