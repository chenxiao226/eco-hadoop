import re
import os
from typing import List
import numpy as np
import pandas as pd
from tqdm import tqdm
from datetime import datetime, timedelta

CPU_SLICES: List[float] = [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 200.0]
SDA_SLICES: List[float] = [0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 7.0, 9.0, 15.0, 200.0]


# 读取本地CSV文件
file_path = "init-hadoop.csv"  # 替换为你的文件路径
Init_hadoop = pd.read_csv(file_path, sep=",")  # 根据文件内容，可能需要指定分隔符，如"\t"
# 打印列标签
print("列标签:", Init_hadoop.columns.tolist())


# 生成开始结束时间-----------------------------------------------------------------------------------------------


def process_time(task_name):
    # 打开文件并逐行读取
    with open('../original_data/'+task_name+'/time.txt', 'r', encoding='utf-8') as file:
        lines = file.readlines()  # 读取所有行
        match = re.search(r'(\d{4})年\s*(\d{2})月\s*(\d{2})日\s*星期\w+\s*(\d{2}):(\d{2}):(\d{2})', lines[0])
        if match:
            # 格式化为14位数字格式 YYYYMMDDHHMMSS
            TASK_START_TIME = (int)(f"{match.group(1)}{match.group(2)}{match.group(3)}{match.group(4)}{match.group(5)}{match.group(6)}")

        match = re.search(r'(\d{4})年\s*(\d{2})月\s*(\d{2})日\s*星期\w+\s*(\d{2}):(\d{2}):(\d{2})', lines[-1])
        if match:
            # 格式化为14位数字格式 YYYYMMDDHHMMSS
            TASK_END_TIME = (int)(f"{match.group(1)}{match.group(2)}{match.group(3)}{match.group(4)}{match.group(5)}{match.group(6)}")

    # 打印或处理提取的日期时间
    print("任务起始时间：", TASK_START_TIME,type(TASK_START_TIME))
    print("任务结束时间：", TASK_END_TIME,type(TASK_END_TIME))
    return TASK_START_TIME, TASK_END_TIME

#处理log.log文件-----------------------------------------------------------------------------------------------
#定义一个保存 “时间戳-频率” 的映射对
master_CpuFreq_Time = {}
def preprocess_log(task_name,file_name,TASK_END_TIME,Hadoop_df):
    preline=''
    cpu_freq=-1
    time=''
    cpu_idle=''
    sda_idle=''
    # 初始化空表-----------------------
    columns = ["time", "cpu_freq", "hadoop_idx","cpu_idle", "sda_idle"]
    #df = pd.DataFrame(columns=columns)
    # 初始化临时存储数据的列表,最后再保存为表格，可以极大提高处理速度
    data_list = []

    # 打开文件并逐行读取
    print("读取",file_name+"log.log...")
    with open('../original_data/'+task_name+'/system_log/'+file_name+'/log.log', 'r', encoding='utf-8') as file:
        total_lines = len(file.readlines()) #获取总行数
        file.seek(0)  # 将文件指针重置到文件开头
        for i, line in enumerate(tqdm(file, total=total_lines, desc="1."+file_name+" log.log 预处理进度")):
            if i > -1 :
                #print((line))
                #如果检测到设置频率
                if 'SET' in line:
                    #print(line.strip())
                    # 使用正则表达式提取 MHz 前面的数字
                    match = re.search(r'(\d+)MHz', line)
                    if match:
                        cpu_freq = int(match.group(1))  # 提取到的频率数值，转为整数
                        #print("cpu_freq:",cpu_freq)
                if '秒' in line:
                    # 使用正则表达式提取年月日和时间部分
                    match = re.search(r'(\d{4})年(\d{2})月(\d{2})日 (\d{2})时(\d{2})分(\d{2})秒', line)
                    if match:
                        year = int(match.group(1))
                        month = int(match.group(2))
                        day = int(match.group(3))
                        hour = int(match.group(4))
                        minute = int(match.group(5))
                        second = int(match.group(6))

                        # 格式化并合并成14位数的字符串
                        time = (int)(f"{year:04}{month:02}{day:02}{hour:02}{minute:02}{second:02}")
                        #print('time:',time,end=" ")
                        #print("now_file:",now_file)
                        if file_name == 'master':
                            master_CpuFreq_Time[time] = cpu_freq
                        else:
                            if time in master_CpuFreq_Time.keys():
                                cpu_freq = master_CpuFreq_Time[time]
                if 'idle' in preline:
                    #print(preline.strip())
                    #print(line.strip())
                    # 使用 split 方法将字符串按空格拆分，然后取第6个值
                    values = line.split()
                    cpu_idle =  round(float(values[5]) / 100, 4)  # 保留小数点后4位
                    #print('idle:',cpu_idle,end=" ")
                if 'sda' in line:
                    #print(line.strip())
                    # 使用 split 方法将字符串按空格拆分，然后取最后一个值
                    values = line.split()
                    sda_idle = round(float(values[-1])/100,4)  # 使用 -1 索引提取最后一个值
                    #print('sda:',sda_idle)

                    #执行到sda这一步，便完成了一组数据的提取
                    #print("cpu_freq:",cpu_freq)
                    #print("time",time,"  TASK_END_TIME",TASK_END_TIME)
                    if cpu_freq!=-1 and time < TASK_END_TIME:
                        # 找到时间范围所在Hadoop_df的行
                        hadoop_idx = Hadoop_df[(Hadoop_df["start_time"] <= time) & (Hadoop_df["end_time"] >= time)]
                        # 如果找到匹配行，返回行号；否则返回 -1

                        if not hadoop_idx.empty:
                            row_index = hadoop_idx.index[0]
                            # 新增一行数据
                            data_list.append(
                                {"time": time, "cpu_freq": cpu_freq, "hadoop_idx":row_index,"cpu_idle": cpu_idle, "sda_idle": sda_idle})



            else:
                break
            preline=line
        # 将列表转为 DataFrame
        cpufreq_cpu_sda_df = pd.DataFrame(data_list)
        print("cpufreq_cpu_sda_df的前几行------------------------")
        print(cpufreq_cpu_sda_df)

        #保存前检查路径
        save_path = './preprocessed_data/'+task_name+'/'+file_name
        if not os.path.exists(save_path):
            print(f"路径 {save_path} 不存在，正在创建...")
            os.makedirs(save_path)  # 创建文件夹
        cpufreq_cpu_sda_df.to_csv(save_path+"/cpufreq_cpu_sda.csv", index=False, sep=",")

        return cpufreq_cpu_sda_df


