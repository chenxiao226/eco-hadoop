import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mchange.v2.c3p0.ComboPooledDataSource;


public class LoadDataWriter {
    private final ComboPooledDataSource dataSource;

    public LoadDataWriter() {
        dataSource = new ComboPooledDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/bd_monitor");
        dataSource.setUser("root");
        dataSource.setPassword("root");
    }

    public void processAndInsert(String logPath) {
        try (Connection conn = dataSource.getConnection()) {
            // 1. 确保表存在（保持与之前相同的表结构）
            createTableIfNotExists(conn);

            // 2. 解析日志文件并插入数据
            int insertedRows = processLogFile(logPath, conn);

            System.out.println("✅ 成功插入/更新 " + insertedRows + " 条负载记录");

        } catch (Exception e) {
            System.err.println("❌ 处理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private int processLogFile(String logPath, Connection conn) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(logPath)));
        // 精准匹配你的日志格式
        Pattern pattern = Pattern.compile(
                "^=====\\s*(.*?)\\s*=====.*?load average:\\s*([0-9]+\\.[0-9]+)"
        );

        // 插入语句（sum字段存储load_1min的值）
        String insertSQL = "INSERT INTO m_load_one (process_time, sum, num) " + "VALUES (?, ?, ?) " ;
        int processedCount = 0;
        int insertedCount = 0;
        int totalLines = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(logPath));
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            String line;
            while ((line = br.readLine()) != null) {
                totalLines++;
                System.out.println("读取第 " + totalLines + " 行: " + (line.length() > 50 ? line.substring(0, 50) + "..." : line));

                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String timestampStr = matcher.group(1);

                    String loadValue = matcher.group(2);

                    System.out.println("处理记录: 时间戳=" + timestampStr + ", 负载值=" + loadValue);
                    // 将时间戳字符串转换为Unix时间戳（毫秒）
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss a z", Locale.ENGLISH);
                    Date date = sdf.parse(timestampStr);
                    long unixTimestamp = date.getTime(); // 毫秒级时间戳
                    unixTimestamp = unixTimestamp /1000;
                    // 设置3个参数 - 注意顺序要和SQL中的字段顺序一致
                    pstmt.setLong(1, unixTimestamp);              // process_time - BIGINT
                    pstmt.setDouble(2, Double.parseDouble(loadValue)); // sum - DOUBLE
                    pstmt.setInt(3, 1);                // num - INT

                    pstmt.executeUpdate();
                    processedCount++;
                    totalLines++;


                } else {
                    System.out.println("✗ 未匹配: " + line);
                }
            }
        }

//        System.out.println("总计读取 " + totalLines + " 行，成功处理 " + processedCount + " 条记录");
        return processedCount;
    }



    private void createTableIfNotExists(Connection conn) throws SQLException {
        // 保持与之前相同的表结构：id, sum, num, process_time
        String createSQL = "CREATE TABLE IF NOT EXISTS m_load_one (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "process_time BIGINT NOT NULL, " +  // 唯一约束避免重复
                "sum DOUBLE NOT NULL, " +                  // 存储 load_1min 的值
                "num INT NOT NULL" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createSQL);
        }
    }

    // 时间戳解析方法（保持不变）
    private long parseTimestamp(String timestampStr) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss a z", Locale.US);
        Date date = sdf.parse(timestampStr);
        return date.getTime() / 1000;  // 转为Unix时间戳（秒）
    }
}

