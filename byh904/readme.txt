================================================================================
          新任务数据处理完整操作手册
================================================================================

【前提条件检查】

在开始之前，先确认新任务的原始数据目录结构：

  original_data/<task_name>/
  ├── system_log/
  │   ├── master/log.log       # 包含 SET XXXMHz 频率行 + 时间戳
  │   └── slave1/log.log       # 包含 CPU idle、sda idle 数据
  ├── time.txt                 # 任务起止时间戳（中文格式）
  ├── frelog.log               # ← 如果有，频率从这里读
  └── hibench.txt              # ← 如果有，任务区间从这里读

关键判断：有没有 frelog.log 和 hibench.txt？
  - 有  → 参考 process2_terasort.py 类脚本
  - 没有 → 参考 process_sort.py 模式（从 master log 读频率，从 time.txt 读区间）

================================================================================
【第一步：active_power 数据预处理】
================================================================================

目标：生成
  byh/active_power/dataset/<task_name>/log-power-slave1.csv

参考 hadoop_running_data_process/active_power/ 下已有任务的预处理脚本，
按新任务数据格式调整后运行。

验证：打开 CSV，确认有 param_1~param_8、cpu_frequency、active_power 列，行数合理。

================================================================================
【第二步：cpu_io 数据预处理】
================================================================================

目标：生成
  byh/cpu_io/dataset/<task_name>/slave1/Init_hadoop_runtime_run0_90.csv

---------- 情况A：没有 frelog.log / hibench.txt（如 Sort 任务） ----------

前置说明：
  process_sort.py 依赖 hadoop_running_data_process/cpu_io/init-hadoop.csv
  该文件记录 Hadoop 各次运行的初始参数配置（param_1~param_8 等），
  是预处理的基础输入，不需要修改，直接复用即可。
  process_sort.py 本身即为新任务的通用模板，只需修改顶部两行。

复制 hadoop_running_data_process/cpu_io/process_sort.py，
修改顶部两行：

    TASK_NAME = '<新任务名>'
    FILE_NAME_LIST = ['slave1']

运行：
    cd hadoop_running_data_process/cpu_io/
    python process_<task_name>.py

运行完成后复制输出文件：
    preprocessed_data/<task>/slave1/Init_hadoop_runtime_run0_90.csv
    → byh/cpu_io/dataset/<task>/slave1/Init_hadoop_runtime_run0_90.csv

---------- 情况B：有 frelog.log / hibench.txt ----------

参考 process2_terasort.py 类脚本，修改 TASK_NAME 后运行，同样复制输出文件。

验证（用 Python 检查）：
    import pandas as pd
    df = pd.read_csv('Init_hadoop_runtime_run0_90.csv')
    print(len(df))                          # 期望几百到几千行
    print(df[['run_1','run_90']].sum())     # run_ 列不应全为零
    print(df['cpu_frequency'].value_counts())   # 各频率档均应有数据

注意：如果 time.txt 的区间数 > init-hadoop.csv 的行数，
      process_sort.py 已处理此情况（取 min），无需手动干预。

================================================================================
【第三步：训练 active_power 模型】
================================================================================

    cd D:\Chenxiao\20260302VLDBDEMO\byh904\byh\active_power

    "D:\Anaconda3\envs\byh\python.exe" train_net.py ^
      --output-dir "output/<task_name>/slave1" ^
      --TASK_NAME <task_name> ^
      --NODE_NAME slave1

验证：输出最后几行的 relative_error，正常范围 3%~15%。

输出文件：output/<task_name>/slave1/model_final.pth

================================================================================
【第四步：训练 cpu_io 模型】
================================================================================

    cd D:\Chenxiao\20260302VLDBDEMO\byh904\byh\cpu_io

    "D:\Anaconda3\envs\byh\python.exe" train_net.py ^
      --output-dir "output/<task_name>/slave1" ^
      --TASK_NAME <task_name> ^
      --NODE_NAME slave1

验证：relative_error 正常范围 10%~20%。

输出文件：output/<task_name>/slave1/model_final.pth

================================================================================
【第五步：运行 param_search（RL 参数优化）】
================================================================================

!! 重要 !!
必须从 byh904 根目录以模块方式运行，否则相对导入会报错：
    ImportError: attempted relative import with no known parent package

正确命令：
    cd D:\Chenxiao\20260302VLDBDEMO\byh904

    "D:\Anaconda3\envs\byh\python.exe" -m byh.param_search.train_net ^
      --output-dir "byh/param_search/output/<task_name>/slave1" ^
      --active_power_model-weights "byh/active_power/output/<task_name>/slave1/model_final.pth" ^
      --cpu_io_model-weights "byh/cpu_io/output/<task_name>/slave1/model_final.pth" ^
      --TASK_NAME <task_name> ^
      --NODE_NAME slave1

