package quant;

import lombok.extern.slf4j.Slf4j;
import org.clever.quant.*;
import org.junit.jupiter.api.Test;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/26 18:13 <br/>
 */
@Slf4j
public class BaseTest {
    @Test
    public void t01() {
        BarSeries barSeries = new BarSeries(1_000);

        Indicator<?> indicator_01 = null;
        Indicator<?> indicator_02 = null;
        Indicator<?> indicator_03 = null;

        Rule entryRule = null;
        Rule exitRule = null;

        Strategy strategy = null;

        barSeries.registerBarListener(indicator_01);
        barSeries.registerBarListener(indicator_02);
        barSeries.registerBarListener(indicator_03);

        // indicator_04.bind()

        Account account;
        // barSeries.start(account)

        // 开始加入数据
        for (int i = 0; i < 100; i++) {
            barSeries.appendBar(null);
        }
    }
}
