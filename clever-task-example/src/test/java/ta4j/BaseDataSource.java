package ta4j;

import com.zaxxer.hikari.HikariConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.clever.core.function.OneConsumer;
import org.clever.data.jdbc.Jdbc;
import ta4j.model.StockBarData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/21 12:23 <br/>
 */
@Slf4j
public class BaseDataSource {
    public static Jdbc createDorisJdbc() {
        HikariConfig hikariConfig = new HikariConfig();
        //hikariConfig.setDriverClassName("org.apache.arrow.driver.jdbc.ArrowFlightJdbcDriver");
        hikariConfig.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
        hikariConfig.setJdbcUrl("jdbc:p6spy:arrow-flight-sql://192.168.1.201:8070?useServerPrepStmts=false&cachePrepStmts=true&useSSL=false&useEncryption=false");
        hikariConfig.setUsername("admin");
        hikariConfig.setPassword("admin123456");
        hikariConfig.setAutoCommit(false);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(512);
        return new Jdbc(hikariConfig);
    }

    public static void get1dkBar(String stockCode, OneConsumer<StockBarData> onBar) {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from xtquant.stock_1dk_bar ");
        sql.append("where suspendFlag=0 ");
        sql.append(String.format("and stock_code='%s' ", stockCode));
        sql.append("order by time asc ");
        // sql.append("limit 100");
        try (Jdbc jdbc = createDorisJdbc()) {
            jdbc.queryForCursor(
                sql.toString(),
                rowData -> {
                    StockBarData data = rowData.getRowData(StockBarData.class);
                    onBar.call(data);
                }
            );
        }
    }

    private static Properties getKafkaCommonProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.1.201:9092");
        props.put("client.id", "kafka-client-demo");
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "PLAIN");
        props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"admin\" password=\"admin123567!\" user_admin=\"admin123567!\";");
        return props;
    }

    public static Admin createKafkaAdmin() {
        Properties props = getKafkaCommonProps();
        return Admin.create(props);
    }

    public static KafkaProducer<String, String> createKafkaProducer() {
        Properties props = getKafkaCommonProps();
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 50 * 1024 * 1024);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }

    public static KafkaConsumer<String, String> createKafkaConsumer(String groupId, OneConsumer<Properties> customProps) {
        Properties props = getKafkaCommonProps();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 100);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 3000);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 50 * 1024 * 1024);
        if (customProps != null) {
            customProps.call(props);
        }
        return new KafkaConsumer<>(props);
    }

    public static KafkaConsumer<String, String> createKafkaConsumer(String groupId) {
        return createKafkaConsumer(groupId, null);
    }

    @SneakyThrows
    public static void createTopic(Admin admin, String topic) {
        Set<String> existingTopics = admin.listTopics().names().get();
        if (!existingTopics.contains(topic)) {
            NewTopic newTopic = new NewTopic(topic, 1, (short) 1);
            newTopic.configs(new HashMap<>() {{
                // 数据保留时间 3600000(1小时) | 21600000(6小时) | 43200000(12小时) | 86400000(1天) | 172800000(2天) | 604800000(7天)
                put("retention.ms", "172800000");
                put("cleanup.policy", "delete");
                put("compression.type", "zstd");
            }});
            CreateTopicsResult result = admin.createTopics(Collections.singleton(newTopic));
            result.all().get();
            log.info("创建Topic成功: {}", topic);
        }
    }
}
