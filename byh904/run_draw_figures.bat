@echo off
chcp 65001 > nul
set PYTHONIOENCODING=utf-8
set KMP_DUPLICATE_LIB_OK=TRUE
cd /d D:\Chenxiao\20260302VLDBDEMO\byh904
set PY=D:\Anaconda3\envs\byh\python.exe

echo === End-to-End Performance ===
%PY% draw_experiments_figures\End_to_End_Performance\plot.py
echo.

echo === Ablation Study ===
%PY% draw_experiments_figures\Ablation_Study\plot.py
echo.

echo === Sensitivity Analysis ===
%PY% draw_experiments_figures\Sensitivity_Analysis\plot.py
echo.

echo === Parameter Attribution ===
%PY% draw_experiments_figures\Parameter_Attribution\plot.py
echo.

echo All done.
