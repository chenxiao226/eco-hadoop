
"""
集群实时数据采集脚本
每5秒采集一次，写入 MySQL bd_monitor 数据库

依赖安装：
    pip install paramiko pymysql requests

运行：
    python cluster_collector.py
"""

import time
import pymysql
import paramiko
import requests
from datetime import datetime

# ============================================================
# 配置
# ============================================================

NODES = {
    'master': '10.168.1.101',
    'slave1': '10.168.1.100',
    'slave2': '10.168.1.103',
}
SSH_USER = 'hadoop'
SSH_PASS = 'hadoop'
SSH_PORT = 22

NAMENODE_API = 'http://10.168.1.101:9870'
YARN_API     = 'http://10.168.1.101:8088'

DB_CONFIG = {
    'host':     '10.168.1.102',
    'port':     3306,
    'user':     'root',
    'password': 'root',
    'database': 'bd_monitor',
    'charset':  'utf8mb4',
}

INTERVAL = 5  # 采集间隔（秒）

# ============================================================
# 数据库
# ============================================================

def get_conn():
    return pymysql.connect(**DB_CONFIG)

def insert(cursor, table, sum_val, num_val, process_time):
    cursor.execute(
        f"INSERT INTO `{table}` (sum, num, process_time) VALUES (%s, %s, %s)",
        (sum_val, num_val, process_time)
    )

def ensure_tables(cursor):
    tables = [
        'm_cpu_idle', 'm_cpu_user', 'm_cpu_system', 'm_cpu_wio',
        'm_cpu_num', 'm_load_one',
        'm_disk_free', 'm_disk_total',
        'm_disk_read_bytes', 'm_disk_write_bytes',
        'm_network_bytes_in', 'm_network_bytes_out',
        'm_network_pkts_in', 'm_network_pkts_out',
        'm_rpc_rpc_rpcprocessingtimeavgtime',
        'm_proc_run', 'm_proc_total',
        # namenode
        'm_jvm_jvmmetrics_gctimemillis',
        'm_jvm_jvmmetrics_memheapusedm',
        'm_jvm_jvmmetrics_memheapcommittedm',
        'm_dfs_fsnamesystemstate_capacitytotal',
        'm_dfs_fsnamesystemstate_capacityused',
        'm_dfs_fsnamesystemstate_capacityremaining',
        # memory
        'm_memory_free', 'm_memory_total',
        # datanode
        'm_dfs_datanode_bytesread',
        'm_dfs_datanode_byteswritten',
        'm_dfs_datanode_blocksread',
        'm_dfs_datanode_blockwritten',
        'm_dfs_datanode_readblockopavgtime',
        'm_dfs_datanode_writeblockopavgtime',
    ]
    for t in tables:
        cursor.execute(f"""
            CREATE TABLE IF NOT EXISTS `{t}` (
                `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                `sum` DOUBLE NOT NULL,
                `num` INT DEFAULT 1,
                `process_time` BIGINT NOT NULL,
                INDEX idx_pt (`process_time`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """)

# ============================================================
# SSH 采集
# ============================================================

ssh_clients = {}
prev_stats = {}  # 保存每个节点上次的 /proc/stat 原始值
prev_diskstats = {}  # 保存每个节点上次的 /proc/diskstats 原始值
prev_netstats = {}  # 保存每个节点上次的 /proc/net/dev 原始值

def get_ssh(node):
    if node not in ssh_clients or not ssh_clients[node].get_transport() or \
       not ssh_clients[node].get_transport().is_active():
        client = paramiko.SSHClient()
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        client.connect(NODES[node], port=SSH_PORT, username=SSH_USER, password=SSH_PASS, timeout=5)
        ssh_clients[node] = client
    return ssh_clients[node]

def ssh_exec(node, cmd):
    try:
        client = get_ssh(node)
        _, stdout, _ = client.exec_command(cmd, timeout=5)
        return stdout.read().decode().strip()
    except Exception as e:
        print(f"  SSH {node} 失败: {e}")
        return None

