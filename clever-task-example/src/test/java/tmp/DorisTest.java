package tmp;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.java.UnirestHttpClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.clever.core.id.IDCreateUtils;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Base64;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/01/20 09:28 <br/>
 */
@Slf4j
public class DorisTest {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin123456";

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
            .addHeader("label", IDCreateUtils.uuid())
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
            .build();
        // 构建 HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://192.168.1.201:8030/api/tushare/api4_logs/_stream_load"))
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .expectContinue(true) // Enable Expect: 100-continue
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
        log.info("Status Code: {}", response.statusCode());
        log.info("Response Body: {}", response.body());
    }
}
