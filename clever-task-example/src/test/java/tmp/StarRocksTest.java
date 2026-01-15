package tmp;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.starrocks.data.load.stream.StreamLoadDataFormat;
import com.starrocks.data.load.stream.StreamLoadSnapshot;
import com.starrocks.data.load.stream.properties.StreamLoadProperties;
import com.starrocks.data.load.stream.properties.StreamLoadTableProperties;
import com.starrocks.data.load.stream.v2.StreamLoadManagerV2;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.clever.core.id.IDCreateUtils;
import org.clever.core.mapper.JacksonMapper;
import org.clever.data.jdbc.Jdbc;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/01/14 13:02 <br/>
 */
@Slf4j
public class StarRocksTest {
    private static final long CACHE_MAX_BYTES_100MB = 10 * 1024 * 1024;
    private static final String BASE_URL = "http://192.168.1.201:8030";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin123456";
    private static final String DB_NAME = "tushare";

    static {
        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "connection,content-length,expect,host,upgrade");
        Unirest.config()
            .connectTimeout(3_000)
            .requestTimeout(3_000)
            .connectionTTL(Duration.ofMinutes(10))
            .retryAfter(true, 3)
            .instrumentWith(request -> {
                long startNanos = System.nanoTime();
                log.info("---> 请求 [{}] {}", request.getHttpMethod(), request.getRawPath());
                return (response, ex) -> {
                    double cost = (System.nanoTime() - startNanos) / 1_000_000.0;
                    if (ex == null) {
                        log.info("<--- 响应 [{}] {} ({}ms)", response.getStatus(), request.getRawPath(), cost);
                    } else {
                        if (response == null) {
                            log.error("<--- 响应 [000] {} ({}ms)", request.getRawPath(), cost, ex);
                        } else {
                            log.error("<--- 响应 [{}] {} ({}ms)", response.getStatus(), request.getRawPath(), cost, ex);
                        }
                    }
                };
            });
    }

    public static void setLogLevel(String packageName, Level level) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(packageName);
        logger.setLevel(level);
    }

    @Test
    public void test01() {
        setLogLevel("com.starrocks.data.load.stream", Level.WARN);
        Jdbc jdbc = JdbcTest.createInnerJdbc();
        String tblName = "bas_item";
        String uniqueKey = DB_NAME + "." + tblName;
        StreamLoadTableProperties tableProps = StreamLoadTableProperties.builder()
            .uniqueKey(uniqueKey)
            .database(DB_NAME)
            .table(tblName)
            .streamLoadDataFormat(StreamLoadDataFormat.JSON)
            // .columns("item_id,owner_id")
            // .addProperty("format", "json")
            // .addCommonProperties()
            .enableUpsertDelete(true)
            .maxBufferRows(300)
            .build();
        StreamLoadProperties properties = StreamLoadProperties.builder()
            .loadUrls(BASE_URL)
            .connectTimeout(30_000)
            .socketTimeout(60_000)
            .username(USERNAME)
            .password(PASSWORD)
            .version("4.0.3")
            .enableTransaction()
            .labelPrefix("test-")
            .defaultTableProperties(tableProps)
            // .addTableProperties(tableProps)
            .cacheMaxBytes(CACHE_MAX_BYTES_100MB)
            .scanningFrequency(50)
            .ioThreadCount(1)
            .addHeader("format", "json")
            .addHeader("strip_outer_array", "true")
            .build();
        StreamLoadManagerV2 manager = new StreamLoadManagerV2(properties, false);
        manager.init();
        final long startTime = System.currentTimeMillis();
        final AtomicLong count = new AtomicLong(0);
        jdbc.queryForCursor("select * from bas_item limit 30000", 1000, rowData -> {
            String[] jsonRows = rowData.getRowDataList().stream()
                .map(row -> JacksonMapper.getInstance().toJson(row))
                .toList().toArray(new String[0]);
            manager.write(uniqueKey, DB_NAME, tblName, jsonRows);
            long cnt = count.addAndGet(jsonRows.length);
            if (cnt % 10000 == 0) {
                log.info("[写]数量: {}", cnt);
                manager.flush();
                StreamLoadSnapshot snapshot = manager.snapshot();
                manager.commit(snapshot);
                // manager.close();
            }
        });
        manager.flush();
        StreamLoadSnapshot snapshot = manager.snapshot();
        manager.commit(snapshot);
        log.info("[写]数量: {}", count.get());
        final long endTime = System.currentTimeMillis();
        log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count.get() * 1.0 / (endTime - startTime));
        jdbc.close();
        manager.close();
    }

    @Test
    public void test02() {
        UnirestInstance unirest = Unirest.primaryInstance();
        String tblName = "bas_item";

        Jdbc jdbc = JdbcTest.createInnerJdbc();
        final long startTime = System.currentTimeMillis();
        final AtomicLong count = new AtomicLong(0);
        jdbc.queryForCursor("select * from bas_item order by item_id -- limit 30000", 3000, rowData -> {
            String LABEL = "test02-" + IDCreateUtils.shortUuid();
            HttpResponse<String> beginResp = unirest.post(BASE_URL + "/api/transaction/begin")
                .basicAuth(USERNAME, PASSWORD)
                .header("label", LABEL)
                .header("Expect", "100-continue")
                .header("db", DB_NAME)
                .header("table", tblName)
                .asString();
            // log.info("开始事务 -> \n{}", beginResp.getBody());

            String[] jsonRows = rowData.getRowDataList().stream()
                .map(row -> JacksonMapper.getInstance().toJson(row))
                .toList().toArray(new String[0]);
            String body = StringUtils.join(jsonRows, "\n");
            HttpResponse<?> loadResp = unirest.put(BASE_URL + "/api/transaction/load")
                .basicAuth(USERNAME, PASSWORD)
                .header("label", LABEL)
                .header("Expect", "100-continue")
                .header("db", DB_NAME)
                .header("table", tblName)
                .header("format", "json")
                .header("strip_outer_array", "false")
                .body(body)
                .asEmpty();

            HttpResponse<String> prepareResp = unirest.post(BASE_URL + "/api/transaction/prepare")
                .basicAuth(USERNAME, PASSWORD)
                .header("label", LABEL)
                .header("Expect", "100-continue")
                .header("db", DB_NAME)
                .header("prepared_timeout", "300")
                .asString();
            // log.info("预提交事务 -> \n{}", prepareResp.getBody());

            HttpResponse<String> commitResp = unirest.post(BASE_URL + "/api/transaction/commit")
                .basicAuth(USERNAME, PASSWORD)
                .header("label", LABEL)
                .header("Expect", "100-continue")
                .header("db", DB_NAME)
                .asString();
            // log.info("提交事务 -> \n{}", commitResp.getBody());

            // log.info("写入数据 -> \n{}", loadResp.getBody());
            long cnt = count.addAndGet(jsonRows.length);
            log.info("[写]数量: {}", cnt);
        });

        log.info("[写]数量: {}", count.get());
        final long endTime = System.currentTimeMillis();
        log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count.get() * 1.0 / (endTime - startTime));

        jdbc.close();
        unirest.close();
    }


    @Test
    public void test03() {
        UnirestInstance unirest = Unirest.primaryInstance();
        String tblName = "bas_item";
        Jdbc jdbc = JdbcTest.createInnerJdbc();
        final long startTime = System.currentTimeMillis();
        final AtomicLong count = new AtomicLong(0);
        jdbc.queryForCursor("select * from bas_item order by item_id -- limit 30000", 10000, rowData -> {
            String LABEL = "test03-" + IDCreateUtils.shortUuid();
            String[] jsonRows = rowData.getRowDataList().stream()
                .map(row -> JacksonMapper.getInstance().toJson(row))
                .toList().toArray(new String[0]);
            String body = StringUtils.join(jsonRows, "\n");
            log.info("LABEL={}", LABEL);
            HttpResponse<?> loadResp = unirest.put(BASE_URL + "/api/" + DB_NAME + "/" + tblName + "/_stream_load")
                .basicAuth(USERNAME, PASSWORD)
                .header("label", LABEL)
                .header("Expect", "100-continue")
                .header("format", "json")
                .header("strip_outer_array", "false")
                .contentType("text/json; charset=utf-8")
                .body(body)
                .asString();
            long cnt = count.addAndGet(jsonRows.length);
            log.info("[写]数量: {} ->\n{}", cnt, loadResp.getBody());
        });
        log.info("[写]数量: {}", count.get());
        final long endTime = System.currentTimeMillis();
        log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count.get() * 1.0 / (endTime - startTime));
        jdbc.close();
        unirest.close();
    }

    @Test
    public void test04() {
        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "connection,content-length,expect,host,upgrade");
        String tblName = "bas_item";
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        Jdbc jdbc = JdbcTest.createInnerJdbc();
        final long startTime = System.currentTimeMillis();
        final AtomicLong count = new AtomicLong(0);
        jdbc.queryForCursor("select * from bas_item order by item_id -- limit 30000", 3000, rowData -> {
            String LABEL = "test03-" + IDCreateUtils.uuid();
            String[] jsonRows = rowData.getRowDataList().stream()
                .map(row -> JacksonMapper.getInstance().toJson(row))
                .toList().toArray(new String[0]);
            String body = StringUtils.join(jsonRows, "\n");
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/" + DB_NAME + "/" + tblName + "/_stream_load"))
                .timeout(Duration.ofMinutes(10))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()))
                .header("label", LABEL)
                .header("Expect", "100-continue")
                .header("format", "json")
                .header("strip_outer_array", "false")
                .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
            java.net.http.HttpResponse<String> resp;
            try {
                resp = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            long cnt = count.addAndGet(jsonRows.length);
            log.info("[写]数量: {} ->\n{}", cnt, resp.body());
            log.info("[写]数量: {}", cnt);
        });
        log.info("[写]数量: {}", count.get());
        final long endTime = System.currentTimeMillis();
        log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count.get() * 1.0 / (endTime - startTime));
        jdbc.close();
    }


}
