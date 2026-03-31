package com.linkedin.drelephant.datautils;

import java.sql.*;
import java.nio.file.*;
import java.util.regex.*;
public class LogProcessor {
    // 添加之前讨论的数据处理方法
    public void processLogFile(String logPath, Connection conn) throws Exception {
        Pattern pattern = Pattern.compile("===== (.*?) =====.*?%idle\\s+(\\d+\\.\\d+)");
        String content = new String(Files.readAllBytes(Paths.get(logPath)));

        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO m_cpu_idle (process_time, sum, num) VALUES (?, ?, ?)")) {

            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                // 时间解析和数据库插入逻辑
                pstmt.setLong(1, parseTimestamp(matcher.group(1)));
                pstmt.setDouble(2, Double.parseDouble(matcher.group(2)));
                pstmt.setInt(3, 3);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private long parseTimestamp(String timestampStr) {
        // 实现时间格式转换
        return System.currentTimeMillis() / 1000; // 示例
    }

}
