package tmp;

import com.github.luben.zstd.Zstd;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.clever.core.Conv;
import org.clever.core.id.SnowFlake;
import org.clever.core.mapper.JacksonMapper;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/01/19 14:11 <br/>
 */
@Slf4j
public class KafkaTest {
    public static Properties getCommonProps() {
//        String jaasConfig = "KafkaClient { "
//            + "org.apache.kafka.common.security.plain.PlainLoginModule required "
//            + "username=\"admin\" "
//            + "password=\"admin123567!\"; "
//            + "};";
//        System.setProperty("java.security.auth.login.config", jaasConfig);

        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.1.201:9092");
        props.put("client.id", "kafka-client-demo");
        // 如果启用了 SASL/SSL，需额外配置
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "PLAIN");
        props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"admin\" password=\"admin123567!\" user_admin=\"admin123567!\";");
        return props;
    }

    @SneakyThrows
    @Test
    public void test01() {
        Properties props = getCommonProps();
        Admin admin = Admin.create(props);
        String topic = "api4-logs";
        Set<String> existingTopics = admin.listTopics().names().get();
        if (existingTopics.contains(topic)) {
            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
            List<AlterConfigOp> ops = new ArrayList<>();
            Map<String, String> topicConfigs = new HashMap<>();
            for (Map.Entry<String, String> entry : topicConfigs.entrySet()) {
                ops.add(new AlterConfigOp(new ConfigEntry(entry.getKey(), entry.getValue()), AlterConfigOp.OpType.SET));
            }
            if (!ops.isEmpty()) {
                Map<ConfigResource, Collection<AlterConfigOp>> configOps = Collections.singletonMap(resource, ops);
                admin.incrementalAlterConfigs(configOps).all().get();
                log.info("更新成功");
            }
        } else {
            NewTopic newTopic = new NewTopic(topic, 1, (short) 1);
            CreateTopicsResult result = admin.createTopics(Collections.singleton(newTopic));
            result.all().get();
            log.info("创建成功");
        }
        admin.close();
    }

    @Test
    public void test02() {
        Properties props = getCommonProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        String topic = "api4-logs";
        for (int idx = 0; idx < 10; idx++) {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                topic,
                "key" + SnowFlake.SNOW_FLAKE.nextId(),
                "Hello Kafka!" + idx
            );
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error(exception.getMessage(), exception);
                } else {
                    log.info("Sent to partition {}, offset {}", metadata.partition(), metadata.offset());
                }
            });
        }
        producer.flush();
        producer.close();
    }

    @Test
    public void test03() {
        Properties props = getCommonProps();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group-01");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        String topic = "api4-logs";
        consumer.subscribe(List.of(topic));
        for (int idx = 0; idx < 30; idx++) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                log.info(
                    "Received: key={}, value={}, partition={}, offset={}",
                    record.key(), record.value(), record.partition(), record.offset()
                );
                TopicPartition partition = new TopicPartition(record.topic(), record.partition());
                OffsetAndMetadata offset = new OffsetAndMetadata(record.offset() + 1);
                Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = Collections.singletonMap(partition, offset);
                consumer.commitSync(offsetsToCommit);
            }
        }
        consumer.close();
    }

    @SneakyThrows
    @Test
    public void test04() {
        Properties props = getCommonProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        String topic = "api4-logs";

        Path csvFile = Paths.get("D:\\api4_logs_1000000.csv");
        FileReader reader = new FileReader(csvFile.toFile());
        CSVParser csvParser = CSVFormat.DEFAULT
            // .withFirstRecordAsHeader()
            .withIgnoreEmptyLines()
            .withTrim()
            .parse(reader);
        long count = 0;
        for (CSVRecord record : csvParser) {
            count++;
            String[] values = record.values();
            Map<String, Object> row = new HashMap<>(12);
            row.put("api_name", values[1]);
            row.put("client", values[2]);
            row.put("err_msg", values[3]);
            row.put("log_id", values[4]);
            row.put("req_data", StringUtils.truncate(values[5], 300 * 1024));
            row.put("req_date", values[6]);
            row.put("res_data", StringUtils.truncate(values[7], 300 * 1024));
            row.put("res_date", values[8]);
            row.put("server", values[9]);
            row.put("status", values[10]);
            row.put("url", values[11]);
            row.put("@timestamp", Conv.asDate(values[6]));
            String data = JacksonMapper.getInstance().toJson(row);
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                topic,
                values[4],
                data
            );
            producer.send(producerRecord, (metadata, exception) -> {
                if (exception != null) {
                    log.error(exception.getMessage(), exception);
                } else {
                    if (metadata.offset() % 10000 == 0) {
                        log.info("partition {}, offset {}", metadata.partition(), metadata.offset());
                    }
                }
            });
            if (count % 10000 == 0) {
                log.info("写入 {}", count);
            }
        }
        csvParser.close();
        reader.close();

        producer.flush();
        producer.close();
    }

    @Test
    public void test05() {
        String str = "Return the original size of a compressed buffer (if known)|Return the original size of a compressed buffer (if known)";
        byte[] src = str.getBytes(StandardCharsets.UTF_8);
        byte[] dst = zstdCompress(src);
        log.info("src={} | dst={} | raw={}", src.length, dst.length, new String(zstdDecompress(dst), StandardCharsets.UTF_8));
    }

    @Test
    public void test06() {
        Properties props = getCommonProps();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group-01");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        String topic = "api4-logs";
        consumer.subscribe(List.of(topic));
        int count = 0;
        final int max = 10;
        int noDataCount = -1;
        do {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            if (records.isEmpty()) {
                if (noDataCount >= 0) {
                    noDataCount++;
                }
            } else {
                noDataCount = 0;
            }
            for (ConsumerRecord<String, String> record : records) {
                count++;
                if (count % 10000 == 0) {
                    log.info("读取 {}", count);
                }
            }
            consumer.commitSync();
        } while (noDataCount <= max);
        log.info("读取 {}", count);
        // 991338
        consumer.close();
    }

    public static byte[] zstdCompress(byte[] src) {
        // 获取压缩后最大可能大小（用于分配 buffer）
        long maxCompressedSize = Zstd.compressBound(src.length);
        byte[] dst = new byte[(int) maxCompressedSize];
        // 执行压缩，返回实际压缩后长度
        long compressedSize = Zstd.compress(dst, src, 3); // 压缩级别 3（默认），范围 1～22

        if (Zstd.isError(compressedSize)) {
            throw new RuntimeException("Zstd compression failed: " + Zstd.getErrorName(compressedSize));
        }
        // 返回实际压缩后的字节数组（避免多余空间）
        return Arrays.copyOfRange(dst, 0, (int) compressedSize);
    }

    public static byte[] zstdDecompress(byte[] compressed) {
        // 先获取原始大小（可选，用于预分配 buffer）
        long decompressedSize = Zstd.getFrameContentSize(compressed);
        if (decompressedSize <= 0) {
            // 如果无法获取原始大小（某些流式压缩），可设一个上限或动态扩容
            decompressedSize = compressed.length * 4L; // 保守估计
        }
        byte[] dst = new byte[(int) decompressedSize];
        long actualSize = Zstd.decompress(dst, compressed);
        if (Zstd.isError(actualSize)) {
            throw new RuntimeException("Zstd decompression failed: " + Zstd.getErrorName(actualSize));
        }
        return Arrays.copyOfRange(dst, 0, (int) actualSize);
    }
}
