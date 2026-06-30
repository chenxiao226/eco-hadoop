import pandas as pd

# Fix: also drop rows where run_ columns all sum to zero (no samples were collected)
run_cols = ['run_{}'.format(i) for i in range(1, 91)]

for path in [
    'hadoop_running_data_process/cpu_io/preprocessed_data/pi/master/Init_hadoop_runtime_run0_90.csv',
    'byh/cpu_io/dataset/pi/master/Init_hadoop_runtime_run0_90.csv'
]:
    df = pd.read_csv(path, encoding='utf-8')
    row_sums = df[run_cols].sum(axis=1)
    zero_rows = (row_sums == 0).sum()
    print(f'{path}: {len(df)} rows, {zero_rows} rows with zero run_ sum')
    df_clean = df[row_sums > 0].reset_index(drop=True)
    print(f'  -> After cleanup: {len(df_clean)} rows')
    df_clean.to_csv(path, index=False, encoding='utf-8')
    print(f'  Saved.')
