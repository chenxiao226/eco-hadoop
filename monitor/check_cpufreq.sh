#!/bin/bash
# ============================================================
# CPU 调频(DVFS/cpufreq)能力检测脚本
# 用途: 在甲方集群每个节点上跑,判断能否手动调节 CPU 频率
# 用法: bash check_cpufreq.sh
#   或在 131 上对三节点批量检测(见文件末尾注释)
# ============================================================

echo "=================================================="
echo " 节点: $(hostname)  ($(date '+%Y-%m-%d %H:%M:%S'))"
echo "=================================================="

CPU0=/sys/devices/system/cpu/cpu0/cpufreq

# 1) cpufreq 子系统是否存在
echo
echo "[1] cpufreq 子系统:"
if [ -d "$CPU0" ]; then
    echo "    可用 ($CPU0 存在)"
else
    echo "    不可用 -- 内核未启用 cpufreq,或被 BIOS/虚拟化屏蔽"
    echo "    => 该节点无法软件调频"
fi

# 2) 调频驱动
echo
echo "[2] 调频驱动 (scaling_driver):"
if [ -f "$CPU0/scaling_driver" ]; then
    DRIVER=$(cat "$CPU0/scaling_driver")
    echo "    $DRIVER"
    case "$DRIVER" in
        intel_pstate)
            echo "    注: intel_pstate 默认只支持 powersave/performance 两档,"
            echo "        无法用 userspace governor 设任意频率;"
            echo "        如需手动设频,需内核参数 intel_pstate=disable 切回 acpi-cpufreq"
            ;;
        acpi-cpufreq|cppc_cpufreq|*)
            echo "    注: 该驱动通常支持 userspace governor,可手动设频率"
            ;;
    esac
else
    echo "    无 (cpufreq 不可用)"
fi

# 3) 可用 governor
echo
echo "[3] 可用调速策略 (available_governors):"
if [ -f "$CPU0/scaling_available_governors" ]; then
    GOVS=$(cat "$CPU0/scaling_available_governors")
    echo "    $GOVS"
    if echo "$GOVS" | grep -qw userspace; then
        echo "    => 支持 userspace,可手动锁定频率 (echo 频率 > scaling_setspeed)"
    else
        echo "    => 不含 userspace,只能在现有 governor 间切换,不能设任意频率"
    fi
else
    echo "    无"
fi

# 4) 当前 governor
echo
echo "[4] 当前调速策略 (scaling_governor):"
[ -f "$CPU0/scaling_governor" ] && cat "$CPU0/scaling_governor" | sed 's/^/    /' || echo "    无"

# 5) 频率范围与可选频点
echo
echo "[5] 频率范围:"
if [ -f "$CPU0/scaling_min_freq" ]; then
    MIN=$(cat "$CPU0/scaling_min_freq")
    MAX=$(cat "$CPU0/scaling_max_freq")
    CUR=$(cat "$CPU0/scaling_cur_freq" 2>/dev/null || echo '?')
    echo "    min=$((MIN/1000)) MHz  max=$((MAX/1000)) MHz  cur=$( [ "$CUR" = '?' ] && echo '?' || echo $((CUR/1000)) ) MHz"
else
    echo "    无"
fi
echo
echo "    可选频点 (scaling_available_frequencies):"
if [ -f "$CPU0/scaling_available_frequencies" ]; then
    cat "$CPU0/scaling_available_frequencies" | sed 's/^/    /'
else
    echo "    无 (intel_pstate 通常不暴露离散频点,而是连续范围)"
fi

# 6) 写权限/能否实际改频 (只读探测,不真正改)
echo
echo "[6] 调频权限探测:"
if [ "$(id -u)" -ne 0 ]; then
    echo "    当前非 root,设频率需要 root 权限"
else
    if [ -w "$CPU0/scaling_governor" ]; then
        echo "    root 且 scaling_governor 可写 => 可以调频"
    else
        echo "    scaling_governor 不可写 => 可能被锁定"
    fi
fi

# 7) 工具是否就绪
echo
echo "[7] 调频工具:"
for tool in cpupower cpufreq-info turbostat; do
    if command -v $tool >/dev/null 2>&1; then
        echo "    $tool: 已安装"
    else
        echo "    $tool: 未安装 (可 yum install kernel-tools / cpupowerutils)"
    fi
done

echo
echo "=================================================="
echo " 检测完成: $(hostname)"
echo "=================================================="

# ============================================================
# 在 131 上批量检测三节点(把本脚本先放到 131):
#   for h in hdfs1 hdfs2 hdfs3; do
#     echo; echo "###### $h ######"
#     ssh root@$h 'bash -s' < check_cpufreq.sh
#   done
# (132/133 需 131 能免密或带密码 ssh 过去)
# ============================================================
