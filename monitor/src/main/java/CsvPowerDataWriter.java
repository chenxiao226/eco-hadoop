
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.util.Locale;
import com.mchange.v2.c3p0.ComboPooledDataSource;
public class CsvPowerDataWriter {

    public static void main(String[] args) throws Exception {
        CsvPowerDataWriter writer = new CsvPowerDataWriter();
        Map<String, String> fileToTableMap = new LinkedHashMap<>();
        fileToTableMap.put("D:/master.csv", "p_master_power");
        fileToTableMap.put("D:/slave1.csv", "p_slave1_power");
        fileToTableMap.put("D:/slave2.csv", "p_slave2_power");
        writer.processAndInsert(fileToTableMap);
    }

    private ComboPooledDataSource dataSource;
//    private final Set<Double> masterPowerSet = new HashSet<>();
//    private final Set<Double> slave1PowerSet = new HashSet<>();
//    private final Set<Double> slave2PowerSet = new HashSet<>();

    public CsvPowerDataWriter() {
        dataSource = new ComboPooledDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/bd_monitor");
        dataSource.setUser("root");
        dataSource.setPassword("root");
    }
    public void processAndInsert(Map<String, String> fileToTableMap) throws Exception {
        for (Map.Entry<String, String> entry : fileToTableMap.entrySet()) {
            String csvPath = entry.getKey();
            String tableName = entry.getValue();

            System.out.println("开始处理文件: " + csvPath + " -> 表: " + tableName);

            try (Connection conn = dataSource.getConnection()) {
                int insertedCount = processCsvFile(csvPath, conn, tableName);
                System.out.println("成功插入 " + insertedCount + " 条记录到表 " + tableName);
            } catch (Exception e) {
                System.err.println("处理文件 " + csvPath + " 时出错: " + e.getMessage());
                throw e;
            }
        }
    }

    private int processCsvFile(String csvPath, Connection conn, String tableName)
            throws Exception {
        String insertSQL = "INSERT INTO " + tableName + " (process_time, sum, num) VALUES (?, ?, ?)";

        int insertedCount = 0;
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            // 读取CSV文件所有行
//            java.util.List<String> lines = Files.readAllLines(Paths.get(csvPath));
            List<String> lines = Files.readAllLines(Paths.get(csvPath), Charset.forName("GBK"));

            for (int i = 1; i < lines.size(); i++) { // 从第2行开始（跳过标题行）


                String line = lines.get(i);

                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                // 解析CSV行（制表符或逗号分隔）
                String[] columns = line.split("\t|,");


                if (columns.length < 5) {
                    System.out.println("⚠️ 第 " + (i+1) + " 行列数不足，跳过: " + line);
                    continue;
                }

                // 解析时间（A列，索引0），去除BOM
                String timeStr = columns[0].replace("\"", "").replace("\uFEFF", "").trim();

                if (timeStr.isEmpty()) {
                    continue;
                }

                // 解析有功功率（E列，索引4）
                String powerStr = columns[4].replace("\"", "").trim();
                if (powerStr.isEmpty()) {
                    continue;
                }

                double powerValue;
                try {
                    powerValue = Double.parseDouble(powerStr);
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ 第 " + (i+1) + " 行功率值解析失败: " + powerStr);
                    continue;
                }


                long unixTime;
                try {

                    unixTime = parseTimeToUnixTimestamp(timeStr);
                } catch (Exception e) {
                    System.out.println("⚠️ 第 " + (i+1) + " 行时间解析失败: " + timeStr);
                    continue;
                }

                // 插入数据
                pstmt.setLong(1, unixTime);
                pstmt.setDouble(2, powerValue);
                pstmt.setInt(3, 1);
                pstmt.addBatch();
                insertedCount++;

                if (insertedCount % 100 == 0) {
                    pstmt.executeBatch();
                    System.out.println("已处理 " + i + " 行，成功插入 " + insertedCount + " 条记录");
                }
            }

            // 执行剩余批次
            pstmt.executeBatch();
            System.out.println("处理完成: 总共处理 " + (lines.size()-1) + " 行，成功插入 " + insertedCount + " 条记录");
        }

        return insertedCount;
    }

    private void createTableIfNotExists(Connection conn, String tableName) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "process_time BIGINT NOT NULL, " +
                "sum DOUBLE NOT NULL, " +
                "num INT NOT NULL DEFAULT 0" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    private long parseTimeToUnixTimestamp(String sdf) throws Exception {
        System.out.println("传过来的参数：" + sdf);
        String timeString = sdf.trim();
        System.out.println("提取出来的时间" + timeString);

        String correctDate = java.time.LocalDate.now().toString();
        String correctedDateTime = correctDate + " " + timeString + " CST";
        System.out.println("合并的时间" + " "+ correctedDateTime + " " + timeString);

//        时间解析器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss zzz"
        );

        ZonedDateTime zonedDateTime = ZonedDateTime.parse(correctedDateTime, formatter);
        long unixTimestamp = zonedDateTime.toEpochSecond();
        return  unixTimestamp;
    }


}
