package quant;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.clever.core.DateUtils;
import org.clever.quant.*;
import org.clever.quant.account.PaperAccount;
import org.clever.quant.fee.StockTradeFeeStrategy;
import org.clever.quant.indicators.averages.SMAIndicator;
import org.clever.quant.indicators.helpers.ClosePriceIndicator;
import org.clever.quant.position.FullPositionStrategy;
import org.clever.quant.rules.CrossedDownIndicatorRule;
import org.clever.quant.rules.CrossedUpIndicatorRule;
import org.clever.quant.strategy.BaseStrategy;
import org.clever.quant.trade.PaperTrader;
import org.clever.quant.trade.TradeLogger;
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
        Rule entryRule = new CrossedUpIndicatorRule(sma30, sma10);
        Rule exitRule = new CrossedDownIndicatorRule(sma30, sma10);
        Strategy strategy = new BaseStrategy(entryRule, exitRule, "均线相交策略");
        Account account = new PaperAccount(10_0000);
        barSeries.registerBarListener((bar, barIdx) -> {
            String date = DateUtils.formatToString(bar.getTime(), DateUtils.yyyy_MM_dd);
            String price = String.format("%.4f", bar.getClose());
            if (strategy.shouldEnter(barIdx, account)) {
                log.info("买入 @ {} 价格: {}", date, price);
                account.enter(barSeries, bar, barIdx, bar.getClose(), 1000, 5);
            }
            if (strategy.shouldExit(barIdx, account)) {
                log.info("卖出 @ {} 价格: {}", date, price);
                account.exit(barSeries, bar, barIdx, bar.getClose(), 1000, 5);
            }
        });
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

    @SuppressWarnings("ExtractMethodRecommender")
    @SneakyThrows
    @Test
    public void t03() {
        BarSeries barSeries = new BarSeries();
        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        SMAIndicator sma10 = new SMAIndicator(closePrice, 10);
        SMAIndicator sma30 = new SMAIndicator(closePrice, 30);
        Rule entryRule = new CrossedUpIndicatorRule(sma30, sma10);
        Rule exitRule = new CrossedDownIndicatorRule(sma30, sma10);
        Strategy strategy = new BaseStrategy(entryRule, exitRule, "均线相交策略");
        Account account = new PaperAccount(10_0000);
        Trader trader = new PaperTrader(account, strategy, new FullPositionStrategy(), new StockTradeFeeStrategy());
        trader.registerTradeListener(new TradeLogger());
        trader.start(barSeries);
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
        log.info("完成");
    }
}
