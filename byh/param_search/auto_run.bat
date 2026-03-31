@echo off
rem Get current timestamp in YYYY-MM-DD_HH-MM-SS format
for /f "tokens=1-4 delims=:., " %%a in ("%time%") do (
    set HH=%%a
    set MM=%%b
    set SS=%%c
)
for /f "tokens=1-3 delims=-/" %%a in ("%date%") do (
    set YYYY=%%a
    set MM=%%b
    set DD=%%c
)
set TIMESTAMP=%YYYY%-%MM%-%DD%_%HH%-%MM%-%SS%

rem Run the Python command
python train_net.py --output-dir output/param_search_%TIMESTAMP% ^
    --active_power_model-weights ../active_power/output/pagerank/master/model_final.pth ^
    --cpu_io_model-weights ../cpu_io/output/pagerank/master/model_final.pth