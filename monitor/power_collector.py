"""
功率数据采集脚本
运行环境：收集功率数据的 Windows 电脑
功能：读取功率计导出的 CSV 文件，将有功功率数据写入 MySQL

依赖安装：
    pip install pymysql

使用方法：
    1. 修改下方配置区的文件路径和数据库连接信息
    2. 每次功率计导出 CSV 后，运行此脚本：
       python power_collector.py
"""

import csv
import pymysql
from datetime import datetime, date

# ============================================================
# 配置区 - 根据实际情况修改
# ============================================================

# 三个 CSV 文件的完整路径
CSV_FILES = {
    'master': r'D:\master.csv',
    'slave1': r'D:\slave1.csv',
    'slave2': r'D:\slave2.csv',
}

# MySQL 连接信息（本机数据库）
DB_CONFIG = {
    'host':     'localhost',
    'port':     3306,
    'user':     'root',
    'password': 'root',
    'database': 'bd_monitor',
    'charset':  'utf8mb4',
}

# CSV 文件编码（如果乱码改成 'gbk' 或 'gb2312'）
CSV_ENCODING = 'gbk'

# 对应数据库表名
TABLE_MAP = {
    'master': 'p_master_power',
    'slave1': 'p_slave1_power',
    'slave2': 'p_slave2_power',
}

# ============================================================
# 核心逻辑
# ============================================================

def parse_process_time(time_str):
    """
    将 CSV 中的时间字符串转为 Unix 时间戳（秒）
    CSV 中只有时间没有日期，用今天的日期补全
    """
    today = date.today()
    dt = datetime.strptime(f"{today} {time_str.strip()}", "%Y-%m-%d %H:%M:%S")
    return int(dt.timestamp())


def read_csv(filepath):
    """读取 CSV，返回 (process_time, power) 列表"""
    records = []
    try:
        with open(filepath, 'r', encoding=CSV_ENCODING) as f:
            reader = csv.DictReader(f, delimiter='\t')
            for row in reader:
                try:
                    time_str = row['接收时间'].strip()
                    power = float(row['有功功率'].strip())
                    process_time = parse_process_time(time_str)
                    records.append((process_time, power))
                except (KeyError, ValueError) as e:
                    print(f"  跳过无效行: {row} -> {e}")
    except FileNotFoundError:
        print(f"  文件不存在: {filepath}")
    except Exception as e:
        print(f"  读取文件出错: {filepath} -> {e}")
    return records


def ensure_table(cursor, table_name):
    """确保表存在，不存在则创建"""
    cursor.execute(f"""
        CREATE TABLE IF NOT EXISTS `{table_name}` (
            `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
            `sum` DOUBLE NOT NULL,
            `num` INT DEFAULT 1,
            `process_time` BIGINT NOT NULL,
            INDEX idx_process_time (`process_time`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)


def get_existing_times(cursor, table_name):
    """获取表中已有的所有 process_time，避免重复插入"""
    cursor.execute(f"SELECT process_time FROM `{table_name}`")
    return set(row[0] for row in cursor.fetchall())


def insert_records(cursor, table_name, records, existing_times):
    """插入新数据，跳过已存在的时间戳"""
    new_records = [(pt, pw) for pt, pw in records if pt not in existing_times]
    if not new_records:
        print(f"  无新数据需要插入")
        return 0
    cursor.executemany(
        f"INSERT INTO `{table_name}` (sum, num, process_time) VALUES (%s, 1, %s)",
        [(pw, pt) for pt, pw in new_records]
    )
    return len(new_records)


def main():
    print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] 开始导入功率数据...")

    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
    except Exception as e:
        print(f"数据库连接失败: {e}")
        print("请检查 DB_CONFIG 中的 host/user/password 是否正确")
        return

    total_inserted = 0

    for node, filepath in CSV_FILES.items():
        table = TABLE_MAP[node]
        print(f"\n处理 {node} -> {filepath}")

        records = read_csv(filepath)
        if not records:
            print(f"  没有读取到数据，跳过")
            continue

        print(f"  读取到 {len(records)} 条记录")

        ensure_table(cursor, table)
        existing_times = get_existing_times(cursor, table)
        inserted = insert_records(cursor, table, records, existing_times)
        conn.commit()

        print(f"  成功插入 {inserted} 条新记录")
        total_inserted += inserted

    cursor.close()
    conn.close()

    print(f"\n完成！共插入 {total_inserted} 条记录")


if __name__ == '__main__':
    main()
