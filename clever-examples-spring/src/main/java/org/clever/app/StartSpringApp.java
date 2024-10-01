package org.clever.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

/**
 * 作者：lizw <br/>
 * 创建时间：2024/10/01 11:49 <br/>
 */
@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        RedisAutoConfiguration.class,
        ElasticsearchRestClientAutoConfiguration.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
    }
)
public class StartSpringApp {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(StartSpringApp.class);
        application.run(args);
    }
}
