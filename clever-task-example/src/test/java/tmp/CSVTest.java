package tmp;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import kong.unirest.core.java.UnirestHttpClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.clever.core.id.IDCreateUtils;
import org.clever.core.mapper.JacksonMapper;
import org.clever.data.jdbc.support.SqlLoggerUtils;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/01/15 11:33 <br/>
 */
@Slf4j
public class CSVTest {
    private static final long CACHE_MAX_BYTES_100MB = 10 * 1024 * 1024;
    private static final String BASE_URL = "http://192.168.1.201:8030";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin123456";
    private static final String DB_NAME = "tushare";

    static {
        // System.setProperty("jdk.httpclient.allowRestrictedHeaders", "connection,content-length,expect,host,upgrade");
        Unirest.config()
            .connectTimeout(3_000)
            .requestTimeout(60_000)
            .connectionTTL(Duration.ofMinutes(10))
            .httpClient(UnirestHttpClient::new)
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

    @SneakyThrows
    @Test
    public void test01() {
        UnirestInstance unirest = Unirest.primaryInstance();
        String tblName = "api4_logs";
        Path csvFile = Paths.get("D:\\api4_logs_1000000.csv");
        FileReader reader = new FileReader(csvFile.toFile());
        CSVParser csvParser = CSVFormat.DEFAULT
            // .withFirstRecordAsHeader()
            .withIgnoreEmptyLines()
            .withTrim()
            .parse(reader);
        StringBuilder stringBuilder = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(stringBuilder, CSVFormat.DEFAULT);
        long count = 0;
        for (CSVRecord record : csvParser) {
            count++;
            String[] values = record.values();
            printer.printRecord(
                values[1],
                values[2],
                values[3],
                // values[4],
                String.valueOf(count),
                StringUtils.truncate(values[5], 300 * 1024),
                values[6],
                StringUtils.truncate(values[7], 300 * 1024),
                values[8],
                values[9],
                values[10],
                values[11]
            );
            if (count % 5000 == 0 || stringBuilder.length() >= (1024 * 1024 * 30)) {
                for (int i = 0; i < 10; i++) {
                    String LABEL = "test01-" + IDCreateUtils.uuid();
                    log.info("LABEL={}", LABEL);
                    try {
                        HttpResponse<String> loadResp = unirest.put(BASE_URL + "/api/" + DB_NAME + "/" + tblName + "/_stream_load")
                            .basicAuth(USERNAME, PASSWORD)
                            .header("label", LABEL)
                            .header("Expect", "100-continue")
                            .header("format", "CSV")
                            .header("column_separator", ",")
                            .header("enclose", "\"")
                            .header("columns", "api_name,client,err_msg,log_id,req_data,req_date,res_data,res_date,server,status,url")
                            .body(stringBuilder.toString())
                            .asString();
                        log.info("[写]数量: {} -> {}", count, SqlLoggerUtils.deleteWhitespace(loadResp.getBody()));
                        // log.info("--> \n\n{}\n\n", stringWriter.toString());
                        printer.close();
                        stringBuilder.delete(0, stringBuilder.length());
                        // printer = new CSVPrinter(stringWriter, CSVFormat.DEFAULT);
                        break;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            // if (count >= 30) break;
        }
        if (!stringBuilder.isEmpty()) {
            String LABEL = "test01-" + IDCreateUtils.uuid();
            log.info("LABEL={}", LABEL);
            HttpResponse<?> loadResp = unirest.put(BASE_URL + "/api/" + DB_NAME + "/" + tblName + "/_stream_load")
                .basicAuth(USERNAME, PASSWORD)
                .header("label", LABEL)
                .header("Expect", "100-continue")
                .header("format", "CSV")
                .header("column_separator", ",")
                .header("enclose", "\"")
                .header("columns", "api_name,client,err_msg,log_id,req_data,req_date,res_data,res_date,server,status,url")
                .body(stringBuilder.toString())
                .asString();
            log.info("[写]数量: {} ->\n{}", count, loadResp.getBody());
        }
        printer.close();
        stringBuilder.delete(0, stringBuilder.length());

        csvParser.close();
        reader.close();
        unirest.close();
    }

    @SneakyThrows
    @Test
    public void test02() {
        UnirestInstance unirest = Unirest.primaryInstance();
        String tblName = "api4_logs";
        Path csvFile = Paths.get("D:\\api4_logs_1000000.csv");
        FileReader reader = new FileReader(csvFile.toFile());
        CSVParser csvParser = CSVFormat.DEFAULT
            // .withFirstRecordAsHeader()
            .withIgnoreEmptyLines()
            .withTrim()
            .parse(reader);
        StringBuilder body = new StringBuilder();
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
            body.append(JacksonMapper.getInstance().toJson(row)).append("\n");
            if (count % 1000 == 0 || body.length() >= (1024 * 1024 * 30)) {
                final String LABEL = "test01-" + IDCreateUtils.uuid();
                log.info("LABEL={}", LABEL);
                HttpResponse<String> beginResp = unirest.post(BASE_URL + "/api/transaction/begin")
                    .basicAuth(USERNAME, PASSWORD)
                    .header("label", LABEL)
                    .header("Expect", "100-continue")
                    .header("db", DB_NAME)
                    .header("table", tblName)
                    .header("timeout", "3600")
                    .asString();
                // log.info("开始事务 -> \n{}", beginResp.getBody());

                for (int i = 0; i < 10; i++) {
                    try {
                        HttpResponse<?> loadResp = unirest.put(BASE_URL + "/api/transaction/load")
                            .basicAuth(USERNAME, PASSWORD)
                            .header("label", LABEL)
                            .header("Expect", "100-continue")
                            .header("db", DB_NAME)
                            .header("table", tblName)
                            .header("format", "json")
                            .header("strip_outer_array", "false")
                            .header("ignore_json_size", "true")
                            .body(body.toString())
                            .asString();
                        // log.info("[写]数量: {} ->\n{}", count, loadResp.getBody());
                        log.info("[写]数量: {}", count);
                        break;
                    } catch (Exception e) {
                        log.error("失败", e);
                    }
                }

                for (int i = 0; i < 10; i++) {
                    try {
                        HttpResponse<String> prepareResp = unirest.post(BASE_URL + "/api/transaction/prepare")
                            .basicAuth(USERNAME, PASSWORD)
                            .header("label", LABEL)
                            .header("Expect", "100-continue")
                            .header("db", DB_NAME)
                            .header("prepared_timeout", "300")
                            .asString();
                        // log.info("预提交事务 -> \n{}", prepareResp.getBody());
                        break;
                    } catch (Exception e) {
                        log.error("失败", e);
                    }
                }

                for (int i = 0; i < 10; i++) {
                    try {
                        HttpResponse<String> commitResp = unirest.post(BASE_URL + "/api/transaction/commit")
                            .basicAuth(USERNAME, PASSWORD)
                            .header("label", LABEL)
                            .header("Expect", "100-continue")
                            .header("db", DB_NAME)
                            .asString();
                        break;
                    } catch (Exception e) {
                        log.error("失败", e);
                    }
                }

                // log.info("提交事务 -> \n{}", commitResp.getBody());
                // log.info("--> \n\n{}\n\n", stringWriter.toString());
                body = new StringBuilder();
                System.gc();
            }
            // if (count >= 30) break;
        }

        csvParser.close();
        reader.close();
        unirest.close();
    }
}
