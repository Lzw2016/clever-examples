package ta4j;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.clever.core.DateUtils;
import org.clever.data.jdbc.Jdbc;
import org.junit.jupiter.api.Test;
import org.ta4j.core.*;
import org.ta4j.core.analysis.EquityCurveMode;
import org.ta4j.core.analysis.ExcessReturns;
import org.ta4j.core.analysis.OpenPositionHandling;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.analysis.frequency.SamplingFrequency;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.backtest.TradeOnCurrentCloseModel;
import org.ta4j.core.bars.TimeBarBuilder;
import org.ta4j.core.criteria.*;
import org.ta4j.core.criteria.commissions.CommissionsImpactPercentageCriterion;
import org.ta4j.core.criteria.drawdown.MaximumDrawdownCriterion;
import org.ta4j.core.criteria.pnl.MaxConsecutiveLossCriterion;
import org.ta4j.core.criteria.pnl.NetProfitLossRatioCriterion;
import org.ta4j.core.criteria.pnl.NetReturnCriterion;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.DecimalNumFactory;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;
import quant.BaseDataSource;

import java.time.Duration;
import java.time.ZoneId;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/21 12:35 <br/>
 */
@Slf4j
public class Ta4j01Test {

    public static Strategy strategy00(BarSeries barSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        SMAIndicator sma10 = new SMAIndicator(closePrice, 10);
        SMAIndicator sma30 = new SMAIndicator(closePrice, 30);

//        Rule entryRule = new CrossedUpIndicatorRule(sma10, sma30);
//        Rule exitRule = new CrossedDownIndicatorRule(sma10, sma30);

        Rule entryRule = new CrossedUpIndicatorRule(sma30, sma10);
        Rule exitRule = new CrossedDownIndicatorRule(sma30, sma10);
        return new BaseStrategy("均线相交策略", entryRule, exitRule, 30);
    }

    /**
     * 构建 ADX+DMI 策略
     *
     * @param barSeries    K 线数据
     * @param adxPeriod    ADX 周期（默认 14）
     * @param adxThreshold ADX 趋势阈值（默认 25）
     * @return 策略对象
     */
    public static Strategy strategy01(BarSeries barSeries, int adxPeriod, double adxThreshold) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        // === ADX 趋势强度指标 ===
        ADXIndicator adx = new ADXIndicator(barSeries, adxPeriod);
        // === DMI 方向指标 ===
        PlusDIIndicator plusDI = new PlusDIIndicator(barSeries, adxPeriod);
        MinusDIIndicator minusDI = new MinusDIIndicator(barSeries, adxPeriod);
        // === ATR 波动率指标（用于止损）===
//        ATRIndicator atr = new ATRIndicator(barSeries, adxPeriod);

        // === 入场规则 ===
        // 条件 1: ADX > 25（确认有趋势）
        OverIndicatorRule adxStrongRule = new OverIndicatorRule(adx, adxThreshold);
        // 条件 2: +DI 上穿-DI（多头信号）
        CrossedUpIndicatorRule diCrossUpRule = new CrossedUpIndicatorRule(plusDI, minusDI);
        // 综合入场：趋势确认 + 方向信号
        Rule entryRule = adxStrongRule.and(diCrossUpRule);

