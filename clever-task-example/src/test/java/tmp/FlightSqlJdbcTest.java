package tmp;

import com.zaxxer.hikari.HikariConfig;
import lombok.extern.slf4j.Slf4j;
import org.clever.core.RenameStrategy;
import org.clever.data.jdbc.Jdbc;
import org.clever.data.jdbc.support.RowDataReaderCallback;
import org.junit.jupiter.api.Test;
import ta4j.model.StockBarData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
        return new Jdbc(hikariConfig);
    }

    public Jdbc createJdbc() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        // ?useCursorFetch=true
        hikariConfig.setJdbcUrl("jdbc:mysql://192.168.1.201:9030/xtquant");
        hikariConfig.setUsername("admin");
        hikariConfig.setPassword("admin123456");
        hikariConfig.setAutoCommit(false);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(512);
        Jdbc jdbc = new Jdbc(hikariConfig);
        // 必须明确设置为 Integer.MIN_VALUE --> Cursor 有效的关键!
        jdbc.getJdbcTemplate().getJdbcTemplate().setFetchSize(Integer.MIN_VALUE);
        return jdbc;
    }

    @Test
    public void test01() {
        // 内存使用不高 Cursor 有效
        Jdbc jdbc = createDorisJdbc();
        jdbc.queryForCursor(
            "select * from xtquant.stock_1dk_bar where stock_code = '600998.SH' order by time asc",
            rowData -> {
                StockBarData data = rowData.getRowData(StockBarData.class);
                log.info("[{}] -> {}", rowData.getRowCount(), data);
            }
        );
        jdbc.close();
    }

    @Test
    public void test02() {
        // 内存使用很高 Cursor 无效
        Jdbc jdbc = createJdbc();
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

    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
    @Test
    public void test03() {
        String sql = "select * from xtquant.stock_1dk_bar order by time asc";
        // 内存使用不高 Cursor 有效
        Jdbc jdbc = createJdbc();
        jdbc.newConnectionExecute(con -> {
            // Statement statement2 = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // PreparedStatement statement = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            PreparedStatement statement = con.prepareStatement(sql);
            // 必须明确设置为 Integer.MIN_VALUE --> Cursor 有效的关键!
            statement.setFetchSize(Integer.MIN_VALUE);
            // statement.setString(1, "600998.SH");
            ResultSet resultSet = statement.executeQuery();
            RowDataReaderCallback rowDataReaderCallback = new RowDataReaderCallback(rowData -> {
                if (rowData.getRowCount() % 10000 == 0) {
                    log.info("[{}] -> {}", rowData.getRowCount(), rowData.getRowData());
                }
                return false;
            }, RenameStrategy.None);
            rowDataReaderCallback.extractData(resultSet);
            resultSet.close();
            return null;
        });
        jdbc.close();
    }
}
