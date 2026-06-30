import com.linkedin.drelephant.datautils.LogProcessor;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class C3P0ConnectionTest {
    public static void main(String[] args) {
        System.out.println("开始测试连接池------------------");
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        try {
            // 读取你的 C3P0 连接信息
            dataSource.setDriverClass("com.mysql.cj.jdbc.Driver");//JDBC驱动类全名
            dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/bd_monitor?useSSL=false&serverTimezone=UTC");
            dataSource.setUser("root");
            dataSource.setPassword("root");
            // 创建固定大小的线程池（6个线程）
            ExecutorService executor = Executors.newFixedThreadPool(8);

////            // 提交5个处理任务
            executor.submit(() -> processCpuData(dataSource));
            executor.submit(() -> processLoadData(dataSource));
            executor.submit(() -> processMemDiskData(dataSource));
            executor.submit(() -> processProcData(dataSource));
            executor.submit(() -> processNetworkData(dataSource));
            executor.submit(() -> {
                try {
                    processPowerData(dataSource);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            // 关闭线程池（不再接受新任务）
            executor.shutdown();

            // 等待所有任务完成（可选）
            while (!executor.isTerminated()) {
                Thread.sleep(1000);
            }

            System.out.println("所有数据处理任务已完成");

        } catch (Exception e) {
            System.err.println("❌ 连接失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            dataSource.close();
        }
    }

    private static void processCpuData(ComboPooledDataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            DataWriter writer = new DataWriter();
            writer.processLogFile("D:/cpulog.log");
            System.out.println("CPU数据处理完成");
        } catch (Exception e) {
            System.err.println("CPU数据处理失败: " + e.getMessage());
        }
    }

    private static void processLoadData(ComboPooledDataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            LoadDataWriter writer = new LoadDataWriter();
            writer.processAndInsert("D:/loadlog.log");
            System.out.println("负载数据处理完成");
        } catch (Exception e) {
            System.err.println("负载数据处理失败: " + e.getMessage());
        }
    }

    private static void processMemDiskData(ComboPooledDataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            MemDiskDataWriter writer = new MemDiskDataWriter();
            writer.processAndInsert("D:/mem_disk_log.log");
            System.out.println("内存磁盘数据处理完成");
        } catch (Exception e) {
            System.err.println("内存磁盘数据处理失败: " + e.getMessage());
        }
    }

    private static void processProcData(ComboPooledDataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            ProcDataWriter writer = new ProcDataWriter();
            writer.processAndInsert("D:/processlog.log");
            System.out.println("进程数据处理完成");
        } catch (Exception e) {
            System.err.println("进程数据处理失败: " + e.getMessage());
        }
    }

    private static void processNetworkData(ComboPooledDataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            NetworkDataWriter writer = new NetworkDataWriter();
            writer.processAndInsert("D:/netlog.log");
            System.out.println("网络数据处理完成");
        } catch (Exception e) {
            System.err.println("网络数据处理失败: " + e.getMessage());
        }
    }

    private static void processPowerData(ComboPooledDataSource dataSource) throws SQLException {
        try(Connection conn = dataSource.getConnection()){
        CsvPowerDataWriter writer = new CsvPowerDataWriter();

        Map<String, String> fileToTableMap = new HashMap<>();
        fileToTableMap.put("D:\\master.csv", "p_master_power");
        fileToTableMap.put("D:\\slave1.csv", "p_slave1_power");
        fileToTableMap.put("D:\\slave2.csv", "p_slave2_power");
//        writer.processAndInsertfileToTableMap();
        writer.processAndInsert(fileToTableMap);
      } catch (Exception e) {
            System.err.println("power数据处理失败: " + e.getMessage());
        }
    }
}