import subprocess, sys, os

py = r'D:\Anaconda3\envs\byh\python.exe'
root = r'D:\Chenxiao\20260302VLDBDEMO\byh904'
env = {**os.environ, 'PYTHONIOENCODING': 'utf-8', 'KMP_DUPLICATE_LIB_OK': 'TRUE'}
scripts = [
    r'draw_experiments_figures\End_to_End_Performance\plot.py',
    r'draw_experiments_figures\Ablation_Study\plot.py',
    r'draw_experiments_figures\Sensitivity_Analysis\plot.py',
    r'draw_experiments_figures\Parameter_Attribution\plot.py',
]

for s in scripts:
    print(f'\n=== Running {s} ===')
    result = subprocess.run(
        [py, s],
        cwd=root,
        capture_output=True,
        text=True,
        encoding='utf-8',
        errors='replace',
        env=env
    )
    print(result.stdout)
    if result.stderr:
        print('STDERR:', result.stderr[:2000])
    print(f'Exit code: {result.returncode}')
