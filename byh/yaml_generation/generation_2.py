import os
import yaml
from itertools import product
from pathlib import Path

# 参数空间（离散值）
cpu_values = ["250m", "500m", "750m", "800m", "1000m", "1200m", "1500m", "2000m", "3000m"]
mem_values = ["512Mi", "1Gi", "1.5Gi", "2Gi", "3Gi", "4Gi", "5Gi","6Gi"]
restart_policies = ["Always"]
priority_classes = ["high-priority", "low-priority"]
cgroups = ["cgroupfs", "systemd"]
volumes = ["none", "emptyDir", "hostPath"]
replica_counts = [0, 1, 2,3 ]

# 模板路径
template_file = "web0-slave1.yaml"
output_dir = Path("./generated_yamls/")
output_dir.mkdir(parents=True, exist_ok=True)

# 加载模板
with open(template_file, 'r', encoding='utf-8') as f:
    documents = list(yaml.safe_load_all(f))
    base_yaml = documents[0]

# 构造组合并生成 YAML
combos = list(product(cpu_values, mem_values, restart_policies, volumes, replica_counts, priority_classes, cgroups))

for idx, (cpu, mem, restart, volume, replica, priority, cgroup) in enumerate(combos, start=1):
    deployment_yaml = yaml.safe_load(yaml.dump(base_yaml))  # 深拷贝

    # [1] 设置资源 requests 和 limits
    container = deployment_yaml['spec']['template']['spec']['containers'][0]
    container['resources']['requests']['cpu'] = cpu
    container['resources']['requests']['memory'] = mem
    container['resources']['limits']['cpu'] = cpu
    container['resources']['limits']['memory'] = mem

    # [2] 设置重启策略
    deployment_yaml['spec']['template']['spec']['restartPolicy'] = restart

    # [3] 设置优先级
    deployment_yaml['spec']['template']['spec']['priorityClassName'] = priority

    # [4] 设置副本数
    deployment_yaml['spec']['replicas'] = replica

    # [5] 注入自定义 CGROUP_DRIVER 环境变量（安全方式）
    container.setdefault('env', []).append({
        'name': 'MY_CGROUP_MODE',
        'value': cgroup
    })

    # [6] 设置卷挂载
    if volume == "emptyDir":
        deployment_yaml['spec']['template']['spec']['volumes'] = [
            {'name': 'workdir', 'emptyDir': {}}
        ]
    elif volume == "hostPath":
        deployment_yaml['spec']['template']['spec']['volumes'] = [
            {'name': 'workdir', 'hostPath': {'path': '/mnt/data', 'type': 'Directory'}}
        ]
    else:
        deployment_yaml['spec']['template']['spec']['volumes'] = []

    container['volumeMounts'] = [
        {'mountPath': '/var/jenkins_home', 'name': 'workdir'}
    ] if volume != "none" else []

    # [7] 固定在 slave1 节点
    deployment_yaml['spec']['template']['spec']['nodeSelector'] = {
        'kubernetes.io/hostname': 'slave1'
    }

    # [8] 输出文件保存
    filename = output_dir / f"web0_conf_{idx:03}.yaml"
    with open(filename, 'w', encoding='utf-8') as f:
        yaml.dump(deployment_yaml, f)

print(f"✅ 生成完毕，共 {len(combos)} 个 YAML 配置，存于: {output_dir.resolve()}")
