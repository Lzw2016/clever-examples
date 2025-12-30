package tmp;

import com.zaxxer.hikari.HikariConfig;
import lombok.extern.slf4j.Slf4j;
import org.clever.data.jdbc.Jdbc;
import org.clever.data.jdbc.config.JdbcConfig;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionDefinition;

/**
 * 作者：lizw <br/>
 * 创建时间：2025/12/29 13:02 <br/>
 */
@Slf4j
public class JdbcTest {
    public static Jdbc createInnerJdbc() {
        org.clever.data.jdbc.metrics.Slf4JLogger.init(new JdbcConfig.P6SpyLog(), new JdbcConfig.JdbcMetrics());
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
        hikariConfig.setJdbcUrl("jdbc:p6spy:postgresql://192.168.1.201:30010/test");
        hikariConfig.setUsername("admin");
        hikariConfig.setPassword("admin123456");
        hikariConfig.setAutoCommit(false);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(512);
        return new Jdbc(hikariConfig);
    }

    private static Jdbc createOutJdbc() {
        org.clever.data.jdbc.metrics.Slf4JLogger.init(new JdbcConfig.P6SpyLog(), new JdbcConfig.JdbcMetrics());
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
        hikariConfig.setJdbcUrl("jdbc:p6spy:postgresql://test-wms-kls-wms_sd-pgsql-rw.dslbuy.com:5432/wms_sd");
        hikariConfig.setUsername("wms_sd_rw");
        hikariConfig.setPassword("DkDY8w1Qi38Ab9O55sZ_");
        hikariConfig.setAutoCommit(false);
        hikariConfig.setReadOnly(true);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(512);
        return new Jdbc(hikariConfig);
    }

    @Test
    public void test01() {
        Jdbc jdbc = createOutJdbc();
        Jdbc target = createInnerJdbc();
        target.beginTX(status -> {
            target.update("truncate table bas_item");
            jdbc.queryForCursor("select * from bas_item", 10000, batchData -> {
                target.batchInsertTable("bas_item", batchData.getRowDataList());
                log.info("--> {}", batchData.getRowCount());
            });
        }, TransactionDefinition.PROPAGATION_REQUIRED, -1);
        jdbc.close();
        target.close();
    }
}
