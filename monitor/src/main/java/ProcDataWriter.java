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
public class ProcDataWriter {
    private final ComboPooledDataSource dataSource;

    public ProcDataWriter() {
        dataSource = new ComboPooledDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/bd_monitor");
        dataSource.setUser("root");
        dataSource.setPassword("root");
    }

    public void processAndInsert(String logPath) {
        try (Connection conn = dataSource.getConnection()) {
            // 1. 确保表存在
            createTablesIfNotExists(conn);

            // 2. 解析日志文件并插入数据
            int insertedRows = processLogFile(logPath, conn);

            System.out.println("✅ 成功插入/更新 " + insertedRows + " 条线程记录");

        } catch (Exception e) {
            System.err.println("❌ 处理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTablesIfNotExists(Connection conn) throws SQLException {
        // 创建两个线程表（结构相同）
        String[] tables = {"m_proc_run", "m_proc_total"};
        String createSQL = "CREATE TABLE IF NOT EXISTS %s (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "process_time BIGINT NOT NULL UNIQUE, " +
                "sum DOUBLE NOT NULL, " +
                "num INT NOT NULL DEFAULT 1" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            for (String table : tables) {
                stmt.execute(String.format(createSQL, table));
            }
        }
    }

    private int processLogFile(String logPath, Connection conn) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(logPath)));

        // 正则匹配：时间戳 + 运行线程数 + 总线程数
        Pattern pattern = Pattern.compile(
                "===== (\\w{3} \\d{2} \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} [AP]M \\w{3}) ===== - (\\d+) - (\\d+)",
                Pattern.MULTILINE
        );

        // 准备两个表的插入语句
        String[] insertSQLs = {
                "INSERT INTO m_proc_run (process_time, sum, num) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1",
                "INSERT INTO m_proc_total (process_time, sum, num) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE sum=VALUES(sum), num=num+1"
        };

        PreparedStatement[] pstmts = new PreparedStatement[2];
        for (int i = 0; i < 2; i++) {
            pstmts[i] = conn.prepareStatement(insertSQLs[i]);
        }

        int processedCount = 0;
        try {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                // 解析时间戳
                long unixTime = parseTimestamp(matcher.group(1));

                // 解析线程数
                int procRun = Integer.parseInt(matcher.group(2));  // 运行线程数
                int procTotal = Integer.parseInt(matcher.group(3)); // 总线程数

                // 设置参数并添加到批处理
                pstmts[0].setLong(1, unixTime);
                pstmts[0].setDouble(2, procRun);
                pstmts[0].setInt(3, 1);
                pstmts[0].addBatch();

                pstmts[1].setLong(1, unixTime);
                pstmts[1].setDouble(2, procTotal);
                pstmts[1].setInt(3, 1);
                pstmts[1].addBatch();

                processedCount++;

                // 每100条提交一次
                if (processedCount % 100 == 0) {
                    pstmts[0].executeBatch();
                    pstmts[1].executeBatch();
                    System.out.println("已处理进程 " + processedCount + " 条记录...");
                }
            }

            // 提交剩余记录
            pstmts[0].executeBatch();
            pstmts[1].executeBatch();

        } finally {
            // 关闭PreparedStatement
            for (PreparedStatement pstmt : pstmts) {
                if (pstmt != null) pstmt.close();
            }
        }

        return processedCount;
    }

    // 时间戳解析方法（与之前相同）
    private long parseTimestamp(String timestampStr) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss a z", Locale.US);
        Date date = sdf.parse(timestampStr);
        return date.getTime() / 1000;
    }
}