def collect_cpu_load(node):
    """返回 (cpu_idle_pct, cpu_user_pct, cpu_system_pct, cpu_wio_pct, cpu_num, load_one) 或 None"""
    out = ssh_exec(node, "cat /proc/stat | head -1")
    if not out:
        return None
    parts = out.split()
    # cpu user nice system idle iowait irq softirq
    vals = [int(x) for x in parts[1:]]

    if node in prev_stats:
        prev = prev_stats[node]
        diff = [vals[i] - prev[i] for i in range(len(vals))]
        total = sum(diff)
        if total <= 0:
            return None
        cpu_idle_pct   = diff[3] / total * 100
        cpu_user_pct   = diff[0] / total * 100
        cpu_system_pct = diff[2] / total * 100
        cpu_wio_pct    = diff[4] / total * 100
    else:
        # 第一次采样，没有上次数据，跳过
        prev_stats[node] = vals
        return None

    prev_stats[node] = vals

    cpu_num = ssh_exec(node, "nproc")
    cpu_num = int(cpu_num) if cpu_num else 1

    load_out = ssh_exec(node, "cat /proc/loadavg")
    load_one = float(load_out.split()[0]) if load_out else 0.0

    return cpu_idle_pct, cpu_user_pct, cpu_system_pct, cpu_wio_pct, cpu_num, load_one

def collect_memory(node):
    """返回 (mem_free_mb, mem_total_mb, buffers_mb, cached_mb, shared_mb, swap_free_mb) 或 None"""
    out = ssh_exec(node, "cat /proc/meminfo")
    if not out:
        return None
    info = {}
    for line in out.splitlines():
        parts = line.split()
        if len(parts) >= 2:
            info[parts[0].rstrip(':')] = float(parts[1]) / 1024  # kB -> MB
    mem_free   = info.get('MemAvailable', info.get('MemFree', 0))
    mem_total  = info.get('MemTotal', 0)
    buffers    = info.get('Buffers', 0)
    cached     = info.get('Cached', 0)
    shared     = info.get('Shmem', 0)
    swap_free  = info.get('SwapFree', 0)
    return mem_free, mem_total, buffers, cached, shared, swap_free

def collect_proc(node):
    """返回 (proc_run, proc_total) 或 None"""
    out = ssh_exec(node, "ps aux | wc -l")
    if not out:
        return None
    proc_total = int(out) - 1  # 减去标题行
    out2 = ssh_exec(node, "ps -eo stat | grep -c '^R'")
    proc_run = int(out2) if out2 and out2.isdigit() else 0
    return proc_run, proc_total

def collect_disk_io(node):
    """返回 (read_bytes_per_sec, write_bytes_per_sec) 或 None"""
    out = ssh_exec(node, "cat /proc/diskstats")
    if not out:
        return None
    # /proc/diskstats 格式: major minor name reads ... sectors_read ... writes ... sectors_written ...
    # 字段3=reads, 字段6=sectors_read, 字段7=writes, 字段10=sectors_written
    # 扇区大小通常512字节
    total_read_sectors = 0
    total_write_sectors = 0
    for line in out.splitlines():
        parts = line.split()
        if len(parts) < 14:
            continue
        dev_name = parts[2]
        # 只统计物理磁盘，跳过 loop、ram、sr 设备和分区（末尾是数字的）
        if dev_name.startswith(('loop', 'ram', 'sr')):
            continue
        if dev_name[-1].isdigit():
            continue
            continue
        try:
            total_read_sectors += int(parts[5])   # 字段6: sectors read
            total_write_sectors += int(parts[9])  # 字段10: sectors written
        except:
            continue

    key = f"{node}_diskstats"
    if key in prev_diskstats:
        prev_read, prev_write = prev_diskstats[key]
        delta_read = total_read_sectors - prev_read
        delta_write = total_write_sectors - prev_write
        # 扇区大小512字节，除以采集间隔得到 bytes/s
        read_bps = (delta_read * 512) / INTERVAL
        write_bps = (delta_write * 512) / INTERVAL
        prev_diskstats[key] = (total_read_sectors, total_write_sectors)
        return read_bps, write_bps
    else:
        prev_diskstats[key] = (total_read_sectors, total_write_sectors)
        return None