#处理hadoop日志，生成1500对时间范围，用于后续标记log.log的内容在哪个范围
def preprocess_hadoop(task_name,file_name):
    durations = []  # 用于存储所有的 duration 值
    start_times = []  # 用于存储起始时间
    end_times = []  # 用于存储结束时间
    # 打开文件并逐行读取
    with open('../original_data/'+task_name+'/hibench.txt', 'r', encoding='utf-8') as file:
        for idx, line in enumerate(file):
            # 跳过表头行
            if line.startswith("Type"):
                continue

            # 使用正则表达式匹配 Date、Time 和 Duration
            match = re.search(r'\b(\d{4}-\d{2}-\d{2})\b\s+(\d{2}:\d{2}:\d{2})\s+\S+\s+(\S+)\s+', line)
            if match:
                date = match.group(1)
                time = match.group(2)
                duration = match.group(3)

                # 合并日期和时间，并转换为 datetime 对象
                start_datetime = datetime.strptime(f"{date} {time}", "%Y-%m-%d %H:%M:%S")

                # 转换为 YYYYMMDDHHMMSS 格式的字符串
                start_time_str = (int)(start_datetime.strftime("%Y%m%d%H%M%S"))
                start_times.append(start_time_str)

                # 计算结束时间
                duration_seconds = round(float(duration))  # 将 duration 转换为整数秒
                end_datetime = start_datetime + timedelta(seconds=duration_seconds)
                end_time_str = (int)(end_datetime.strftime("%Y%m%d%H%M%S"))
                end_times.append(end_time_str)


                # 将四舍五入后的 duration 添加到列表中
                durations.append(round(float(duration)))
                # 输出提取的信息
                # 输出提取和计算的信息
                #print( f"idx: {idx}, Start Time: {start_time_str}, End Time: {end_time_str}, Duration: {duration_seconds}")

    # 创建 DataFrame 并保存结果
    Hadoop_df = pd.DataFrame({
        "start_time": start_times,
        "end_time": end_times,
        "duration": durations
    })
    # 打印结果表的前几行
    print("Hadoop_df的前几行------------------")
    print(Hadoop_df.head())

    # 将 durations 添加为新列到 DataFrame 的最后
    Init_hadoop['run_time'] = durations
    # 如果需要保存为新的CSV文件

    # 保存前检查路径
    save_path = './preprocessed_data/' + task_name + '/' + file_name
    if not os.path.exists(save_path):
        print(f"路径 {save_path} 不存在，正在创建...")
        os.makedirs(save_path)  # 创建文件夹

    Init_hadoop.to_csv(save_path+"/Init_hadoop_runtime.csv", index=False, sep=",")

    # 将 Init_hadoop 拷贝到新的 Init_hadoop_runtime_df
    Init_hadoop_runtime_df = Init_hadoop.copy()

    return Hadoop_df,Init_hadoop_runtime_df




