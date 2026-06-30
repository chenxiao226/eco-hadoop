@echo off
chcp 65001 > nul
set PYTHONIOENCODING=utf-8
cd /d D:\Chenxiao\20260302VLDBDEMO\byh904
D:\Anaconda3\envs\byh\python.exe -m byh.param_search.train_net --output-dir ./byh/param_search/output/pi/master --active_power_model-weights ./byh/active_power/output/pi/master/model_final.pth --cpu_io_model-weights ./byh/cpu_io/output/pi/master/model_final.pth --TASK_NAME pi --NODE_NAME master
