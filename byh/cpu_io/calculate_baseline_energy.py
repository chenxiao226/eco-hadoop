import os
import pandas as pd

def calculate_baseline_energy(csv_path, task_name="pagerank", node_name="master"):
    print("📘 启动 calculate_baseline_energy")
    print("📂 当前脚本 __file__ =", __file__)
    print("📂 os.getcwd() =", os.getcwd())

    # 打印所有上级目录，方便判断在哪一层停止
    level = os.path.abspath(__file__)
    for i in range(1,6):
        level = os.path.dirname(level)
        print(f"⬆️  向上第 {i} 层目录：{level}")

    df = pd.read_csv(csv_path)
    avg_runtime = df["run_time"].mean() if "run_time" in df.columns else 1.0

    # 自动往上找 byh904 根目录
    cur_path = os.path.abspath(__file__)
    project_root = None
    while True:
        base = os.path.basename(cur_path)
        if base == "byh904":
            project_root = cur_path
            # print("✅ 找到了 byh904 根目录：", project_root)
            break
        parent = os.path.dirname(cur_path)
        if parent == cur_path:
            # print("❌ 没找到 byh904 目录。")
            break
        cur_path = parent

    if project_root:
        power_log_path = os.path.join(
            project_root,
            "hadoop_running_data_process",
            "active_power",
            "preprocessed_data",
            task_name,
            f"log-power-{node_name}.csv"
        )
        # print("🔍 构造出的功率日志路径：", power_log_path)
    else:
        power_log_path = None

    if not power_log_path or not os.path.exists(power_log_path):
        # print("❌ 没找到功率日志文件，请检查上面打印的路径。\n")
        return

    df_power = pd.read_csv(power_log_path, encoding="utf-8")
    if "active_power" not in df_power.columns:
        # print("❌ 功率日志文件中没有 active_power 列。")
        return

    avg_power = df_power["active_power"].astype(float).mean()
    avg_energy = avg_power * avg_runtime
    print(f"✅ 平均功率 {avg_power:.3f} W, 平均运行时间 {avg_runtime:.3f} s, 优化前平均能耗 {avg_energy:.3f} W·s")


if __name__ == "__main__":
    task_name = "pagerank"
    node_name = "master"
    csv_path = os.path.join(
        os.path.dirname(__file__),
        "dataset", task_name, node_name, "Init_hadoop_runtime_run0_90.csv"
    )
    calculate_baseline_energy(csv_path, task_name, node_name)
