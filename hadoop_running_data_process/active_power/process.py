import os
import re
import csv
import pandas as pd
from tqdm import tqdm

# 定义要保存的时间戳列表
Timestamp_List = []
def generate_Timestamp_List(task_name):
    # 打开文件并逐行读取
    with open('../original_data/'+task_name+'/time.txt', 'r', encoding='utf-8') as file:
        for line in file:
            #print(line)
            # 使用正则表达式提取完整日期时间部分
            match = re.search(r'(\d{4})年\s*(\d{2})月\s*(\d{2})日\s*星期\w+\s*(\d{2}):(\d{2}):(\d{2})', line)
            if match:
                # 格式化为14位数字格式 YYYYMMDDHHMMSS
                formatted_time = f"{match.group(1)}{match.group(2)}{match.group(3)}{match.group(4)}{match.group(5)}{match.group(6)}"
                Timestamp_List.append(formatted_time)  # 保存格式化后的日期时间
                #print(formatted_time)

    # 打印或处理提取的日期时间
    print("提取的日期时间列表：", Timestamp_List)

#定义一个保存 “时间戳-频率” 的映射对
master_CpuFreq_Time = {}
def preprocess_log(task_name,now_file):
    # 创建文件夹
    os.makedirs('./preprocessed_data/'+task_name, exist_ok=True)  # 创建文件夹，若已存在则不报错
    # 定义 需要保存的CSV 文件名
    csv_filename = './preprocessed_data/'+task_name+'/'+now_file+'log.csv'

    # 在代码开始时，直接用写模式清空文件内容并写入标题行
    with open(csv_filename, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(["time", "cpu_frequency", "cpu_usage", "sda_usage"])  # 写入标题行


    preline=''
    cpu_freq=-1
    time=''
    cpu_idle=''
    sda_idle=''

    # 打开日志文件
    with open('../original_data/'+task_name+'/system_log/'+now_file+'/log.log', 'r', encoding='utf-8') as file:
        total_lines = len(file.readlines()) #获取总行数
        file.seek(0)  # 将文件指针重置到文件开头
        for i, line in enumerate(tqdm(file, total=total_lines, desc=now_file+" 预处理进度")):
            if i > -1 :
                #print(line)
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
                        time = f"{year:04}{month:02}{day:02}{hour:02}{minute:02}{second:02}"
                        #print('time:',time)
                        #print("now_file:",now_file)
                        if now_file == 'master':
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
                    #print('idle:',cpu_idle)
                if 'sda' in line:
                    #print(line.strip())
                    # 使用 split 方法将字符串按空格拆分，然后取最后一个值
                    values = line.split()
                    sda_idle = round(float(values[-1])/100,4)  # 使用 -1 索引提取最后一个值
                    #print('sda:',sda_idle)

                    #执行到sda这一步，便完成了一组数据的提取
                    #print("cpu_freq:",cpu_freq)
                    if cpu_freq != -1 and time in Timestamp_List:
                        #print(time, cpu_freq,cpu_idle,sda_idle)
                        # 后续代码部分，用于记录每条数据
                        with open(csv_filename, 'a', newline='') as csvfile:
                            writer = csv.writer(csvfile)
                            # 记录数据
                            writer.writerow([time, cpu_freq, cpu_idle, sda_idle])

            else:
                break
            preline=line

    print("输出文件保存在：",csv_filename)


def preprocess_power(task_name,now_file):
    print("==开始处理"+task_name+"  "+now_file+"功率信息")
    print("-----1、读取time找出起始日期........")
    start_year  = -1
    start_month = -1
    start_day   = -1
    with open('../original_data/'+task_name+'/time.txt', 'r', encoding='utf-8') as file:
        first_line = file.readline().strip()  # 读取第一行并去除两端的空白字符
        # 使用正则表达式提取年月日
        match = re.search(r'(\d{4})年\s*(\d{2})月\s*(\d{2})日', first_line)
        if match:
            start_year = match.group(1).zfill(4)  # 确保年份是4位
            start_month = match.group(2).zfill(2)  # 确保月份是2位
            start_day = match.group(3).zfill(2)    # 确保日期是2位

    print("-----2、读取功率计采集信息表格........")
    # 读取 CSV 文件
    df = pd.read_csv('../original_data/'+task_name+'/power_collection/'+file_name+'.csv',encoding='gbk')
    # 提取 A 列和 E 列
    df_selected = df[['接收时间', '有功功率']].copy()  # 创建副本
    # 确保接收时间格式为 HH:MM:SS
    df_selected['接收时间'] = pd.to_datetime(df_selected['接收时间'], format='%H:%M:%S').dt.strftime('%H:%M:%S')

    # 新增一个空列，用于生成接收日期
    df_selected.loc[:, '接收日期'] = None
    #print(df_selected.head())

    pre_t = '-1'
    day_add = 0
    # 遍历 '接收时间' 列，并添加序号
    for col, now_t in enumerate(df_selected['接收时间']):
        #print(pre_t[0:2])
        #print(now_t[0:1])

        if pre_t[0:2]=='23' and now_t[0:1]=='0':
            day_add += 1

        now_day = int(start_day) + int(day_add)
        df_selected.loc[col,'接收日期'] = str(start_year)+str(start_month)+str(now_day).zfill(2)
        pre_t = now_t

    #print(df_selected)

    # 合并接收日期和接收时间为14位字符格式，并创建新列
    df_selected.loc[:, '接收日期时间'] = df_selected['接收日期'].astype(str) + df_selected['接收时间'].str.replace(':', '')

    # 创建新的 DataFrame 只包含接收日期时间和有功功率两列
    new_power_df = df_selected[['接收日期时间', '有功功率']]

    print("-----3、开始按照时间戳合并处理后的log文件和功率文件........")
    # 读取 处理后的log CSV 文件
    preprocess_log_df = pd.read_csv('./preprocessed_data/'+task_name+'/'+file_name+'log.csv',encoding='gbk')
    #print(preprocess_log_df.head())
    #print(new_power_df.head())

    # 使用 tqdm 显示进度条
    for power_col, timestamp in tqdm(enumerate(new_power_df['接收日期时间']), total=len(new_power_df),
                                     desc="     合并进度:"):
        # 提取前 13 位，作为时间的前缀
        prefix = timestamp[:13]

        # 生成从 0 到 9 的时间戳列表
        timestamps = [f"{prefix}{i}" for i in range(10)]

        #遍历所有时间戳，只有有一个命中，那就可以把当前功率加到表格中
        for t in timestamps:
            if t in Timestamp_List:
                # 查找目标时间戳所在的行索引

                # 确保 't' 是字符串并去除空格
                t = str(t).strip()
                # 临时将 'time' 列转换为字符串类型，并匹配
                matching_col = preprocess_log_df[preprocess_log_df['time'].astype(str).str.strip() == t].index
                preprocess_log_df.loc[matching_col, 'active_power'] = new_power_df.loc[power_col,'有功功率']
                preprocess_log_df.loc[matching_col, 'power_time'] = new_power_df.loc[power_col, '接收日期时间']
    #print(preprocess_log_df.head())

    # 指定要保存的文件路径
    file_path = './preprocessed_data/'+task_name+'/'+'log-power-'+now_file+'.csv'

    # 使用 'w' 模式保存数据，这会清空已存在的文件内容
    preprocess_log_df.to_csv(file_path, mode='w', index=False)
    print("-----4、合并文件已经被保存到"+file_path)



# 主程序逻辑
if __name__ == "__main__":

    #任务名
    task_name='pagerank'
    generate_Timestamp_List(task_name)

    # 读取文件名-------
    file_name_list = ['master','slave1','slave2']

    for file_name in file_name_list:
        preprocess_log(task_name,file_name)

    for file_name in file_name_list:
        preprocess_power(task_name,file_name)