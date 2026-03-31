-- ============================================================
-- Index 页面补充模拟数据
-- ============================================================
USE bd_monitor;

-- CPU idle 率（用于计算使用率：100 - idle/num）
CREATE TABLE IF NOT EXISTS `m_cpu_idle` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- CPU 核心数
CREATE TABLE IF NOT EXISTS `m_cpu_num` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统负载 load_one
CREATE TABLE IF NOT EXISTS `m_load_one` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 磁盘空闲（GB）
CREATE TABLE IF NOT EXISTS `m_disk_free` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 磁盘总量（GB）
CREATE TABLE IF NOT EXISTS `m_disk_total` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- RPC 平均处理时间（ms）
CREATE TABLE IF NOT EXISTS `m_rpc_rpc_rpcprocessingtimeavgtime` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `sum` DOUBLE NOT NULL,
    `num` INT DEFAULT 1,
    `process_time` BIGINT NOT NULL,
    INDEX idx_process_time (`process_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 插入模拟数据
-- ============================================================
DROP PROCEDURE IF EXISTS insert_index_mock_data;

DELIMITER $$
CREATE PROCEDURE insert_index_mock_data()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE base_time BIGINT;
    SET base_time = UNIX_TIMESTAMP() - 100 * 60; -- 从100分钟前开始，共100条

    WHILE i < 100 DO
        SET @t = base_time + i * 60;

        -- CPU idle: 集群3节点，num=3，sum=空闲率之和
        -- 使用率约30~60%，所以idle约40~70%，sum = idle * num
        INSERT INTO `m_cpu_idle` (sum, num, process_time)
        VALUES ((40 + (i * 17) % 30) * 3, 3, @t);

        -- CPU num: 每节点8核，3节点共24核，固定值
        INSERT INTO `m_cpu_num` (sum, num, process_time)
        VALUES (24, 1, @t);

        -- Load one: 集群平均负载，正常0.5~1.5
        INSERT INTO `m_load_one` (sum, num, process_time)
        VALUES (0.5 + (i * 7 % 100) / 100.0, 1, @t);

        -- Disk free (GB): 3节点总1500GB，使用35%，空闲约975GB，波动
        INSERT INTO `m_disk_free` (sum, num, process_time)
        VALUES (975 - (i % 20) * 0.5, 1, @t);

        -- Disk total (GB): 固定1500GB
        INSERT INTO `m_disk_total` (sum, num, process_time)
        VALUES (1500, 1, @t);

        -- RPC avg time (ms): 正常1~5ms
        INSERT INTO `m_rpc_rpc_rpcprocessingtimeavgtime` (sum, num, process_time)
        VALUES (1.5 + (i % 8) * 0.4, 1, @t);

        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL insert_index_mock_data();
DROP PROCEDURE IF EXISTS insert_index_mock_data;

-- 验证
SELECT 'm_cpu_idle' AS tbl, COUNT(*) AS cnt FROM `m_cpu_idle`
UNION ALL SELECT 'm_cpu_num', COUNT(*) FROM `m_cpu_num`
UNION ALL SELECT 'm_load_one', COUNT(*) FROM `m_load_one`
UNION ALL SELECT 'm_disk_free', COUNT(*) FROM `m_disk_free`
UNION ALL SELECT 'm_disk_total', COUNT(*) FROM `m_disk_total`
UNION ALL SELECT 'm_rpc_rpc_rpcprocessingtimeavgtime', COUNT(*) FROM `m_rpc_rpc_rpcprocessingtimeavgtime`;
