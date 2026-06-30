import os
import re
import csv
import pandas as pd
from tqdm import tqdm

TASK_NAME = 'nnbench'
FILE_NAME_LIST = ['slave1']

Timestamp_List = []

def generate_Timestamp_List(task_name):
    with open('../original_data/' + task_name + '/time.txt', 'r', encoding='utf-8') as f:
        for line in f:
            match = re.search(
                r'(\d{4})年\s*(\d{2})月\s*(\d{2})日\s*(?:星期\w+\s*)?(\d{2})时(\d{2})分(\d{2})秒',
                line
            )
            if match:
                formatted_time = (
                    f"{match.group(1)}{match.group(2)}{match.group(3)}"
                    f"{match.group(4)}{match.group(5)}{match.group(6)}"
                )
                Timestamp_List.append(formatted_time)
    print("提取的日期时间列表：", Timestamp_List)


def load_freq_from_frelog(task_name):
    frelog_path = '../original_data/' + task_name + '/frelog.log'
    node_freq_timeline = {}

    with open(frelog_path, 'r', encoding='utf-8') as f:
        for line in f:
            node_match = re.search(
                r'(\d{4})年(\d{2})月(\d{2})日\s+(\d{2})时(\d{2})分(\d{2})秒\s+SET\s+(\w+)\s+cpufreq\s+To\s+(\d+)MHz',
                line
            )
            if node_match:
                ts = int(
                    f"{node_match.group(1)}{node_match.group(2)}{node_match.group(3)}"
                    f"{node_match.group(4)}{node_match.group(5)}{node_match.group(6)}"
                )
                freq = int(node_match.group(8))
                node_name = node_match.group(7)
                node_freq_timeline.setdefault(node_name, []).append((ts, freq))

    return node_freq_timeline


STANDARD_CPU_FREQS = [800, 1000, 1100, 1300, 1500, 1700, 1800, 2000, 2200, 2300, 2500, 2700, 2900, 3000, 3200]

def snap_freq(freq):
    """将非标准频率映射到最近的标准频率"""
    return min(STANDARD_CPU_FREQS, key=lambda x: abs(x - freq))

def get_freq_at_time(ts_int, freq_timeline):
    freq = -1
    for set_ts, set_freq in freq_timeline:
        if set_ts <= ts_int:
            freq = set_freq
        else:
            break
    return snap_freq(freq) if freq != -1 else -1