def collect_network_io(node):
    """返回 (bytes_in_per_sec, bytes_out_per_sec, pkts_in_per_sec, pkts_out_per_sec) 或 None"""
    out = ssh_exec(node, "cat /proc/net/dev")
    if not out:
        return None
    # /proc/net/dev 格式: interface: bytes packets ...
    # 字段1=recv_bytes, 字段2=recv_packets, 字段9=transmit_bytes, 字段10=transmit_packets
    total_recv_bytes = 0
    total_send_bytes = 0
    total_recv_pkts = 0
    total_send_pkts = 0
    for line in out.splitlines():
        if ':' not in line:
            continue
        parts = line.split(':')
        if len(parts) < 2:
            continue
        iface = parts[0].strip()
        # 跳过 lo 回环接口
        if iface == 'lo':
            continue
        fields = parts[1].split()
        if len(fields) < 10:
            continue
        try:
            total_recv_bytes += int(fields[0])
            total_recv_pkts  += int(fields[1])
            total_send_bytes += int(fields[8])
            total_send_pkts  += int(fields[9])
        except:
            continue

    key = f"{node}_netstats"
    if key in prev_netstats:
        prev_recv_bytes, prev_send_bytes, prev_recv_pkts, prev_send_pkts = prev_netstats[key]
        delta_recv_bytes = total_recv_bytes - prev_recv_bytes
        delta_send_bytes = total_send_bytes - prev_send_bytes
        delta_recv_pkts  = total_recv_pkts - prev_recv_pkts
        delta_send_pkts  = total_send_pkts - prev_send_pkts
        # 除以采集间隔得到速率
        recv_bps = delta_recv_bytes / INTERVAL
        send_bps = delta_send_bytes / INTERVAL
        recv_pps = delta_recv_pkts / INTERVAL
        send_pps = delta_send_pkts / INTERVAL
        prev_netstats[key] = (total_recv_bytes, total_send_bytes, total_recv_pkts, total_send_pkts)
        return recv_bps, send_bps, recv_pps, send_pps
    else:
        prev_netstats[key] = (total_recv_bytes, total_send_bytes, total_recv_pkts, total_send_pkts)
        return None

# ============================================================
# REST API 采集
# ============================================================

def collect_hdfs():
    """返回 (capacity_total_gb, capacity_used_gb, capacity_remaining_gb) 或 None"""
    try:
        r = requests.get(f"{NAMENODE_API}/jmx?qry=Hadoop:service=NameNode,name=FSNamesystemState", timeout=5)
        data = r.json()['beans'][0]
        total     = data['CapacityTotal'] / (1024**3)
        used      = data['CapacityUsed'] / (1024**3)
        remaining = data['CapacityRemaining'] / (1024**3)
        return total, used, remaining
    except Exception as e:
        print(f"  HDFS API 失败: {e}")
        return None

def collect_rpc():
    """返回 RPC 平均处理时间(ms) 或 None"""
    try:
        r = requests.get(f"{NAMENODE_API}/jmx", timeout=5)
        beans = r.json().get('beans', [])
        d = next((b for b in beans if 'RpcActivity' in b.get('name', '')), None)
        if not d:
            print(f"  RPC API 失败: no RpcActivity bean found")
            return None
        return d.get('RpcProcessingTimeAvgTime', 0.0)
    except Exception as e:
        print(f"  RPC API 失败: {e}")
        return None
        print(f"  RPC API 失败: {e}")
        return None

def collect_namenode_jvm():
    """返回 (gc_time_ms, heap_used_mb, heap_committed_mb) 或 None"""
    try:
        r = requests.get(f"{NAMENODE_API}/jmx?qry=Hadoop:service=NameNode,name=JvmMetrics", timeout=5)
        data = r.json()['beans'][0]
        return (
            data.get('GcTimeMillis', 0),
            data.get('MemHeapUsedM', 0),
            data.get('MemHeapCommittedM', 0),
        )
    except Exception as e:
        print(f"  JVM API 失败: {e}")
        return None

DATANODE_PORTS = {
    'slave1': 'http://10.168.1.100:9864',
    'slave2': 'http://10.168.1.103:9864',
}