        // === 出场规则 ===
        // 条件 1: -DI 上穿+DI（空头信号）
        CrossedUpIndicatorRule diCrossDownRule = new CrossedUpIndicatorRule(minusDI, plusDI);
        // 条件 2: ADX < 20（趋势消失）
        UnderIndicatorRule adxWeakRule = new UnderIndicatorRule(adx, 20);
        // 条件 3: ATR 追踪止损（2 倍 ATR）
        TrailingStopLossRule atrStopRule = new TrailingStopLossRule(
            closePrice,
            DecimalNum.valueOf(0.05)// 5% 追踪止损
        );
        Rule exitRule = diCrossDownRule.or(adxWeakRule).or(atrStopRule);
        // === 创建策略 ===
        return new BaseStrategy("ADX-DMI 趋势策略 (" + adxPeriod + ")", entryRule, exitRule, 30);
    }

    @SneakyThrows
    @Test
    public void test01() {
        BarSeries barSeries = new BaseBarSeriesBuilder()
            .withName("K线数据流")
            .withMaxBarCount(256)
            .build();

//        Strategy strategy = strategy01(barSeries, 14, 25);
        Strategy strategy = strategy00(barSeries);

        TradingRecord tradingRecord = new LiveTradingRecord(
            Trade.TradeType.BUY,
            ExecutionMatchPolicy.AVG_COST,
            new LinearTransactionCostModel(0.00035),
            new ZeroCostModel(),
            null,
            null
        );
        Jdbc jdbc = BaseDataSource.createJdbc();
        String stockCode = "600998.SH";
        BaseDataSource.get1dkBar(jdbc, stockCode, stockBarData -> {
            Bar bar = new TimeBarBuilder(DecimalNumFactory.getInstance())
                .beginTime(stockBarData.getTime().toInstant())
                .timePeriod(Duration.ofDays(1))
                .openPrice(stockBarData.getOpen())
                .highPrice(stockBarData.getHigh())
                .lowPrice(stockBarData.getLow())
                .closePrice(stockBarData.getClose())
                .volume(stockBarData.getVolume())
                .amount(stockBarData.getAmount())
                .build();
            barSeries.addBar(bar);

            int lastIndex = barSeries.getEndIndex();
            String date = DateUtils.formatToString(stockBarData.getTime(), DateUtils.yyyy_MM_dd);
            boolean shouldEnter = strategy.shouldEnter(lastIndex, tradingRecord);
            boolean shouldExit = strategy.shouldExit(lastIndex, tradingRecord);
            Num price = barSeries.getBar(lastIndex).getClosePrice();
            if (shouldEnter) {
                tradingRecord.enter(lastIndex, price, DecimalNum.valueOf(2000));
                log.info("买入 @ {} 价格: {}", date, price);
            } else if (shouldExit) {
                tradingRecord.exit(lastIndex, price, DecimalNum.valueOf(2000));
                log.info("卖出 @ {} 价格: {}", date, price);
            }
            // if (sma10.isStable() && sma30.isStable()) {
            // }
        });
        jdbc.close();
        // 创建绩效分析器
        AnalysisCriterion netReturn = new NetReturnCriterion(ReturnRepresentation.DECIMAL);
        AnalysisCriterion maxDrawdown = new MaximumDrawdownCriterion(EquityCurveMode.MARK_TO_MARKET, OpenPositionHandling.MARK_TO_MARKET);
        AnalysisCriterion netProfitLossRatio = new NetProfitLossRatioCriterion();
        AnalysisCriterion numberOfWinningPositions = new NumberOfWinningPositionsCriterion();
        AnalysisCriterion expectancy = new ExpectancyCriterion();
        AnalysisCriterion commissionImpact = new CommissionsImpactPercentageCriterion();
        AnalysisCriterion maxConsecutiveLoss = new MaxConsecutiveLossCriterion();
        AnalysisCriterion positionDuration = new PositionDurationCriterion(Statistics.P95);
        AnalysisCriterion sharpeRatio = new SharpeRatioCriterion(
            0.03,
            SamplingFrequency.BAR,
            Annualization.ANNUALIZED,
            ZoneId.of("Asia/Shanghai"),
            ExcessReturns.CashReturnPolicy.CASH_EARNS_ZERO,
            EquityCurveMode.REALIZED,
            OpenPositionHandling.IGNORE
        );

        // 计算指标
        Num returnVal = netReturn.calculate(barSeries, tradingRecord);
        Num drawdownVal = maxDrawdown.calculate(barSeries, tradingRecord);
        Num netProfitLossRatioVal = netProfitLossRatio.calculate(barSeries, tradingRecord);
        Num numberOfWinningPositionsVal = numberOfWinningPositions.calculate(barSeries, tradingRecord);
        Num expectancyVal = expectancy.calculate(barSeries, tradingRecord);
        Num commissionVal = commissionImpact.calculate(barSeries, tradingRecord);
        Num maxConsecutiveLossVal = maxConsecutiveLoss.calculate(barSeries, tradingRecord);
        Num positionDurationVal = positionDuration.calculate(barSeries, tradingRecord);
        Num sharpeVal = sharpeRatio.calculate(barSeries, tradingRecord);

        // 输出结果
        log.info("总交易次数: {}", tradingRecord.getPositionCount());
        log.info("净收益率：{}%", returnVal.doubleValue() * 100);
        log.info("最大回撤：{}%", drawdownVal.doubleValue() * 100);
        log.info("盈亏比：{}%", netProfitLossRatioVal.doubleValue());
        log.info("胜率：{}%", numberOfWinningPositionsVal.doubleValue());
        log.info("期望值：{}%", expectancyVal.doubleValue() * 100);
        log.info("手续费影响：{}%", commissionVal.doubleValue());
        log.info("连续亏损：{}", maxConsecutiveLossVal.doubleValue());
        log.info("持仓周期：{}天", positionDurationVal.doubleValue() / (60 * 60 * 24));
        log.info("夏普比率：{}", sharpeVal);
    }

    @SneakyThrows
    @Test
    public void test02() {
        BarSeries barSeries = new BaseBarSeriesBuilder()
            .withName("测试")
            .build();
        Jdbc jdbc = BaseDataSource.createJdbc();
        String stockCode = "600998.SH";
        BaseDataSource.get1dkBar(jdbc, stockCode, stockBarData -> {
            Bar bar = new TimeBarBuilder(DecimalNumFactory.getInstance())
                .beginTime(stockBarData.getTime().toInstant())
                .timePeriod(Duration.ofDays(1))
                .openPrice(stockBarData.getOpen())
                .highPrice(stockBarData.getHigh())
                .lowPrice(stockBarData.getLow())
                .closePrice(stockBarData.getClose())
                .volume(stockBarData.getVolume())
                .amount(stockBarData.getAmount())
                .build();
            barSeries.addBar(bar);
        });
        jdbc.close();
        Strategy strategy = strategy00(barSeries);
        BarSeriesManager manager = new BarSeriesManager(
            barSeries,
            new LinearTransactionCostModel(0.00035),
            new ZeroCostModel(),
            new TradeOnCurrentCloseModel()
        );
        TradingRecord tradingRecord = manager.run(strategy, Trade.TradeType.BUY, DecimalNum.valueOf(2000));
        // 创建绩效分析器
        AnalysisCriterion netReturn = new NetReturnCriterion(ReturnRepresentation.DECIMAL);
        AnalysisCriterion maxDrawdown = new MaximumDrawdownCriterion(EquityCurveMode.MARK_TO_MARKET, OpenPositionHandling.MARK_TO_MARKET);
        AnalysisCriterion netProfitLossRatio = new NetProfitLossRatioCriterion();
        AnalysisCriterion numberOfWinningPositions = new NumberOfWinningPositionsCriterion();
        AnalysisCriterion expectancy = new ExpectancyCriterion();
        AnalysisCriterion commissionImpact = new CommissionsImpactPercentageCriterion();
        AnalysisCriterion maxConsecutiveLoss = new MaxConsecutiveLossCriterion();
        AnalysisCriterion positionDuration = new PositionDurationCriterion(Statistics.P95);
        AnalysisCriterion sharpeRatio = new SharpeRatioCriterion(
            0.03,
            SamplingFrequency.BAR,
            Annualization.ANNUALIZED,
            ZoneId.of("Asia/Shanghai"),
            ExcessReturns.CashReturnPolicy.CASH_EARNS_ZERO,
            EquityCurveMode.REALIZED,
            OpenPositionHandling.IGNORE
        );

        // 计算指标
        Num returnVal = netReturn.calculate(barSeries, tradingRecord);
        Num drawdownVal = maxDrawdown.calculate(barSeries, tradingRecord);
        Num netProfitLossRatioVal = netProfitLossRatio.calculate(barSeries, tradingRecord);
        Num numberOfWinningPositionsVal = numberOfWinningPositions.calculate(barSeries, tradingRecord);
        Num expectancyVal = expectancy.calculate(barSeries, tradingRecord);
        Num commissionVal = commissionImpact.calculate(barSeries, tradingRecord);
        Num maxConsecutiveLossVal = maxConsecutiveLoss.calculate(barSeries, tradingRecord);
        Num positionDurationVal = positionDuration.calculate(barSeries, tradingRecord);
        Num sharpeVal = sharpeRatio.calculate(barSeries, tradingRecord);

        // 输出结果
        log.info("总交易次数: {}", tradingRecord.getPositionCount());
        log.info("净收益率：{}%", returnVal.doubleValue() * 100);
        log.info("最大回撤：{}%", drawdownVal.doubleValue() * 100);
        log.info("盈亏比：{}%", netProfitLossRatioVal.doubleValue());
        log.info("胜率：{}%", numberOfWinningPositionsVal.doubleValue());
        log.info("期望值：{}%", expectancyVal.doubleValue() * 100);
        log.info("手续费影响：{}%", commissionVal.doubleValue());
        log.info("连续亏损：{}", maxConsecutiveLossVal.doubleValue());
        log.info("持仓周期：{}天", positionDurationVal.doubleValue() / (60 * 60 * 24));
        log.info("夏普比率：{}", sharpeVal);
    }
}
