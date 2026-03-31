
import pandas as pd
import re

# 读取两个文件
k8s_df = pd.read_csv(r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\k8s_params_output.csv")
log_df = pd.read_csv(r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\active_power\k8s_dataset\jenkins\timelog_jenkins_web0.txt", sep=",", encoding="utf-8")  # 或 sep=","

# 读取两个文件
# k8s_df = pd.read_csv(k8s_csv)
# log_df = pd.read_csv(log_txt)

# 提取编号
def extract_index(yaml_str):
    match = re.search(r'web0_conf_(\d+)\.yaml', yaml_str)
    return int(match.group(1)) if match else None

log_df["index"] = log_df["yaml"].apply(extract_index)

# 合并数据
merged_rows = []
for _, row in log_df.iterrows():
    idx = row["index"]
    if pd.notna(idx) and 1 <= idx <= len(k8s_df):
        k8s_row = k8s_df.iloc[idx - 1].copy()  # 注意编号从 1 开始
        k8s_row["begin_time"] = row["begin_time"]
        k8s_row["end_time"] = row["end_time"]
        merged_rows.append(k8s_row)

# 创建合并后的 DataFrame
merged_df = pd.DataFrame(merged_rows)

# 保存结果
merged_df.to_csv(r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\group_stats_by_time.csv", index=False)
print("✅ 合并完成，输出文件: merged_k8s_timelog_output.csv")