def collect_datanode_jvm():
    """聚合所有DataNode的JMX指标，返回 (bytes_read, bytes_written, blocks_read, blocks_written, read_avg_ms, write_avg_ms) 或 None"""
    total_bytes_read     = 0.0
    total_bytes_written  = 0.0
    total_blocks_read    = 0.0
    total_blocks_written = 0.0
    total_read_avg       = 0.0
    total_write_avg      = 0.0
    count = 0
    for node, url in DATANODE_PORTS.items():
        try:
            r = requests.get(f"{url}/jmx", timeout=5)
            beans = r.json().get('beans', [])
            # 找到名称包含 DataNodeActivity 的 bean
            d = next((b for b in beans if 'DataNodeActivity' in b.get('name', '')), None)
            if not d:
                print(f"  DataNode {node}: no DataNodeActivity bean found")
                continue
            total_bytes_read     += d.get('BytesRead', 0)
            total_bytes_written  += d.get('BytesWritten', 0)
            total_blocks_read    += d.get('BlocksRead', 0)
            total_blocks_written += d.get('BlocksWritten', 0)
            total_read_avg       += d.get('ReadBlockOpAvgTime', 0)
            total_write_avg      += d.get('WriteBlockOpAvgTime', 0)
            count += 1
        except Exception as e:
            print(f"  DataNode {node} JMX 失败: {e}")
    if count == 0:
        return None
    return (
        total_bytes_read,
        total_bytes_written,
        total_blocks_read,
        total_blocks_written,
        total_read_avg / count,
        total_write_avg / count,
    )

# ============================================================
# 主循环
# ============================================================

