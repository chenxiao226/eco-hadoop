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
public class NetworkDataWriter {
    private final ComboPooledDataSource dataSource;
    private static final double AVG_PACKET_SIZE = 1500.0; // 假设平均包大小为1500 bytes

    public NetworkDataWriter() {
        dataSource = new ComboPooledDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/bd_monitor");
        dataSource.setUser("root");
        dataSource.setPassword("root");
    }

    public void processAndInsert(String logPath) {
        try (Connection conn = dataSource.getConnection()) {
            // 1. 确保所有表存在
            createTablesIfNotExists(conn);

            // 2. 解析日志文件并插入数据
            int processedRows = processLogFile(logPath, conn);

            System.out.println("✅ 成功处理 " + processedRows + " 条网络记录");

        } catch (Exception e) {
            System.err.println("❌ 处理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTablesIfNotExists(Connection conn) throws SQLException {
        // 字节速率表（bytes/s）
        String[] byteTables = {"m_bytes_in", "m_bytes_out"};
        // 数据包速率表（packets/s）
        String[] packetTables = {"m_pkts_in", "m_pkts_out"};

        String createTableSQL = "CREATE TABLE IF NOT EXISTS %s (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "process_time BIGINT NOT NULL UNIQUE, " +
                "sum DOUBLE NOT NULL, " +
                "num INT NOT NULL DEFAULT 1" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            // 创建字节速率表
            for (String table : byteTables) {
                stmt.execute(String.format(createTableSQL, table));
            }
            // 创建数据包速率表
            for (String table : packetTables) {
                stmt.execute(String.format(createTableSQL, table));
            }
        }
    }

    private int processLogFile(String logPath, Connection conn) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(logPath)));

        // 正则匹配：时间戳 + 输入速率 + 输出速率（假设原始单位是KB/s）
        Pattern pattern = Pattern.compile(
                "===== (\\w{3} \\d{2} \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} [AP]M \\w{3}) ===== -\\s+(\\d+\\.\\d+)\\s+(\\d+\\.\\d+)",
                Pattern.MULTILINE
        );

        // 准备4个表的插入语句
        String[] insertSQLs = {
                // 字节速率（bytes/s）
                "INSERT INTO m_bytes_in (process_time, sum, num) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1",
                "INSERT INTO m_bytes_out (process_time, sum, num) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1",
                // 数据包速率（packets/s）
                "INSERT INTO m_pkts_in (process_time, sum, num) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1",
                "INSERT INTO m_pkts_out (process_time, sum, num) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1"
        };

        PreparedStatement[] pstmts = new PreparedStatement[4];
        for (int i = 0; i < 4; i++) {
            pstmts[i] = conn.prepareStatement(insertSQLs[i]);
        }

        int processedCount = 0;
        try {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                // 解析时间戳
                long unixTime = parseTimestamp(matcher.group(1));

                // 原始数据（假设单位是KB/s）
                double kBytesIn = Double.parseDouble(matcher.group(2));
                double kBytesOut = Double.parseDouble(matcher.group(3));

                // 转换为bytes/s
                double bytesIn = kBytesIn * 1024;
                double bytesOut = kBytesOut * 1024;

                // 计算数据包速率（packets/s）
                double pktsIn = bytesIn / AVG_PACKET_SIZE;
                double pktsOut = bytesOut / AVG_PACKET_SIZE;

                // 设置参数并添加到批处理
                // 字节速率表
                pstmts[0].setLong(1, unixTime); pstmts[0].setDouble(2, bytesIn); pstmts[0].setInt(3, 1);
                pstmts[1].setLong(1, unixTime); pstmts[1].setDouble(2, bytesOut); pstmts[1].setInt(3, 1);
                // 数据包速率表
                pstmts[2].setLong(1, unixTime); pstmts[2].setDouble(2, pktsIn); pstmts[2].setInt(3, 1);
                pstmts[3].setLong(1, unixTime); pstmts[3].setDouble(2, pktsOut); pstmts[3].setInt(3, 1);

                for (PreparedStatement pstmt : pstmts) {
                    pstmt.addBatch();
                }

                processedCount++;

                // 每100条提交一次
                if (processedCount % 100 == 0) {
                    for (PreparedStatement pstmt : pstmts) {
                        pstmt.executeBatch();
                    }
                    System.out.println("已处理网络 " + processedCount + " 条记录...");
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