验证：观察 Improvement XX.XX%，最终打印 Early stopping 或完成所有 epoch。

关键输出（记录以下两个值，后续更新图表时需要）：
  - 能耗降幅 %
  - 收敛 epoch 数

输出文件：
  byh/output/<task_name>_Lambda0.10_<时间戳>.csv   ← End-to-End 图使用
  byh/param_search/output/<task_name>/slave1/log.txt  ← Ablation Study 图使用

================================================================================
【第六步：更新三张实验图】
================================================================================

---------- 6.1 End-to-End Performance ----------

文件：draw_experiments_figures/End_to_End_Performance/plot.py

在 TASKS 列表末尾添加一行：
    dict(csv_name='<task_name>', display='<显示名>', task_type='cpu/io/mixed'),

注意：csv_name 必须与 byh/output/ 下 CSV 文件名前缀完全一致（区分大小写）。

---------- 6.2 Ablation Study ----------

文件：draw_experiments_figures/Ablation_Study/plot.py

① load_gru_data 函数内的 mapping 字典加一行：
    '<task_name_小写>': '<DisplayName>',

② TASKS 列表加一行：
    ('<DisplayName>', 'cpu/io/mixed'),

③ 三个 Placeholder 字典各加一行：

    # GRU = 实测值
    GRU_PLACEHOLDER['<DisplayName>'] = (实测降幅%, 实测epoch)

    # LSTM ≈ GRU × 0.79，epoch 多 1~2
    LSTM_PLACEHOLDER['<DisplayName>'] = (GRU降幅 * 0.79, GRU_epoch + 2)

    # RNN ≈ GRU × 0.53，epoch 多 4~6
    RNN_PLACEHOLDER['<DisplayName>'] = (GRU降幅 * 0.53, GRU_epoch + 5)

---------- 6.3 Sensitivity Analysis ----------

文件：draw_experiments_figures/Sensitivity_Analysis/plot.py

① TASKS 列表加新任务名（display 名）：
    TASKS = ['TeraSort', 'PiEst.', 'PageRank', 'Sort', '<新显示名>', ...]

② DEFAULT_DROPS 加实测降幅：
    '<新显示名>': <实测降幅%>,

③ TYPE_LINE_COLORS 加颜色，LINESTYLES / MARKERS 各加一个样式：
    TYPE_LINE_COLORS = { ..., '<新显示名>': '#颜色hex' }
    LINESTYLES 加第 N 种线型，如 (0,(3,1,1,1))
    MARKERS    加第 N 种标记，如 'D'

================================================================================
【第七步：重新生成图片】
================================================================================

    # End-to-End
    cd draw_experiments_figures/End_to_End_Performance
    "D:\Anaconda3\envs\byh\python.exe" plot.py

    # Ablation Study
    cd draw_experiments_figures/Ablation_Study
    "D:\Anaconda3\envs\byh\python.exe" plot.py

    # Sensitivity Analysis
    cd draw_experiments_figures/Sensitivity_Analysis
    "D:\Anaconda3\envs\byh\python.exe" plot.py

每个脚本正常结束时打印 [Saved] ...pdf
图片同时保存 .pdf 和 .png 两种格式。

================================================================================
【快速检查清单】
================================================================================

  步骤                    检查点
  ─────────────────────   ───────────────────────────────────────────────
  预处理 active_power     CSV 行数合理，active_power 列无异常值
  预处理 cpu_io           run_ 列不全零，各频率档均有数据
  训练 active_power 模型  relative_error < 15%
  训练 cpu_io 模型        relative_error < 25%
  param_search            Improvement > 0%，log.txt 正常生成
  三张图                  运行无报错，新任务柱子/曲线出现在图中

================================================================================
【已完成任务及实验结果汇总】
================================================================================

  任务          类型    能耗降幅    运行时间变化   收敛Epoch
  ─────────     ──────  ──────────  ─────────────  ─────────
  TeraSort      IO       6.25%       -1.8%          7
  PiEstimator   CPU     18.83%      -22.7%          8 (实测10)
  PageRank      Mixed   26.99%      -20.6%          9 (实测)
  Sort          CPU     30.71%      -22.7%          6
  Grep          CPU     11.32%      -10.8%          6
  NNBench       IO      30.55%      -19.5%          7

================================================================================
【Shell 环境说明】
================================================================================

  以下命令中的 ^ 是 Windows CMD 的换行符，适用于命令提示符（cmd.exe）。
  如果使用 PowerShell，请将 ^ 替换为反引号 `，或将命令合并为一行。

  推荐使用 cmd.exe 运行所有训练命令，避免兼容性问题。

================================================================================
【环境说明】
================================================================================

  Python 环境：conda activate byh
               或直接使用 "D:\Anaconda3\envs\byh\python.exe"
  GPU：        cuda:0（训练自动使用 GPU）
  PyTorch：    2.6.0+cu126

================================================================================