#统计每个cpu_idle和sda_idle的组合出现的次数
def Calculate_Hadoop_Statistics(Init_hadoop_runtime_df,cpufreq_cpu_sda_df, CPU_SLICES, SDA_SLICES,file_name):
    # 生成新列名列表------------------------------------------------------------------
    new_columns = [f"run_{i}" for i in range(1, 91)]  # run_1, run_2, ..., run_90
    # 新增 90 列到 DataFrame，并初始化为 None 或其他默认值
    for col in new_columns:
        Init_hadoop_runtime_df[col] = 0
    #------------------------------------------------------------------------------


    #查找区间范围
    def find_range(value: float, slices: List[float]):
        for i in range(len(slices) - 1):
            if slices[i] <= value < slices[i + 1]:
                return i
        return None  # 如果不在任何区间中返回None


    # 遍历每一行并读取指定列
    pre_hadoop_idx = -1
    run_list = [0] * 90
    for index, row in tqdm(cpufreq_cpu_sda_df.iterrows(), total=len(cpufreq_cpu_sda_df), desc="2."+ file_name + " 统计并生成run_1 - run_90:"):
        now_hadoop_idx = row["hadoop_idx"]
        cpu_idle = row["cpu_idle"]
        sda_idle = row["sda_idle"]

        # 找到cpu_idle和sda_idle对应的区间
        cpu_idx = find_range(cpu_idle*100, CPU_SLICES)
        sda_idx = find_range(sda_idle*100, SDA_SLICES)
        #print("cpu_idle:", cpu_idle, "cpu_idx:", cpu_idx, "sda_idle: ",sda_idle, "sda_idx:",sda_idx)
        run_idx = cpu_idx + sda_idx*10
        run_list[run_idx] += 1
        #print(f"cpu_idle: {cpu_idle:.4f}  cpu_idx: {cpu_idx:3d}  sda_idle: {sda_idle:.4f}  sda_idx: {sda_idx:3d}  run_idx: {run_idx:3d}")


        # 用于检测是否完成一整块hadoop_idx的统计，是的话，清空run_list
        if pre_hadoop_idx != now_hadoop_idx:
            # 更新第 5 行，第 6-76 列
            column_range = Init_hadoop_runtime_df.columns[10:100]             # 获取第 11-100 列的列名,其实就是run_1~run_90
            #print(len(column_range),column_range)
            Init_hadoop_runtime_df.loc[pre_hadoop_idx, column_range] = run_list
            run_list = [0] * 90

        pre_hadoop_idx = now_hadoop_idx

    # 保存前检查路径
    save_path = './preprocessed_data/' + task_name + '/' + file_name
    if not os.path.exists(save_path):
        print(f"路径 {save_path} 不存在，正在创建...")
        os.makedirs(save_path)  # 创建文件夹
    Init_hadoop_runtime_df.to_csv(save_path + "/Init_hadoop_runtime_run0_90.csv", index=False, sep=",")





# 主程序逻辑
if __name__ == "__main__":
    #任务名
    task_name='pagerank'

    # 读取文件名-------
    file_name_list = ['master','slave1','slave2']
    #file_name_list = ['slave1','slave2']

    for file_name in file_name_list:
        print("开始执行",task_name,"-",file_name," 数据任务...")
        Hadoop_df,Init_hadoop_runtime_df = preprocess_hadoop(task_name,file_name)
        TASK_START_TIME,TASK_END_TIME = process_time(task_name)
        cpufreq_cpu_sda_df = preprocess_log(task_name,file_name,TASK_END_TIME,Hadoop_df)
        Calculate_Hadoop_Statistics(Init_hadoop_runtime_df,cpufreq_cpu_sda_df, CPU_SLICES, SDA_SLICES,file_name)
