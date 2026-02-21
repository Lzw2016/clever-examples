package tmp;

import com.zaxxer.hikari.HikariConfig;
import lombok.extern.slf4j.Slf4j;
import org.clever.data.jdbc.Jdbc;
import org.junit.jupiter.api.Test;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/20 21:58 <br/>
 */
@Slf4j
public class FlightSqlJdbcTest {

    public Jdbc createDorisJdbc() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.apache.arrow.driver.jdbc.ArrowFlightJdbcDriver");
        // catalog=xtquant
        hikariConfig.setJdbcUrl("jdbc:arrow-flight-sql://192.168.1.201:8070?useServerPrepStmts=false&cachePrepStmts=true&useSSL=false&useEncryption=false");
        hikariConfig.setUsername("admin");
        hikariConfig.setPassword("admin123456");
        hikariConfig.setAutoCommit(false);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(512);
        // hikariConfig.setCatalog("xtquant");
        // hikariConfig.setSchema("xtquant");
        return new Jdbc(hikariConfig);
    }

    public Jdbc createJdbc() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl("jdbc:mysql://192.168.1.201:9030/xtquant");
        hikariConfig.setUsername("admin");
        hikariConfig.setPassword("admin123456");
        hikariConfig.setAutoCommit(false);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(512);
        return new Jdbc(hikariConfig);
    }

    @Test
    public void test01() {
        // 内存使用不高 Cursor 有效
        Jdbc jdbc = createDorisJdbc();
        jdbc.queryForCursor(
            "select * from xtquant.stock_1dk_bar order by time asc",
            rowData -> {
                if (rowData.getRowCount() % 10000 == 0) {
                    log.info("[{}] -> {}", rowData.getRowCount(), rowData.getRowData());
                }
            }
        );
        jdbc.close();
    }

    @Test
    public void test02() {
        // 内存使用很高 Cursor 无效
        Jdbc jdbc = createJdbc();
        jdbc.queryForCursor(
            "select * from xtquant.stock_1dk_bar order by time asc limit 100000",
            rowData -> {
                if (rowData.getRowCount() % 10000 == 0) {
                    log.info("[{}] -> {}", rowData.getRowCount(), rowData.getRowData());
                }
            }
        );
        jdbc.close();
    }
}
