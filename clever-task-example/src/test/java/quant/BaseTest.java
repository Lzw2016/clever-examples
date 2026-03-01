package quant;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.clever.quant.*;
import org.clever.quant.indicators.averages.SMAIndicator;
import org.clever.quant.indicators.helpers.ClosePriceIndicator;
import org.junit.jupiter.api.Test;
import ta4j.BaseDataSource;

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

    @SneakyThrows
    @Test
    public void t02() {
        BarSeries barSeries = new BarSeries();
        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        SMAIndicator sma10 = new SMAIndicator(closePrice, 10);
        SMAIndicator sma30 = new SMAIndicator(closePrice, 30);

        String stockCode = "600998.SH";
        BaseDataSource.get1dkBar(stockCode, stockBarData -> {
            Bar bar = Bar.builder()
                .code(stockCode)
                .period(Period._1d)
                .time(stockBarData.getTime())
                .open(stockBarData.getOpen().doubleValue())
                .high(stockBarData.getHigh().doubleValue())
                .low(stockBarData.getLow().doubleValue())
                .close(stockBarData.getClose().doubleValue())
                .volume(stockBarData.getVolume())
                .amount(stockBarData.getAmount().doubleValue())
                .build();
            barSeries.appendBar(bar);
        });
        Thread.sleep(60_000);
        log.info("完成");
    }
}
