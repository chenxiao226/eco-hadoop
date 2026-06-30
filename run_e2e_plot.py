import subprocess, sys, os

py   = r'D:\Anaconda3\envs\byh\python.exe'
root = r'D:\Chenxiao\20260302VLDBDEMO\byh904'

result = subprocess.run(
    [py, r'draw_experiments_figures\End_to_End_Performance\plot.py'],
    cwd=root, capture_output=True, text=True,
    encoding='utf-8', errors='replace',
    env={**os.environ, 'PYTHONIOENCODING': 'utf-8'}
)
print(result.stdout)
if result.stderr:
    print('STDERR:', result.stderr[:3000])
