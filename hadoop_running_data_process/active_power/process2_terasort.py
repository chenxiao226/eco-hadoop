import os
import re
import csv
import pandas as pd
from tqdm import tqdm

# ── 任务名固定为 pi，本次只处理 slave1 ──────────────────────────────────────
TASK_NAME = 'terasort'
FILE_NAME_LIST = ['slave1']

# 定义要保存的时间戳列表
Timestamp_List = []

def generate_Timestamp_List(task_name):
    """从 time.txt 生成 14 位时间戳列表，逻辑与 process.py 完全一致。"""
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


# ── 核心差异：从 frelog.log 构建 {node: [(time_int, freq), ...]} ─────────────
def load_freq_from_frelog(task_name):
    """
    解析 frelog.log，按节点名称建立有序的 (时间戳int, 频率int) 列表。
    frelog.log 格式示例：
        *******SET 800MHz*******
        2026年03月12日 11时01分58秒 SET slave1 cpufreq To 800MHz
        2026年03月12日 11时01分58秒 SET slave2 cpufreq To 800MHz
    返回：
        { 'slave1': [(20260312110158, 800), (20260312125249, 1300), ...],
          'slave2': [...], ... }
    """
    frelog_path = '../original_data/' + task_name + '/frelog.log'
    node_freq_timeline = {}   # node_name -> list of (ts_int, freq_int)

    current_freq = -1
    with open(frelog_path, 'r', encoding='utf-8') as f:
        for line in f:
            # 识别频率段标题行，例如 ***SET 800MHz***
            header_match = re.search(r'SET\s+(\d+)MHz', line)
            if header_match and 'SET' in line and '年' not in line:
                current_freq = int(header_match.group(1))
                continue

            # 识别具体节点设置行
            # 格式: 2026年03月12日 11时01分58秒 SET slave1 cpufreq To 800MHz
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


def get_freq_at_time(ts_int, freq_timeline):
    """
    给定一个整数时间戳，在有序的 [(ts, freq), ...] 列表中
    找到 <= ts 的最近一次频率设置，返回对应频率；
    若时间戳早于所有设置记录则返回 -1。
    """
    freq = -1
    for set_ts, set_freq in freq_timeline:
        if set_ts <= ts_int:
            freq = set_freq
        else:
            break
    return freq


# ── preprocess_log：与 process.py 逻辑相同，但频率来自 frelog ─────────────────
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
            # 识别时间戳行
            if '秒' in line:
                match = re.search(
                    r'(\d{4})年(\d{2})月(\d{2})日\s+(\d{2})时(\d{2})分(\d{2})秒', line
                )
                if match:
                    time = (
                        f"{match.group(1)}{match.group(2)}{match.group(3)}"
                        f"{match.group(4)}{match.group(5)}{match.group(6)}"
                    )

            # 识别 CPU idle 行（紧跟在含 idle 的表头之后）
            if 'idle' in preline:
                values = line.split()
                if len(values) >= 6:
                    try:
                        cpu_idle = round(float(values[5]) / 100, 4)
                    except (ValueError, IndexError):
                        pass

            # 识别 sda 行，触发写出
            if line.startswith('sda'):
                values = line.split()
                try:
                    sda_idle = round(float(values[-1]) / 100, 4)
                except (ValueError, IndexError):
                    preline = line
                    continue

                if time in Timestamp_List:
                    # 从 frelog 时间线中查找当前频率
                    cpu_freq = get_freq_at_time(int(time), freq_timeline)
                    if cpu_freq != -1:
                        with open(csv_filename, 'a', newline='') as csvfile:
                            writer = csv.writer(csvfile)
                            writer.writerow([time, cpu_freq, cpu_idle, sda_idle])

            preline = line

    print("输出文件保存在：", csv_filename)


# ── preprocess_power：与 process.py 完全一致 ─────────────────────────────────
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
    new_power_df = df_selected[['接收日期时间', '有功功率']]

    print("-----3、开始按照时间戳合并处理后的log文件和功率文件........")
    preprocess_log_df = pd.read_csv(
        './preprocessed_data/' + task_name + '/' + now_file + 'log.csv',
        encoding='gbk'
    )

    for power_col, timestamp in tqdm(
        enumerate(new_power_df['接收日期时间']),
        total=len(new_power_df),
        desc="     合并进度:"
    ):
        prefix = timestamp[:13]
        timestamps = [f"{prefix}{i}" for i in range(10)]
        for t in timestamps:
            if t in Timestamp_List:
                t = str(t).strip()
                matching_col = preprocess_log_df[
                    preprocess_log_df['time'].astype(str).str.strip() == t
                ].index
                if len(matching_col) > 0:
                    preprocess_log_df.loc[matching_col, 'active_power'] = (
                        new_power_df.loc[power_col, '有功功率']
                    )
                    preprocess_log_df.loc[matching_col, 'power_time'] = (
                        new_power_df.loc[power_col, '接收日期时间']
                    )

    file_path = './preprocessed_data/' + task_name + '/log-power-' + now_file + '.csv'
    preprocess_log_df.to_csv(file_path, mode='w', index=False)
    print("-----4、合并文件已经被保存到" + file_path)


# ── 主入口 ───────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    generate_Timestamp_List(TASK_NAME)

    # 从 frelog.log 加载各节点频率时间线（一次性解析，所有节点共用）
    node_freq_timeline = load_freq_from_frelog(TASK_NAME)
    print("已加载 frelog 频率时间线：", {k: v for k, v in node_freq_timeline.items()})

    # frelog 中只记录了 slave 节点；master 与 slave1 频率设置同步，复用其时间线
    slave1_tl = node_freq_timeline.get('slave1', [])
    for file_name in FILE_NAME_LIST:
        freq_tl = node_freq_timeline.get(file_name, slave1_tl)
        preprocess_log(TASK_NAME, file_name, freq_tl)

    for file_name in FILE_NAME_LIST:
        preprocess_power(TASK_NAME, file_name)
