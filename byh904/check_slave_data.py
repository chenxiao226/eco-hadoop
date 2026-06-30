import os, glob

base = r'D:\Chenxiao\20260302VLDBDEMO\byh904\hadoop_running_data_process\original_data\pi'

for node in ['slave1', 'slave2']:
    sl = os.path.join(base, 'system_log', node)
    pc = os.path.join(base, 'power_collection', node)
    sl_ok = os.path.isdir(sl)
    pc_ok = os.path.isdir(pc)
    print(f"\n=== {node} ===")
    print(f"  system_log:      {'EXISTS' if sl_ok else 'MISSING'}")
    print(f"  power_collection: {'EXISTS' if pc_ok else 'MISSING'}")
    if sl_ok:
        log = os.path.join(sl, 'log.log')
        if os.path.exists(log):
            sz = os.path.getsize(log)
            print(f"  log.log size: {sz:,} bytes")
            with open(log, 'r', encoding='utf-8', errors='replace') as f:
                lines = [f.readline() for _ in range(6)]
            print("  log.log head:", ''.join(lines[:3]).strip()[:120])
    if pc_ok:
        files = os.listdir(pc)
        print(f"  power_collection files: {files[:5]}")

# Also check what's already preprocessed
print("\n=== Already preprocessed ===")
for path in [
    r'D:\Chenxiao\20260302VLDBDEMO\byh904\hadoop_running_data_process\active_power\preprocessed_data\pi',
    r'D:\Chenxiao\20260302VLDBDEMO\byh904\hadoop_running_data_process\cpu_io\preprocessed_data\pi',
]:
    if os.path.isdir(path):
        nodes = os.listdir(path)
        print(f"  {os.path.basename(os.path.dirname(path))}/pi: {nodes}")
