import subprocess, os, sys

py   = r'D:\Anaconda3\envs\byh\python.exe'
cwd  = r'D:\Chenxiao\20260302VLDBDEMO\byh904\byh\cpu_io'
env  = {**os.environ, 'PYTHONIOENCODING': 'utf-8'}

cmd = [py, 'train_net.py',
       '--CSV_PATH', 'dataset/pi/master/Init_hadoop_runtime_run0_90.csv',
       '--NUM_EPOCHS_1', '10000',
       '--TASK_NAME', 'pi',
       '--FILE_NAME', 'master']

print(f"Running: {' '.join(cmd)}")
result = subprocess.run(cmd, cwd=cwd, env=env,
                        capture_output=True, text=True,
                        encoding='utf-8', errors='replace')
print(result.stdout[-3000:] if len(result.stdout) > 3000 else result.stdout)
if result.stderr:
    print('STDERR:', result.stderr[-2000:])
print('Exit code:', result.returncode)
