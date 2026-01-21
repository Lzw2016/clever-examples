package tmp;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import kong.unirest.core.java.UnirestHttpClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.clever.core.id.IDCreateUtils;
import org.clever.data.jdbc.support.SqlLoggerUtils;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/01/20 09:28 <br/>
 */
@Slf4j
public class DorisTest {
    private static final String BASE_URL = "http://192.168.1.201:8030";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin123456";
    private static final String DB_NAME = "tushare";

    @Test
    public void test01() {
        Unirest.config()
            .connectTimeout(3_000)
            .requestTimeout(60_000)
            .followRedirects(true)
            .httpClient(UnirestHttpClient::new)
        ;
        HttpResponse<String> response = Unirest.put("http://192.168.1.201:8030/api/tushare/api4_logs/_stream_load")
            .basicAuth(USERNAME, PASSWORD)
            .header("label", IDCreateUtils.uuid())
            .header("Expect", "100-continue")
            .header("format", "CSV")
            .header("column_separator", ",")
            .header("columns", "api_name,client,err_msg,log_id,req_data,req_date,res_data,res_date,server,status,url")
            .header("enclose", "\"")
            .header("Content-Type", "text/plain")
            .body("matrixContainerArriveSowSkidWay,,,1998120484100636673,\"\"\"\r\nAAAaaaaa\",2026-01-03T10:17:26.679Z,aaa,2026-01-03T10:17:26.693Z,WMS,成功,https://wms-shunde.dslyy.com/wms/wcs/matrixContainerArriveSowSkidWay\r\n")
            .asString();
        log.info("->\n{}", response.getBody());
    }

    @SneakyThrows
    @Test
    public void test02() {
        // 构造 Basic Auth header
        String credentials = USERNAME + ":" + PASSWORD;
        String encodedAuth = Base64.getEncoder().encodeToString(credentials.getBytes());
        String authHeader = "Basic " + encodedAuth;
        OkHttpClient client = new OkHttpClient().newBuilder()
            .followRedirects(true)
            .addNetworkInterceptor(chain -> {
                Request request = chain.request();
                // 如果重定向后的请求没有 Auth，手动加上
                if (request.header("Authorization") == null) {
                    request = request.newBuilder()
                        .header("Authorization", authHeader)
                        .build();
                }
                return chain.proceed(request);
            })
            .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(
            "matrixContainerArriveSowSkidWay,,,1998120484100636673,\"\"\"\r\nAAAaaaaa\",2026-01-03T10:17:26.679Z,aaa,2026-01-03T10:17:26.693Z,WMS,成功,https://wms-shunde.dslyy.com/wms/wcs/matrixContainerArriveSowSkidWay\r\n",
            mediaType
        );
        Request request = new Request.Builder()
            .url("http://192.168.1.201:8030/api/tushare/api4_logs/_stream_load")
            .method("PUT", body)
            // .addHeader("label", IDCreateUtils.uuid())
            .header("label", "test01-ddca4547-ddd9-4676-8e03-55dd8f09eba0")
            .addHeader("Expect", "100-continue")
            .addHeader("format", "CSV")
            .addHeader("column_separator", ",")
            .addHeader("columns", "api_name,client,err_msg,log_id,req_data,req_date,res_data,res_date,server,status,url")
            .addHeader("enclose", "\"")
            .addHeader("Content-Type", "text/plain")
            .header("Authorization", authHeader)
            .build();
        Response response = client.newCall(request).execute();
        log.info("->\n{}", response.body().string());
        response.close();
    }

    @SneakyThrows
    @Test
    public void test03() {
        // 构造请求体
        String body = "matrixContainerArriveSowSkidWay,,,1998120484100636673,\"\"\"\r\nAAAaaaaa\",2026-01-03T10:17:26.679Z,aaa,2026-01-03T10:17:26.693Z,WMS,成功,https://wms-shunde.dslyy.com/wms/wcs/matrixContainerArriveSowSkidWay\r\n";
        // 构造 Basic Auth header
        String credentials = USERNAME + ":" + PASSWORD;
        String encodedAuth = Base64.getEncoder().encodeToString(credentials.getBytes());
        String authHeader = "Basic " + encodedAuth;
        // 创建 HttpClient（可复用）
        HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build();
        // 构建 HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://192.168.1.201:8030/api/tushare/api4_logs/_stream_load"))
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .expectContinue(true) // Enable Expect: 100-continue
            .header("Authorization", authHeader)
            // .header("label", IDCreateUtils.uuid())
            .header("label", "test01-ddca4547-ddd9-4676-8e03-55dd8f09eba0")
            .header("format", "CSV")
            .header("column_separator", ",")
            .header("columns", "api_name,client,err_msg,log_id,req_data,req_date,res_data,res_date,server,status,url")
            .header("enclose", "\"")
            .header("Content-Type", "text/plain")
            .timeout(Duration.ofSeconds(30))
            .build();
        // 发送请求并获取响应
        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        log.info("Status Code: {}", response.statusCode());
        log.info("Response Body: {}", response.body());
    }

