import pandas as pd

# 读取原始数据
input_path = r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\7param_merged_dataset.csv"
df = pd.read_csv(input_path)

# 删除不需要的字段
drop_columns = ["requests_cpu", "requests_memory", "limits_cpu", "limits_memory", "begin_time", "end_time"]
df.drop(columns=drop_columns, inplace=True, errors="ignore")

# 重命名字段
rename_dict = {
    "requests_cpu_num": "param_1",
    "limits_cpu_num": "param_3",
    "requests_mem_byte": "param_2",
    "limits_mem_byte": "param_4",
    "replica": "param_5",
    "cpu_freq": "cpu_freq",
    "run_time": "run_time"
}
df.rename(columns=rename_dict, inplace=True)

# 重新排列字段顺序
desired_order = ["param_1", "param_3", "param_2", "param_4", "param_5", "cpu_freq", "run_time"]
df = df[desired_order]

# 保存最终数据
output_path = input_path.replace(r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\7param_merged_dataset.csv", r"D:\Chenxiao\20241211HADOOPauto\byh904\byh\cpu_io\prediction_calculation\output\7param_dataset.csv")
df.to_csv(output_path, index=False)
print(f"✅ 处理完成，结果已保存为：{output_path}")
