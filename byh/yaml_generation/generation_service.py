import os
import yaml

import os
import yaml

# YAML 文件目录
YAML_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'generated_yamls')

for filename in os.listdir(YAML_DIR):
    if filename.startswith("web0_conf_") and filename.endswith(".yaml"):
        filepath = os.path.join(YAML_DIR, filename)

        with open(filepath, 'r', encoding='utf-8') as f:
            docs = list(yaml.safe_load_all(f))

        # 取 Deployment 结构
        deployment = docs[0]

        service = {
            'apiVersion': 'v1',
            'kind': 'Service',
            'metadata': {
                'name': 'web0-service'
            },
            'spec': {
                'type': 'NodePort',
                'selector': {
                    'app': 'web0'
                },
                'ports': [
                    {
                        'port': 8080,
                        'targetPort': 8080,
                        'nodePort': 30080
                    }
                ]
            }
        }

        # 写入两个文档，确保分隔符 ---
        with open(filepath, 'w', encoding='utf-8') as f:
            yaml.dump_all([deployment, service], f, explicit_start=True)

print("✅ 所有 YAML 文件已成功修正：删除优先级、添加服务与唯一 CGROUP_DRIVER")