    @SneakyThrows
    @Test
    public void test04() {
        // 构造 Basic Auth header
        String credentials = USERNAME + ":" + PASSWORD;
        String encodedAuth = Base64.getEncoder().encodeToString(credentials.getBytes());
        String authHeader = "Basic " + encodedAuth;
        // 创建 HttpClient（可复用）
        HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

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
                        // 构建 HttpRequest
                        HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/api/" + DB_NAME + "/" + tblName + "/_stream_load"))
                            .PUT(HttpRequest.BodyPublishers.ofString(stringBuilder.toString()))
                            .expectContinue(true)
                            .header("Authorization", authHeader)
                            .header("label", IDCreateUtils.uuid())
                            .header("format", "CSV")
                            .header("column_separator", ",")
                            .header("columns", "api_name,client,err_msg,log_id,req_data,req_date,res_data,res_date,server,status,url")
                            .header("enclose", "\"")
                            .header("Content-Type", "text/plain")
                            .timeout(Duration.ofSeconds(30))
                            .build();
                        // 发送请求并获取响应
                        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                        log.info("[写]数量: {} -> {}", count, SqlLoggerUtils.deleteWhitespace(response.body()));
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

    // 最完美
    @SneakyThrows
    @Test
    public void test05() {
        // 构造 Basic Auth header
        String credentials = USERNAME + ":" + PASSWORD;
        String encodedAuth = Base64.getEncoder().encodeToString(credentials.getBytes());
        String authHeader = "Basic " + encodedAuth;
        OkHttpClient client = new OkHttpClient().newBuilder()
            .followRedirects(true)
            .addNetworkInterceptor(chain -> {
                Request request = chain.request();
                // 如果重定向后的请求没有 Auth，手动加上
                if (request.header("Authorization") == null) {
                    request = request.newBuilder()
                        .header("Authorization", authHeader)
                        .build();
                }
                return chain.proceed(request);
            })
            .build();
        MediaType mediaType = MediaType.parse("text/plain");

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
                String LABEL = "test01-" + IDCreateUtils.uuid();
                log.info("LABEL={}", LABEL);
                Request request = new Request.Builder()
                    .url("http://192.168.1.201:8030/api/tushare/api4_logs/_stream_load")
                    .method("PUT", RequestBody.create(stringBuilder.toString(), mediaType))
                    .header("label", LABEL)
                    .addHeader("Expect", "100-continue")
                    .header("format", "CSV")
                    .header("column_separator", ",")
                    .header("columns", "api_name,client,err_msg,log_id,req_data,req_date,res_data,res_date,server,status,url")
                    .header("enclose", "\"")
                    .header("Content-Type", "text/plain")
                    .header("Authorization", authHeader)
                    .build();
                Response response = client.newCall(request).execute();
                log.info("[写]数量: {} -> {}", count, SqlLoggerUtils.deleteWhitespace(response.body().string()));
                response.close();
                printer.close();
                stringBuilder.delete(0, stringBuilder.length());
            }
            // if (count >= 30) break;
        }
        if (!stringBuilder.isEmpty()) {
            String LABEL = "test01-" + IDCreateUtils.uuid();
            log.info("LABEL={}", LABEL);
            Request request = new Request.Builder()
                .url("http://192.168.1.201:8030/api/tushare/api4_logs/_stream_load")
                .method("PUT", RequestBody.create(stringBuilder.toString(), mediaType))
                .header("label", LABEL)
                .addHeader("Expect", "100-continue")
                .header("format", "CSV")
                .header("column_separator", ",")
                .header("columns", "api_name,client,err_msg,log_id,req_data,req_date,res_data,res_date,server,status,url")
                .header("enclose", "\"")
                .header("Content-Type", "text/plain")
                .header("Authorization", authHeader)
                .build();
            Response response = client.newCall(request).execute();
            log.info("[写]数量: {} -> {}", count, SqlLoggerUtils.deleteWhitespace(response.body().string()));
            response.close();
            stringBuilder.delete(0, stringBuilder.length());
        }
        printer.close();
        stringBuilder.delete(0, stringBuilder.length());

        csvParser.close();
        reader.close();
    }
}
