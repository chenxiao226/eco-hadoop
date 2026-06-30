import sys, io
# Force UTF-8 stdout/stderr before any other imports
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

import os
os.chdir(r'D:\Chenxiao\20260302VLDBDEMO\byh904')
sys.argv = [
    'train_net.py',
    '--output-dir', './byh/param_search/output/pi/master',
    '--active_power_model-weights', './byh/active_power/output/pi/master/model_final.pth',
    '--cpu_io_model-weights', './byh/cpu_io/output/pi/master/model_final.pth',
    '--TASK_NAME', 'pi',
    '--NODE_NAME', 'master',
]

# Now run train_net as __main__
import runpy
runpy.run_module('byh.param_search.train_net', run_name='__main__', alter_sys=True)
