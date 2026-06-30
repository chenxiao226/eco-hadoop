import java.sql.*;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.regex.*;
import com.linkedin.drelephant.datautils.LogProcessor;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.io.File;
import java.sql.Connection;

public class DataWriter {
    private ComboPooledDataSource dataSource;

    public DataWriter() {
        dataSource = new ComboPooledDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/bd_monitor");
        dataSource.setUser("root");
        dataSource.setPassword("root");
    }

    public int processLogFile(String logPath) throws Exception {
        Connection conn = dataSource.getConnection();

        // 为每个表维护“上一次插入的 sum 值”，初始为 null 表示尚未插入
        Map<Integer, Double> lastSumMap = new HashMap<>();
        // key: 表索引 (0~5), value: 上一次插入的 sum 值
        for (int i = 0; i < 6; i++) {
            lastSumMap.put(i, null);
        }

        // 准备6个表的插入语句
        String[] insertSQLs = {
                "INSERT INTO m_cpu_user (process_time, sum, num) VALUES (?, ?, ?)",
                "INSERT INTO m_cpu_nice (process_time, sum, num) VALUES (?, ?, ?)",
                "INSERT INTO m_cpu_system (process_time, sum, num) VALUES (?, ?, ?)",
                "INSERT INTO m_cpu_wio (process_time, sum, num) VALUES (?, ?, ?)",
                "INSERT INTO m_cpu_steal (process_time, sum, num) VALUES (?, ?, ?)",
                "INSERT INTO m_cpu_idle (process_time, sum, num) VALUES (?, ?, ?)"
        };

        // 为每个表创建 PreparedStatement
        PreparedStatement[] pstmts = new PreparedStatement[6];
        for (int i = 0; i < 6; i++) {
            pstmts[i] = conn.prepareStatement(insertSQLs[i]);
        }

        // 读取日志文件
        String content = new String(Files.readAllBytes(Paths.get(logPath)));

        // 正则匹配日志块
        Pattern pattern = Pattern.compile(
                "===== (\\w{3} \\d{2} \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} [AP]M CST) =====.*?avg-cpu:.*?\\n\\s+" +
                        "(\\d+\\.\\d+)\\s+(\\d+\\.\\d+)\\s+(\\d+\\.\\d+)\\s+(\\d+\\.\\d+)\\s+(\\d+\\.\\d+)\\s+(\\d+\\.\\d+)",
                Pattern.DOTALL
        );

        Matcher matcher = pattern.matcher(content);
        int insertedCount = 0;

        try {
            while (matcher.find()) {
                long unixTime = parseTimestamp(matcher.group(1));

                // 处理每个表的 sum 值
                for (int i = 0; i < 6; i++) {
                    double currentSum = Double.parseDouble(matcher.group(i + 2));

                    // 获取上一次插入的 sum
                    Double lastSum = lastSumMap.get(i);

//                    // 如果上一次有值，且和当前值相同，跳过
//                    if (lastSum != null && Math.abs(lastSum - currentSum) < 1e-9) {
//                        continue; // 跳过该表的插入
//                    }

                    // 否则，插入当前值
                    pstmts[i].setLong(1, unixTime);
                    pstmts[i].setDouble(2, currentSum);
                    pstmts[i].setInt(3, 1);
                    pstmts[i].addBatch();

                    // 更新“上一次插入的 sum”
                    lastSumMap.put(i, currentSum);
                    insertedCount++;
                }

                // 每100条提交一次
                if (insertedCount % 100 == 0 && insertedCount > 0) {
                    for (PreparedStatement pstmt : pstmts) {
                        pstmt.executeBatch();
                    }
                    System.out.println("已处理 " + insertedCount + " 条非重复记录...");
                }
            }

            // 提交剩余批次
            for (PreparedStatement pstmt : pstmts) {
                pstmt.executeBatch();
            }
        } finally {
            for (PreparedStatement pstmt : pstmts) {
                if (pstmt != null) {
                    pstmt.close();
                }
            }
        }

        System.out.println("共插入 " + insertedCount + " 条去重后的新记录。");
        return insertedCount;
    }

    private long parseTimestamp(String timestampStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss a z", Locale.US);
            Date date = format.parse(timestampStr);
            return date.getTime() / 1000; // 秒级时间戳
        } catch (Exception e) {
            throw new RuntimeException("时间戳解析失败: " + timestampStr, e);
        }
    }
}
