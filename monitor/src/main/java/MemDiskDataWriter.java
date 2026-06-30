import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class MemDiskDataWriter {
    private final ComboPooledDataSource dataSource;

    public MemDiskDataWriter() {
        dataSource = new ComboPooledDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/bd_monitor");
        dataSource.setUser("root");
        dataSource.setPassword("root");
    }

    public void processAndInsert(String logPath) {
        try (Connection conn = dataSource.getConnection()) {
            // 1. 确保所有表都存在（根据您的要求调整）
            createTablesIfNotExists(conn);

            // 2. 解析日志文件并插入数据
            int processedRows = processLogFile(logPath, conn);

            System.out.println("✅ 成功处理 " + processedRows + " 条内存/磁盘记录");

        } catch (Exception e) {
            System.err.println("❌ 处理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTablesIfNotExists(Connection conn) throws SQLException {
        // 内存相关表（移除m_mem_used，拆分buffcache）
        String[] memTables = {
                "m_mem_total", "m_mem_free", "m_mem_shared",
                "m_mem_buffers", "m_mem_cached"  // 拆分为两个表
        };

        // 磁盘相关表
        String[] diskTables = {
                "m_disk_total", "m_disk_free", "m_disk_usage"
        };

        // 创建所有表（统一结构）
        String createSQL = "CREATE TABLE IF NOT EXISTS %s (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "process_time BIGINT NOT NULL UNIQUE, " +
                "sum DOUBLE NOT NULL, " +
                "num INT NOT NULL DEFAULT 1" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            for (String table : memTables) {
                stmt.execute(String.format(createSQL, table));
            }
            for (String table : diskTables) {
                stmt.execute(String.format(createSQL, table));
            }
        }
    }

    private int processLogFile(String logPath, Connection conn) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(logPath)));

        // 正则匹配：时间戳 + 内存数据 + 磁盘数据
        Pattern pattern = Pattern.compile(
                "===== (\\w{3} \\d{2} \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} [AP]M \\w{3}) =====.*?" +
                        "MEM_TOTAL=(\\d+), MEM_USED=(\\d+), MEM_FREE=(\\d+), MEM_SHARED=(\\d+), MEM_BUFFCACHE=(\\d+).*?" +
                        "DISK_TOTAL=([\\d.]+)G, DISK_USED=([\\d.]+)G, DISK_FREE=([\\d.]+)G, DISK_USAGE=(\\d+)%",
                Pattern.DOTALL
        );

        // 准备所有表的插入语句（根据表结构调整）
        String[] insertSQLs = {
                // 内存表（不再使用m_mem_used）
                "INSERT INTO m_mem_total (process_time, sum, num) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1",
                "INSERT INTO m_mem_free (process_time, sum, num) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1",
                "INSERT INTO m_mem_shared (process_time, sum, num) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1",
                // MEM_BUFFCACHE 拆分为两个表（值相同）
                "INSERT INTO m_mem_buffers (process_time, sum, num) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1",
                "INSERT INTO m_mem_cached (process_time, sum, num) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1",

                // 磁盘表
                "INSERT INTO m_disk_total (process_time, sum, num) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1",
                "INSERT INTO m_disk_free (process_time, sum, num) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1",
                "INSERT INTO m_disk_usage (process_time, sum, num) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1"
        };

        PreparedStatement[] pstmts = new PreparedStatement[insertSQLs.length];
        for (int i = 0; i < insertSQLs.length; i++) {
            pstmts[i] = conn.prepareStatement(insertSQLs[i]);
        }

        int processedCount = 0;
        try {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                // 解析时间戳
                long unixTime = parseTimestamp(matcher.group(1));

                // 内存数据（单位：MB）
                int memTotal = Integer.parseInt(matcher.group(2));
                int memFree = Integer.parseInt(matcher.group(4));
                int memShared = Integer.parseInt(matcher.group(5));
                int memBuffCache = Integer.parseInt(matcher.group(6)); // 用于buffers和cached

                // 磁盘数据（去掉"G"单位）
                double diskTotal = Double.parseDouble(matcher.group(7));
                double diskFree = Double.parseDouble(matcher.group(9));
                int diskUsage = Integer.parseInt(matcher.group(10));

                // 设置内存表参数（跳过m_mem_used）
                pstmts[0].setLong(1, unixTime); pstmts[0].setDouble(2, memTotal); pstmts[0].setInt(3, 1);
                pstmts[1].setLong(1, unixTime); pstmts[1].setDouble(2, memFree); pstmts[1].setInt(3, 1);
                pstmts[2].setLong(1, unixTime); pstmts[2].setDouble(2, memShared); pstmts[2].setInt(3, 1);
                // MEM_BUFFCACHE 拆分为两个表（值相同）
                pstmts[3].setLong(1, unixTime); pstmts[3].setDouble(2, memBuffCache); pstmts[3].setInt(3, 1);
                pstmts[4].setLong(1, unixTime); pstmts[4].setDouble(2, memBuffCache); pstmts[4].setInt(3, 1);

                // 设置磁盘表参数
                pstmts[5].setLong(1, unixTime); pstmts[5].setDouble(2, diskTotal); pstmts[5].setInt(3, 1);
                pstmts[6].setLong(1, unixTime); pstmts[6].setDouble(2, diskFree); pstmts[6].setInt(3, 1);
                pstmts[7].setLong(1, unixTime); pstmts[7].setDouble(2, diskUsage); pstmts[7].setInt(3, 1);

                // 添加到批处理
                for (PreparedStatement pstmt : pstmts) {
                    pstmt.addBatch();
                }

                processedCount++;

                // 每100条提交一次
                if (processedCount % 100 == 0) {
                    for (PreparedStatement pstmt : pstmts) {
                        pstmt.executeBatch();
                    }
                    System.out.println("已处理内存和磁盘 " + processedCount + " 条记录...");
                }
            }

            // 提交剩余记录
            for (PreparedStatement pstmt : pstmts) {
                pstmt.executeBatch();
            }

        } finally {
            // 关闭所有PreparedStatement
            for (PreparedStatement pstmt : pstmts) {
                if (pstmt != null) pstmt.close();
            }
        }

        return processedCount;
    }

    // 时间戳解析方法（保持不变）
    private long parseTimestamp(String timestampStr) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss a z", Locale.US);
        Date date = sdf.parse(timestampStr);
        return date.getTime() / 1000;
    }
}