def collect_once(cursor, ts):
    # 聚合3个节点的 CPU idle / num / load
    total_idle   = 0.0
    total_user   = 0.0
    total_system = 0.0
    total_wio    = 0.0
    total_num    = 0
    total_load   = 0.0
    node_count   = 0

    total_mem_free   = 0.0
    total_mem_total  = 0.0
    total_buffers    = 0.0
    total_cached     = 0.0
    total_shared     = 0.0
    total_swap_free  = 0.0

    for node in NODES:
        result = collect_cpu_load(node)
        if result:
            cpu_idle, cpu_user, cpu_system, cpu_wio, cpu_num, load_one = result
            total_idle   += cpu_idle
            total_user   += cpu_user
            total_system += cpu_system
            total_wio    += cpu_wio
            total_num    += cpu_num
            total_load   += load_one
            node_count   += 1

        mem = collect_memory(node)
        if mem:
            total_mem_free   += mem[0]
            total_mem_total  += mem[1]
            total_buffers    += mem[2]
            total_cached     += mem[3]
            total_shared     += mem[4]
            total_swap_free  += mem[5]

    total_proc_run   = 0
    total_proc_total = 0
    proc_node_count  = 0
    for node in NODES:
        proc = collect_proc(node)
        if proc:
            total_proc_run   += proc[0]
            total_proc_total += proc[1]
            proc_node_count  += 1

    if proc_node_count > 0:
        insert(cursor, 'm_proc_run',   total_proc_run,   1, ts)
        insert(cursor, 'm_proc_total', total_proc_total, 1, ts)

    # Disk I/O
    total_read_bps  = 0.0
    total_write_bps = 0.0
    disk_node_count = 0
    for node in NODES:
        disk_io = collect_disk_io(node)
        if disk_io:
            total_read_bps  += disk_io[0]
            total_write_bps += disk_io[1]
            disk_node_count += 1

    if disk_node_count > 0:
        insert(cursor, 'm_disk_read_bytes',  total_read_bps,  1, ts)
        insert(cursor, 'm_disk_write_bytes', total_write_bps, 1, ts)

    # Network I/O
    total_recv_bps = 0.0
    total_send_bps = 0.0
    total_recv_pps = 0.0
    total_send_pps = 0.0
    net_node_count = 0
    for node in NODES:
        net_io = collect_network_io(node)
        if net_io:
            total_recv_bps += net_io[0]
            total_send_bps += net_io[1]
            total_recv_pps += net_io[2]
            total_send_pps += net_io[3]
            net_node_count += 1

    if net_node_count > 0:
        insert(cursor, 'm_network_bytes_in',  total_recv_bps, 1, ts)
        insert(cursor, 'm_network_bytes_out', total_send_bps, 1, ts)
        insert(cursor, 'm_network_pkts_in',   total_recv_pps, 1, ts)
        insert(cursor, 'm_network_pkts_out',  total_send_pps, 1, ts)

    if node_count > 0:
        insert(cursor, 'm_cpu_idle',   total_idle,                node_count, ts)
        insert(cursor, 'm_cpu_user',   total_user,                node_count, ts)
        insert(cursor, 'm_cpu_system', total_system,              node_count, ts)
        insert(cursor, 'm_cpu_wio',    total_wio,                 node_count, ts)
        insert(cursor, 'm_cpu_num',    total_num,                 1,          ts)
        insert(cursor, 'm_load_one',   total_load / node_count,   1,          ts)

    if total_mem_total > 0:
        insert(cursor, 'm_mem_free',     total_mem_free,  1, ts)
        insert(cursor, 'm_memory_free',  total_mem_free,  1, ts)
        insert(cursor, 'm_memory_total', total_mem_total, 1, ts)
        insert(cursor, 'm_mem_total',    total_mem_total, 1, ts)
        insert(cursor, 'm_mem_buffers',  total_buffers,   1, ts)
        insert(cursor, 'm_mem_cached',   total_cached,    1, ts)
        insert(cursor, 'm_mem_shared',   total_shared,    1, ts)
        insert(cursor, 'm_swap_free',    total_swap_free, 1, ts)

    # HDFS
    hdfs = collect_hdfs()
    if hdfs:
        insert(cursor, 'm_disk_total',                        hdfs[0], 1, ts)
        insert(cursor, 'm_disk_free',                         hdfs[2], 1, ts)
        insert(cursor, 'm_dfs_fsnamesystemstate_capacitytotal',    hdfs[0], 1, ts)
        insert(cursor, 'm_dfs_fsnamesystemstate_capacityused',     hdfs[1], 1, ts)
        insert(cursor, 'm_dfs_fsnamesystemstate_capacityremaining',hdfs[2], 1, ts)

    # RPC
    rpc = collect_rpc()
    if rpc is not None:
        insert(cursor, 'm_rpc_rpc_rpcprocessingtimeavgtime', rpc, 1, ts)

    # NameNode JVM
    jvm = collect_namenode_jvm()
    if jvm:
        insert(cursor, 'm_jvm_jvmmetrics_gctimemillis',      jvm[0], 1, ts)
        insert(cursor, 'm_jvm_jvmmetrics_memheapusedm',      jvm[1], 1, ts)
        insert(cursor, 'm_jvm_jvmmetrics_memheapcommittedm', jvm[2], 1, ts)

    # DataNode JMX
    dn = collect_datanode_jvm()
    if dn:
        insert(cursor, 'm_dfs_datanode_bytesread',           dn[0], 1, ts)
        insert(cursor, 'm_dfs_datanode_byteswritten',        dn[1], 1, ts)
        insert(cursor, 'm_dfs_datanode_blocksread',          dn[2], 1, ts)
        insert(cursor, 'm_dfs_datanode_blockwritten',        dn[3], 1, ts)
        insert(cursor, 'm_dfs_datanode_readblockopavgtime',  dn[4], 1, ts)
        insert(cursor, 'm_dfs_datanode_writeblockopavgtime', dn[5], 1, ts)


def main():
    print(f"启动集群采集脚本，间隔 {INTERVAL} 秒...")
    conn = get_conn()
    cursor = conn.cursor()
    ensure_tables(cursor)
    conn.commit()

    while True:
        ts = int(time.time())
        print(f"[{datetime.now().strftime('%H:%M:%S')}] 采集中...")
        try:
            collect_once(cursor, ts)
            conn.commit()
            print(f"  写入成功")
        except Exception as e:
            print(f"  写入失败: {e}")
            try:
                conn.rollback()
                conn = get_conn()
                cursor = conn.cursor()
            except:
                pass
        time.sleep(INTERVAL)


if __name__ == '__main__':
    main()
