-- ============================================================
-- Monitor 项目初始化脚本
-- 建库、建表、插入模拟数据（接近真实 Hadoop 集群数值）
-- process_time 使用 Unix 时间戳（秒），从当前时间往前推20条
-- ============================================================

CREATE DATABASE IF NOT EXISTS bd_monitor DEFAULT CHARACTER SET utf8mb4;
USE bd_monitor;

-- ============================================================
-- 通用建表宏：每张指标表结构完全相同
-- ============================================================

-- NameNode JVM 指标
CREATE TABLE IF NOT EXISTS `m_jvm_jvmmetrics_gctimemillis` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_jvm_jvmmetrics_gccount` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_jvm_jvmmetrics_memheapusedm` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_jvm_jvmmetrics_memheapmaxm` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_jvm_jvmmetrics_threadsblocked` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_jvm_jvmmetrics_threadswaiting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- NameNode DFS 指标
CREATE TABLE IF NOT EXISTS `m_dfs_fsnamesystem_totalfiles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_dfs_fsnamesystem_blockstotal` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_dfs_fsnamesystem_corruptblocks` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_dfs_fsnamesystem_missingblocks` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_dfs_fsnamesystem_capacitytotal` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_dfs_fsnamesystem_capacityused` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- DataNode 指标
CREATE TABLE IF NOT EXISTS `m_dfs_datanode_bytesread` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_dfs_datanode_byteswritten` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_dfs_datanode_blocksread` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_dfs_datanode_blockwritten` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_dfs_datanode_readblockopavgtime` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_dfs_datanode_writeblockopavgtime` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Memory 指标（单位 MB，集群3节点合计约 48GB = 49152 MB）
CREATE TABLE IF NOT EXISTS `m_mem_total` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_mem_free` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_mem_buffers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_mem_cached` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_mem_shared` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_swap_total` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `m_swap_free` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 插入模拟数据
-- 基准时间：UNIX_TIMESTAMP() 当前时间，往前推20条，每条间隔60秒
-- ============================================================

-- 用存储过程批量插入，避免手写20条重复SQL
DROP PROCEDURE IF EXISTS insert_mock_data;

DELIMITER $$
CREATE PROCEDURE insert_mock_data()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE base_time BIGINT;
    SET base_time = UNIX_TIMESTAMP() - 20 * 60; -- 从20分钟前开始

    WHILE i < 20 DO
        SET @t = base_time + i * 60;

        -- NameNode GC Time (ms): 正常值 50~300ms，模拟波动
        INSERT INTO `m_jvm_jvmmetrics_gctimemillis` (sum, num, process_time)
        VALUES (80 + (i * 7) % 220, 1, @t);

        -- NameNode GC Count: 累计值，缓慢增长
        INSERT INTO `m_jvm_jvmmetrics_gccount` (sum, num, process_time)
        VALUES (100 + i * 3, 1, @t);

        -- NameNode MemHeapUsedM (MB): 正常 200~600MB
        INSERT INTO `m_jvm_jvmmetrics_memheapusedm` (sum, num, process_time)
        VALUES (320 + (i * 11) % 280, 1, @t);

        -- NameNode MemHeapMaxM (MB): 固定上限 1024MB
        INSERT INTO `m_jvm_jvmmetrics_memheapmaxm` (sum, num, process_time)
        VALUES (1024, 1, @t);

        -- ThreadsBlocked: 正常 0~5
        INSERT INTO `m_jvm_jvmmetrics_threadsblocked` (sum, num, process_time)
        VALUES ((i * 3) % 6, 1, @t);

        -- ThreadsWaiting: 正常 10~30
        INSERT INTO `m_jvm_jvmmetrics_threadswaiting` (sum, num, process_time)
        VALUES (15 + (i * 2) % 15, 1, @t);

        -- TotalFiles: 缓慢增长，模拟真实集群
        INSERT INTO `m_dfs_fsnamesystem_totalfiles` (sum, num, process_time)
        VALUES (12480 + i * 5, 1, @t);

        -- BlocksTotal
        INSERT INTO `m_dfs_fsnamesystem_blockstotal` (sum, num, process_time)
        VALUES (8320 + i * 3, 1, @t);

        -- CorruptBlocks: 正常为0
        INSERT INTO `m_dfs_fsnamesystem_corruptblocks` (sum, num, process_time)
        VALUES (0, 1, @t);

        -- MissingBlocks: 正常为0
        INSERT INTO `m_dfs_fsnamesystem_missingblocks` (sum, num, process_time)
        VALUES (0, 1, @t);

        -- CapacityTotal (bytes): 3节点 * 500GB = 1.5TB = 1610612736000 bytes
        INSERT INTO `m_dfs_fsnamesystem_capacitytotal` (sum, num, process_time)
        VALUES (1610612736000, 1, @t);

        -- CapacityUsed (bytes): 约使用35% = 563714457600 bytes
        INSERT INTO `m_dfs_fsnamesystem_capacityused` (sum, num, process_time)
        VALUES (563714457600 + i * 1048576, 1, @t);

        -- DataNode BytesRead (bytes): 累计值，持续增长
        INSERT INTO `m_dfs_datanode_bytesread` (sum, num, process_time)
        VALUES (10737418240 + i * 52428800, 1, @t);

        -- DataNode BytesWritten (bytes)
        INSERT INTO `m_dfs_datanode_byteswritten` (sum, num, process_time)
        VALUES (5368709120 + i * 31457280, 1, @t);

        -- DataNode BlocksRead: 累计值
        INSERT INTO `m_dfs_datanode_blocksread` (sum, num, process_time)
        VALUES (2048 + i * 12, 1, @t);

        -- DataNode BlocksWritten
        INSERT INTO `m_dfs_datanode_blockwritten` (sum, num, process_time)
        VALUES (1024 + i * 8, 1, @t);

        -- ReadBlockOpAvgTime (ms): 正常 1~10ms
        INSERT INTO `m_dfs_datanode_readblockopavgtime` (sum, num, process_time)
        VALUES (2.5 + (i % 7) * 0.8, 1, @t);

        -- WriteBlockOpAvgTime (ms)
        INSERT INTO `m_dfs_datanode_writeblockopavgtime` (sum, num, process_time)
        VALUES (3.2 + (i % 5) * 1.1, 1, @t);

        -- Memory Total (MB): 3节点合计 48GB = 49152 MB，固定值
        INSERT INTO `m_mem_total` (sum, num, process_time)
        VALUES (49152, 1, @t);

        -- Memory Free (MB): 波动在 15000~25000 MB
        INSERT INTO `m_mem_free` (sum, num, process_time)
        VALUES (18432 + (i * 317) % 6144, 1, @t);

        -- Memory Buffers (MB)
        INSERT INTO `m_mem_buffers` (sum, num, process_time)
        VALUES (512 + (i * 13) % 256, 1, @t);

        -- Memory Cached (MB)
        INSERT INTO `m_mem_cached` (sum, num, process_time)
        VALUES (8192 + (i * 97) % 2048, 1, @t);

        -- Memory Shared (MB)
        INSERT INTO `m_mem_shared` (sum, num, process_time)
        VALUES (128 + (i * 7) % 64, 1, @t);

        -- Swap Total (MB): 固定 8GB = 8192 MB
        INSERT INTO `m_swap_total` (sum, num, process_time)
        VALUES (8192, 1, @t);

        -- Swap Free (MB): 基本不用
        INSERT INTO `m_swap_free` (sum, num, process_time)
        VALUES (7800 + (i * 3) % 200, 1, @t);

        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL insert_mock_data();
DROP PROCEDURE IF EXISTS insert_mock_data;

-- 验证插入结果
SELECT 'm_jvm_jvmmetrics_gctimemillis' AS tbl, COUNT(*) AS cnt FROM `m_jvm_jvmmetrics_gctimemillis`
UNION ALL SELECT 'm_jvm_jvmmetrics_gccount', COUNT(*) FROM `m_jvm_jvmmetrics_gccount`
UNION ALL SELECT 'm_jvm_jvmmetrics_memheapusedm', COUNT(*) FROM `m_jvm_jvmmetrics_memheapusedm`
UNION ALL SELECT 'm_dfs_fsnamesystem_totalfiles', COUNT(*) FROM `m_dfs_fsnamesystem_totalfiles`
UNION ALL SELECT 'm_dfs_datanode_bytesread', COUNT(*) FROM `m_dfs_datanode_bytesread`
UNION ALL SELECT 'm_mem_total', COUNT(*) FROM `m_mem_total`
UNION ALL SELECT 'm_mem_free', COUNT(*) FROM `m_mem_free`;
