package tmp;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/01/02 23:03 <br/>
 */
@Slf4j
public class DebeziumTest {
    @SneakyThrows
    @Test
    public void test01() {
        // 1. 配置 Debezium 引擎
        Properties props = new Properties();
        props.setProperty("name", "mysql-connector");
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        props.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
        props.setProperty("offset.storage.file.filename", "/tmp/offsets.dat");
        props.setProperty("offset.flush.interval.ms", "60000");

        // MySQL 连接配置
        props.setProperty("database.hostname", "localhost");
        props.setProperty("database.port", "3306");
        props.setProperty("database.user", "debezium");
        props.setProperty("database.password", "dbz_password");
        props.setProperty("database.server.name", "my-mysql-server"); // 逻辑服务器名
        props.setProperty("database.include.list", "testdb");
        props.setProperty("table.include.list", "testdb.users");
        props.setProperty("database.history", "io.debezium.relational.history.FileDatabaseHistory");
        props.setProperty("database.history.file.filename", "/tmp/dbhistory.dat");

        // 2. 创建 Debezium 引擎（输出格式为 JSON）
        DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine.create(Json.class)
            .using(props)
            .notifying(record -> {
                // 3. 处理变更事件
                System.out.println("=== Received change event ===");
                System.out.println("Key: " + record.key());
                System.out.println("Value: " + record.value());
                System.out.println("Partition: " + record.partition());
                // System.out.println("Offset: " + record.offset());
                System.out.println("=============================\n");
            })
            .build();

        // 4. 启动引擎（在后台线程运行）
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);

        // 5. 等待用户输入后关闭
        System.out.println("Debezium engine started. Insert/update records in MySQL to see events.");
        int read = System.in.read();

        // 6. 关闭引擎
        engine.close();
        executor.shutdown();
        System.out.println("Engine stopped.");
    }
}
