package tmp;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.PutIndexTemplateRequest;
import co.elastic.clients.elasticsearch.indices.PutIndexTemplateResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.clever.core.Conv;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/01/15 17:41 <br/>
 */
@Slf4j
public class ElasticsearchTest {
    @SneakyThrows
    @Test
    public void test01() {
        // === 1. 创建 ES 客户端（假设启用了安全认证）===
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials("elastic", "qZA4JRQ5MvEAzU3JNaxL"));

        RestClient restClient = RestClient.builder(new HttpHost("180.100.199.56", 9200, "http"))
            .setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
            .build();

        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);

        // === 2. 定义数据流名称和模板 ===
        String templateName = "template-api4-logs";
        String indexPattern = "api4-logs*";           // 模板匹配模式

        // === 3. 构建 mappings（按你的表结构映射）===
        Map<String, Property> properties = new HashMap<>();

        // log_id → long
        properties.put("log_id", Property.of(p -> p.long_(l -> l)));

        // 字符串字段（长度可能很大）→ text（支持全文搜索）
        properties.put("api_name", Property.of(p -> p.keyword(t -> t)));
        properties.put("server", Property.of(p -> p.keyword(t -> t)));
        properties.put("client", Property.of(p -> p.keyword(t -> t)));
        properties.put("url", Property.of(p -> p.text(t -> t)));
        properties.put("status", Property.of(p -> p.keyword(t -> t)));
        properties.put("err_msg", Property.of(p -> p.text(t -> t.index(true))));
        // 大文本字段（JSON/字符串）→ text，可选是否开启 indexing
        properties.put("req_data", Property.of(p -> p.text(t -> t.index(true))));
        properties.put("res_data", Property.of(p -> p.text(t -> t.index(true))));
        // 时间字段 → date（用 req_date 作为 @timestamp）
        properties.put("req_date", Property.of(p -> p.date(d -> d)));
        properties.put("res_date", Property.of(p -> p.date(d -> d)));

        // ⭐ 关键：添加 @timestamp 字段（数据流必需）
        properties.put("@timestamp", Property.of(p -> p.date(d -> d)));
        TypeMapping mapping = TypeMapping.of(m -> m.properties(properties));

        // === 5. 创建并发送 PutIndexTemplateRequest ===
        PutIndexTemplateRequest request = PutIndexTemplateRequest.of(builder ->
            builder
                .name(templateName)
                .indexPatterns(indexPattern)
                .dataStream(ds -> ds) // 👈 关键：启用数据流
                .template(tmpl -> tmpl
                    .mappings(mapping)
                    .settings(b -> b.numberOfShards("1").numberOfReplicas("0").index(b2->b2.mode("logsdb")))
                    .lifecycle(b -> b.enabled(true).dataRetention(b2 -> b2.time("90d")))
                )
        );
        PutIndexTemplateResponse response = client.indices().putIndexTemplate(request);
        log.info("✅ 索引模板创建成功: {}", response.acknowledged());
        // 清理
        restClient.close();
    }

    @SneakyThrows
    @Test
    public void test02() {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials("elastic", "qZA4JRQ5MvEAzU3JNaxL"));

        RestClient restClient = RestClient.builder(new HttpHost("180.100.199.56", 9200, "http"))
            .setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
            .build();

        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);
        String dataStreamName = "api4-logs-002";

        Path csvFile = Paths.get("D:\\api4_logs_1000000.csv");
        FileReader reader = new FileReader(csvFile.toFile());
        CSVParser csvParser = CSVFormat.DEFAULT
            // .withFirstRecordAsHeader()
            .withIgnoreEmptyLines()
            .withTrim()
            .parse(reader);

        List<BulkOperation> operations = new ArrayList<>(500);
        long count = 0;
        for (CSVRecord record : csvParser) {
            count++;
            String[] values = record.values();
            Map<String, Object> row = new HashMap<>(12);
            row.put("api_name", values[1]);
            row.put("client", values[2]);
            row.put("err_msg", values[3]);
            row.put("log_id", values[4]);
            row.put("req_data", values[5]);
            row.put("req_date", values[6]);
            row.put("res_data", values[7]);
            row.put("res_date", values[8]);
            row.put("server", values[9]);
            row.put("status", values[10]);
            row.put("url", values[11]);
            row.put("@timestamp", Conv.asDate(values[6]));
            BulkOperation operation = BulkOperation.of(op -> op
                .create(co -> co
                    .index(dataStreamName)   // ← 写入数据流名
                    .document(row)
                )
            );
            operations.add(operation);
            if (count % 500 == 0) {
                BulkRequest bulkRequest = BulkRequest.of(b -> b
                    .operations(operations)
                    .refresh(Refresh.True) // 可选：立即刷新使数据可见（影响性能，生产环境慎用）
                );
                BulkResponse response = client.bulk(bulkRequest);
                if (response.errors()) {
                    log.info("⚠️ Bulk 写入有错误:");
                    response.items().forEach(item -> {
                        if (item.error() != null) {
                            log.info("  - 错误: {}", item.error().reason());
                        }
                    });
                } else {
                    log.info("✅ 成功批量写入 {} 条日志到数据流: {}", count, dataStreamName);
                }
                operations.clear();
            }
        }
        csvParser.close();
        reader.close();
        restClient.close();
    }
}
