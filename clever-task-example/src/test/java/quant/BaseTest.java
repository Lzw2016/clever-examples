package quant;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.clever.core.Conv;
import org.clever.core.DateUtils;
import org.clever.core.id.SnowFlake;
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
import quant.entity.*;
import ta4j.BaseDataSource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    @SneakyThrows
    @Test
    public void t03() {
        BarSeries barSeries = new BarSeries();
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
        String stockCode = "600998.SH";
        Map<String, Double> priceTable = new HashMap<>();
        log.info("初始资产: {}", String.format("%.2f", account.getTotalAssets(priceTable)));
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
            priceTable.put(bar.getCode(), bar.getClose());
        });
        log.info("总资产: {}", String.format("%.2f", account.getTotalAssets(priceTable)));
        log.info("完成");
    }

    public BacktestRecord createBacktestRecord(String name, String tag, Account account) {
        BacktestRecord backtestRecord = new BacktestRecord();
        backtestRecord.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestRecord.setName(name);
        backtestRecord.setTag(tag);
        backtestRecord.setStartTime(new Date());
        // backtestRecord.setConfig();
        backtestRecord.setInitialCapital(Conv.asDecimal(account.getInitAmount()));
        backtestRecord.setCreateAt(new Date());
        backtestRecord.setDelFlag(0);
        return backtestRecord;
    }

    public void saveBacktestRecord(BacktestRecord backtestRecord, Account account, Map<String, Double> priceTable) {
        backtestRecord.setSuccess(true);
        backtestRecord.setEndTime(new Date());
        backtestRecord.setFinalTotalAssets(Conv.asDecimal(account.getTotalAssets(priceTable)));
        backtestRecord.setMinTotalAssets(null);
        backtestRecord.setProfitAmount(null);
        backtestRecord.setTotalFee(null);
        backtestRecord.setFeeRatio(null);
        backtestRecord.setCumulativeReturnRate(null);
        backtestRecord.setAvgAnnualReturnRate(null);
        backtestRecord.setReturnVolatility(null);
        backtestRecord.setMaxDrawdown(null);
        backtestRecord.setLossStdDev(null);
        backtestRecord.setMaxConsecutiveLossDays(null);
        backtestRecord.setTotalTradeCount(null);
        backtestRecord.setProfitTradeCount(null);
        backtestRecord.setWinRate(null);
        backtestRecord.setWinRate(null);
        backtestRecord.setHoldingTimeP90(null);
        backtestRecord.setMaxConsecutiveProfitCount(null);
        backtestRecord.setMaxConsecutiveLossCount(null);
    }

    public BacktestBarSeries createBacktestBarSeries(BacktestRecord backtestRecord, BarSeries barSeries, boolean main) {
        BacktestBarSeries backtestBarSeries = new BacktestBarSeries();
        backtestBarSeries.setId(barSeries.getId());
        backtestBarSeries.setBacktestRecordId(backtestRecord.getId());
        backtestBarSeries.setMain(main);
        backtestBarSeries.setSource(null);
        backtestBarSeries.setTableName(null);
        backtestBarSeries.setStartTime(null);
        backtestBarSeries.setEndTime(null);
        backtestBarSeries.setCode(null);
        backtestBarSeries.setName(null);
        backtestBarSeries.setPeriod(null);
        backtestBarSeries.setCount(null);
        backtestBarSeries.setSlidingWindow(barSeries.getSlidingWindow());
        backtestBarSeries.setExtData(barSeries.getExtData());
        backtestBarSeries.setCreateAt(new Date());
        backtestBarSeries.setDelFlag(0);
        return backtestBarSeries;
    }

    public BacktestBar createBacktestBar(BacktestRecord backtestRecord, BarSeries barSeries, Bar bar, long barIdx) {
        BacktestBar backtestBar = new BacktestBar();
        backtestBar.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestBar.setBacktestRecordId(backtestRecord.getId());
        backtestBar.setBarSeriesId(barSeries.getId());
        backtestBar.setBarIdx(barIdx);
        backtestBar.setTime(bar.getTime());
        backtestBar.setOpen(Conv.asDecimal(bar.getOpen()));
        backtestBar.setHigh(Conv.asDecimal(bar.getHigh()));
        backtestBar.setLow(Conv.asDecimal(bar.getHigh()));
        backtestBar.setClose(Conv.asDecimal(bar.getClose()));
        backtestBar.setVolume(bar.getVolume());
        backtestBar.setAmount(Conv.asDecimal(bar.getAmount()));
        backtestBar.setExtData(bar.getExtData());
        backtestBar.setCreateAt(new Date());
        backtestBar.setDelFlag(0);
        return backtestBar;
    }

    public BacktestIndicator createBacktestIndicator(BacktestRecord backtestRecord, BarSeries barSeries, Indicator<?> indicator, long barIdx) {
        BacktestIndicator backtestIndicator = new BacktestIndicator();
        backtestIndicator.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestIndicator.setBacktestRecordId(backtestRecord.getId());
        backtestIndicator.setBarSeriesId(barSeries.getId());
        backtestIndicator.setBarIdx(barIdx);
        backtestIndicator.setName(indicator.getClass().getSimpleName());
        Object val = indicator.getValue(barIdx);
        if (val == null) {
            backtestIndicator.setValType(Constant.indicator_val_type_0);
        } else if (val instanceof Number) {
            backtestIndicator.setValType(Constant.indicator_val_type_1);
            backtestIndicator.setNumberVal(Conv.asDecimal(val, null));
        } else if (val instanceof Boolean) {
            backtestIndicator.setValType(Constant.indicator_val_type_2);
            backtestIndicator.setBoolVal(Conv.asBoolean(val, null));
        } else if (val instanceof CharSequence) {
            backtestIndicator.setValType(Constant.indicator_val_type_3);
            backtestIndicator.setStringVal(Conv.asString(val, null));
        } else {
            backtestIndicator.setValType(Constant.indicator_val_type_4);
            backtestIndicator.setObjVal(val);
        }
        backtestIndicator.setCreateAt(new Date());
        backtestIndicator.setDelFlag(0);
        return backtestIndicator;
    }

    public BacktestRule createBacktestRule(BacktestRecord backtestRecord, Account account, Rule rule, long barIdx) {
        BacktestRule backtestRule = new BacktestRule();
        backtestRule.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestRule.setBacktestRecordId(backtestRecord.getId());
        backtestRule.setBarSeriesIds(rule.getAllBarSeries().stream().map(BarSeries::getId).toList());
        backtestRule.setBarIdx(barIdx);
        backtestRule.setName(rule.getName());
        backtestRule.setSatisfied(rule.isSatisfied(barIdx, account));
        backtestRule.setCreateAt(new Date());
        backtestRule.setDelFlag(0);
        return backtestRule;
    }

    public BacktestStrategy createBacktestStrategy(BacktestRecord backtestRecord, Strategy strategy, long barIdx) {
        BacktestStrategy backtestStrategy = new BacktestStrategy();
        backtestStrategy.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestStrategy.setBacktestRecordId(backtestRecord.getId());
        backtestStrategy.setBarSeriesIds(strategy.getAllBarSeries().stream().map(BarSeries::getId).toList());
        backtestStrategy.setEntryRuleId(strategy.getEntryRule().getId());
        backtestStrategy.setExitRuleId(strategy.getExitRule().getId());
        backtestStrategy.setBarIdx(barIdx);
        backtestStrategy.setName(strategy.getName());
        backtestStrategy.setCreateAt(new Date());
        backtestStrategy.setDelFlag(0);
        return backtestStrategy;
    }

    public BacktestAccountSnapshot createBacktestAccountSnapshot(BacktestRecord backtestRecord, Account account, Map<String, Double> priceTable, long barIdx) {
        BacktestAccountSnapshot backtestAccountSnapshot = new BacktestAccountSnapshot();
        backtestAccountSnapshot.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestAccountSnapshot.setBacktestRecordId(backtestRecord.getId());
        backtestAccountSnapshot.setBarIdx(barIdx);
        TradeAccountSnapshot snapshot = account.getSnapshot();
        backtestAccountSnapshot.setBalance(Conv.asDecimal(snapshot.getBalance()));
        backtestAccountSnapshot.setTotalAssets(Conv.asDecimal(snapshot.getTotalAssets(priceTable)));
        backtestAccountSnapshot.setCreateAt(new Date());
        backtestAccountSnapshot.setDelFlag(0);
        return backtestAccountSnapshot;
    }

    public BacktestPositions createBacktestPositions(BacktestRecord backtestRecord, Position position, long barIdx) {
        BacktestPositions backtestPositions = new BacktestPositions();
        backtestPositions.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestPositions.setBacktestRecordId(backtestRecord.getId());
        backtestPositions.setBarIdx(barIdx);
        backtestPositions.setCode(position.getCode());
        backtestPositions.setVolume(position.getVolume());
        backtestPositions.setAvailableVolume(position.getAvailableVolume());
        backtestPositions.setAvgCostPrice(Conv.asDecimal(position.getAvgCostPrice()));
        backtestPositions.setCreateAt(new Date());
        backtestPositions.setDelFlag(0);
        return backtestPositions;
    }

    public BacktestTradeLog createBacktestTradeLog(BacktestRecord backtestRecord, TradeLog tradeLog, long barIdx) {
        BacktestTradeLog backtestTradeLog = new BacktestTradeLog();
        backtestTradeLog.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestTradeLog.setBacktestRecordId(backtestRecord.getId());
        backtestTradeLog.setBarIdx(barIdx);
        backtestTradeLog.setCode(tradeLog.getCode());
        backtestTradeLog.setTradeType(tradeLog.getTradeType().getName());
        backtestTradeLog.setTime(tradeLog.getTime());
        backtestTradeLog.setPrice(Conv.asDecimal(tradeLog.getPrice()));
        backtestTradeLog.setVolume(tradeLog.getVolume());
        backtestTradeLog.setFee(Conv.asDecimal(tradeLog.getFee()));
        backtestTradeLog.setAmount(Conv.asDecimal(tradeLog.getAmount()));
        backtestTradeLog.setCreateAt(new Date());
        backtestTradeLog.setDelFlag(0);
        return backtestTradeLog;
    }
}
