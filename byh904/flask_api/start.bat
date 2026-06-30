@echo off
REM Activate the byh conda environment and start the Flask API
echo Activating conda environment: byh

REM Call conda's own activate script (works in plain cmd without 'conda init')
call D:\Anaconda3\Scripts\activate.bat byh

echo Starting Flask API on port 5001 ...
cd /d "%~dp0"
python app.py

pause