def preprocess_log(task_name, now_file, freq_timeline):
    os.makedirs('./preprocessed_data/' + task_name, exist_ok=True)
    csv_filename = './preprocessed_data/' + task_name + '/' + now_file + 'log.csv'

    with open(csv_filename, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(["time", "cpu_frequency", "cpu_usage", "sda_usage"])

    preline = ''
    time = ''
    cpu_idle = ''
    sda_idle = ''

    log_path = '../original_data/' + task_name + '/system_log/' + now_file + '/log.log'
    with open(log_path, 'r', encoding='utf-8') as file:
        total_lines = len(file.readlines())
        file.seek(0)
        for i, line in enumerate(tqdm(file, total=total_lines, desc=now_file + " 预处理进度")):
            if '秒' in line:
                match = re.search(
                    r'(\d{4})年(\d{2})月(\d{2})日\s+(\d{2})时(\d{2})分(\d{2})秒', line
                )
                if match:
                    time = (
                        f"{match.group(1)}{match.group(2)}{match.group(3)}"
                        f"{match.group(4)}{match.group(5)}{match.group(6)}"
                    )

            if 'idle' in preline:
                values = line.split()
                if len(values) >= 6:
                    cpu_idle = round(float(values[5]) / 100, 4)

            if line.startswith('sda'):
                values = line.split()
                try:
                    sda_idle = round(float(values[-1]) / 100, 4)
                except (ValueError, IndexError):
                    preline = line
                    continue

                if time in Timestamp_List:
                    cpu_freq = get_freq_at_time(int(time), freq_timeline)
                    if cpu_freq != -1:
                        with open(csv_filename, 'a', newline='') as csvfile:
                            writer = csv.writer(csvfile)
                            writer.writerow([time, cpu_freq, cpu_idle, sda_idle])

            preline = line

    print("输出文件保存在：", csv_filename)


def preprocess_power(task_name, now_file):
    print("==开始处理" + task_name + "  " + now_file + "功率信息")
    print("-----1、读取time找出起始日期........")

    start_year = start_month = start_day = -1
    with open('../original_data/' + task_name + '/time.txt', 'r', encoding='utf-8') as f:
        first_line = f.readline().strip()
        match = re.search(r'(\d{4})年\s*(\d{2})月\s*(\d{2})日', first_line)
        if match:
            start_year  = match.group(1).zfill(4)
            start_month = match.group(2).zfill(2)
            start_day   = match.group(3).zfill(2)

    print("-----2、读取功率计采集信息表格........")
    df = pd.read_csv(
        '../original_data/' + task_name + '/power_collection/' + now_file + '.csv',
        encoding='gbk'
    )
    df_selected = df[['接收时间', '有功功率']].copy()
    df_selected['接收时间'] = pd.to_datetime(
        df_selected['接收时间'], format='%H:%M:%S'
    ).dt.strftime('%H:%M:%S')

    df_selected.loc[:, '接收日期'] = None
    pre_t = '-1'
    day_add = 0
    for col, now_t in enumerate(df_selected['接收时间']):
        if pre_t[0:2] == '23' and now_t[0:1] == '0':
            day_add += 1
        now_day = int(start_day) + int(day_add)
        df_selected.loc[col, '接收日期'] = str(start_year) + str(start_month) + str(now_day).zfill(2)
        pre_t = now_t

    df_selected.loc[:, '接收日期时间'] = (
        df_selected['接收日期'].astype(str) + df_selected['接收时间'].str.replace(':', '')
    )
    new_power_df = df_selected[['接收日期时间', '有功功率']].reset_index(drop=True)

    print("-----3、开始按照时间戳合并处理后的log文件和功率文件........")
    preprocess_log_df = pd.read_csv(
        './preprocessed_data/' + task_name + '/' + now_file + 'log.csv',
        encoding='gbk'
    )

    # 构建功率查找字典：时间戳 -> 有功功率
    power_dict = dict(zip(new_power_df['接收日期时间'].astype(str), new_power_df['有功功率']))

    # 对 log 中每个时间戳，查找功率；若找不到则用最近一次已知功率填充
    last_known_power = None
    for idx, row in tqdm(preprocess_log_df.iterrows(), total=len(preprocess_log_df), desc="     合并进度:"):
        t = str(row['time']).strip()
        # 精确匹配
        if t in power_dict:
            last_known_power = power_dict[t]
            preprocess_log_df.loc[idx, 'active_power'] = last_known_power
            preprocess_log_df.loc[idx, 'power_time'] = t
        else:
            # 尝试同一秒内的 10 个子时间戳（前13位相同）
            prefix = t[:13]
            found = False
            for i in range(10):
                candidate = f"{prefix}{i}"
                if candidate in power_dict:
                    last_known_power = power_dict[candidate]
                    preprocess_log_df.loc[idx, 'active_power'] = last_known_power
                    preprocess_log_df.loc[idx, 'power_time'] = candidate
                    found = True
                    break
            # 时间戳对不上：用最近一次已知功率填充
            if not found and last_known_power is not None:
                preprocess_log_df.loc[idx, 'active_power'] = last_known_power
                preprocess_log_df.loc[idx, 'power_time'] = t

    file_path = './preprocessed_data/' + task_name + '/log-power-' + now_file + '.csv'
    preprocess_log_df.to_csv(file_path, mode='w', index=False)
    print("-----4、合并文件已经被保存到" + file_path)


if __name__ == "__main__":
    generate_Timestamp_List(TASK_NAME)

    node_freq_timeline = load_freq_from_frelog(TASK_NAME)
    print("已加载 frelog 频率时间线：", {k: v for k, v in node_freq_timeline.items()})

    slave1_tl = node_freq_timeline.get('slave1', [])
    for file_name in FILE_NAME_LIST:
        freq_tl = node_freq_timeline.get(file_name, slave1_tl)
        preprocess_log(TASK_NAME, file_name, freq_tl)

    for file_name in FILE_NAME_LIST:
        preprocess_power(TASK_NAME, file_name)
