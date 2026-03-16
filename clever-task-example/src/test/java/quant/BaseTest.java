package quant;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.clever.core.DateUtils;
import org.clever.data.jdbc.Jdbc;
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

import java.util.*;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/26 18:13 <br/>
 */
@Slf4j
public class BaseTest {
    @Test
    public void t01() {
        BarSeries barSeries = new BarSeries(new ArrayList<>(), 1_000);

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
        BarSeries barSeries = new BarSeries(new ArrayList<>());
        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        SMAIndicator sma10 = new SMAIndicator(closePrice, 10);
        SMAIndicator sma30 = new SMAIndicator(closePrice, 30);
        Rule entryRule = new CrossedUpIndicatorRule(sma30, sma10);
        Rule exitRule = new CrossedDownIndicatorRule(sma30, sma10);
        Strategy strategy = new BaseStrategy(entryRule, exitRule, "均线相交策略");
        Account account = new PaperAccount(10_0000);
        barSeries.registerBarListener((bar, barIdx) -> {
            String date = DateUtils.formatToString(bar.getTime(), DateUtils.yyyy_MM_dd);
            String price = String.format("%.4f", bar.getClose(AdjustType.none));
            if (strategy.shouldEnter(barIdx, account)) {
                log.info("买入 @ {} 价格: {}", date, price);
                account.enter(barSeries, bar, barIdx, bar.getClose(AdjustType.none), 1000, 5);
            }
            if (strategy.shouldExit(barIdx, account)) {
                log.info("卖出 @ {} 价格: {}", date, price);
                account.exit(barSeries, bar, barIdx, bar.getClose(AdjustType.none), 1000, 5);
            }
        });
        Jdbc jdbc = BaseDataSource.createDorisJdbc();
        String stockCode = "600998.SH";
        BaseDataSource.get1dkBar(jdbc, stockCode, stockBarData -> {
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
        jdbc.close();
        Thread.sleep(60_000);
        log.info("完成");
    }

    @SneakyThrows
    @Test
    public void t03() {
        Jdbc jdbc = BaseDataSource.createDorisJdbc();
        Admin admin = BaseDataSource.createKafkaAdmin();
        KafkaProducer<String, String> kafkaProducer = BaseDataSource.createKafkaProducer();

        BarSeries barSeries = new BarSeries(new ArrayList<>());
        barSeries.addExtData(BarSeries.EXT_SOURCE, "xtquant");
        barSeries.addExtData(BarSeries.EXT_TABLE_NAME, "stock_1dk_bar");
        Indicator<Double> closePrice = new ClosePriceIndicator(barSeries);
        Indicator<Double> sma10 = new SMAIndicator(closePrice, 10);
        Indicator<Double> sma30 = new SMAIndicator(closePrice, 30);
        Rule entryRule = new CrossedUpIndicatorRule(sma30, sma10);
        Rule exitRule = new CrossedDownIndicatorRule(sma30, sma10);
        Strategy strategy = new BaseStrategy(entryRule, exitRule, "均线相交策略");
        Account account = new PaperAccount(10_0000);
        Trader trader = new PaperTrader(account, strategy, new FullPositionStrategy(), new StockTradeFeeStrategy());
        trader.registerTradeListener(new TradeLogger());
        trader.start(barSeries);
        BacktestArchiver backtestArchiver = new KafkaBacktestArchiver(
            "均线相交策略",
            "600998",
            account,
            barSeries,
            new HashSet<>(),
            List.of(sma10, sma30),
            List.of(entryRule, exitRule),
            List.of(strategy),
            admin,
            kafkaProducer,
            "quant_data"
        );
        backtestArchiver.start(trader);
        String stockCode = "600998.SH";
        Map<String, Double> priceTable = new HashMap<>();
        log.info("初始资产: {}", String.format("%.2f", account.getTotalAssets(priceTable)));
        BaseDataSource.get1dkBar(jdbc, stockCode, stockBarData -> {
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
            priceTable.put(bar.getCode(), bar.getClose(AdjustType.none));
        });
        backtestArchiver.end();
        admin.close();
        kafkaProducer.close();
        jdbc.close();
        log.info("总资产: {}", String.format("%.2f", account.getTotalAssets(priceTable)));
        log.info("完成");
    }

    @Test
    public void t05() {
        BaseDataSource.backtest((jdbc, admin, kafkaProducer) -> {
            List<BaseDataSource.StockSymbol> allStockSymbols = BaseDataSource.getAllStockSymbols(jdbc);
            int idx = 0;
            for (BaseDataSource.StockSymbol stockSymbol : allStockSymbols) {
                idx++;
                log.info("{}/{} [{}]开始计算...", idx, allStockSymbols.size(), stockSymbol.getCode());
                long count = BaseDataSource.get1dkBarCount(jdbc, stockSymbol.getCode());
                if (count <= 0) {
                    log.info("[{}] 没有数据跳过", stockSymbol.getCode());
                    continue;
                }
                BarSeries barSeries = new BarSeries(new ArrayList<>());
                barSeries.addExtData(BarSeries.EXT_SOURCE, "xtquant");
                barSeries.addExtData(BarSeries.EXT_TABLE_NAME, "stock_1dk_bar");
                Indicator<Double> closePrice = new ClosePriceIndicator(barSeries);
                Indicator<Double> sma10 = new SMAIndicator(closePrice, 10);
                Indicator<Double> sma30 = new SMAIndicator(closePrice, 30);
                Rule entryRule = new CrossedUpIndicatorRule(sma10, sma30);
                Rule exitRule = new CrossedDownIndicatorRule(sma10, sma30);
                Strategy strategy = new BaseStrategy(entryRule, exitRule, "均线相交");
                Account account = new PaperAccount(10_0000);
                Trader trader = new PaperTrader(account, strategy, new FullPositionStrategy(), new StockTradeFeeStrategy());
                // trader.registerTradeListener(new TradeLogger());
                trader.start(barSeries);
                BacktestArchiver backtestArchiver = new KafkaBacktestArchiver(
                    "均线相交(正向)",
                    String.format("%s(%s)", stockSymbol.getCode(), stockSymbol.getName()),
                    account,
                    barSeries,
                    new HashSet<>(),
                    List.of(sma10, sma30),
                    List.of(entryRule, exitRule),
                    List.of(strategy),
                    admin,
                    kafkaProducer,
                    "quant_data"
                );
                backtestArchiver.start(trader);
                Map<String, Double> priceTable = new HashMap<>();
                BaseDataSource.get1dkBar(jdbc, stockSymbol.getCode(), stockBarData -> {
                    Bar bar = Bar.builder()
                        .code(stockSymbol.getCode())
                        .period(Period._1d)
                        .time(stockBarData.getTime())
                        .open(stockBarData.getOpen().doubleValue())
                        .high(stockBarData.getHigh().doubleValue())
                        .low(stockBarData.getLow().doubleValue())
                        .close(stockBarData.getClose().doubleValue())
                        .volume(stockBarData.getVolume())
                        .amount(stockBarData.getAmount().doubleValue())
                        .adjustPriceCalc(null)
                        .build();
                    barSeries.appendBar(bar);
                    priceTable.put(bar.getCode(), bar.getClose(AdjustType.none));
                });
                backtestArchiver.end();
                log.info("[{}] 总资产: {}", stockSymbol.getCode(), String.format("%.2f", account.getTotalAssets(priceTable)));
            }
        });
    }

    @Test
    public void t06() {
        Jdbc jdbc = BaseDataSource.createJdbc();
        List<Dividend> dividends = BaseDataSource.getDividends(jdbc, "600998.SH");
        for (Dividend dividend : dividends) {
            log.info("-> {}", dividend);
        }
        jdbc.close();
    }
}
