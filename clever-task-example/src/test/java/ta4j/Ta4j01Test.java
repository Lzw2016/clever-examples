package ta4j;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.clever.core.DateUtils;
import org.junit.jupiter.api.Test;
import org.ta4j.core.*;
import org.ta4j.core.analysis.EquityCurveMode;
import org.ta4j.core.analysis.OpenPositionHandling;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.bars.TimeBarBuilder;
import org.ta4j.core.criteria.*;
import org.ta4j.core.criteria.commissions.CommissionsImpactPercentageCriterion;
import org.ta4j.core.criteria.drawdown.MaximumDrawdownCriterion;
import org.ta4j.core.criteria.pnl.MaxConsecutiveLossCriterion;
import org.ta4j.core.criteria.pnl.NetProfitLossRatioCriterion;
import org.ta4j.core.criteria.pnl.NetReturnCriterion;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.DecimalNumFactory;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.time.Duration;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/21 12:35 <br/>
 */
@Slf4j
public class Ta4j01Test {
    @SneakyThrows
    @Test
    public void test01() {
        BarSeries barSeries = new BaseBarSeriesBuilder()
            .withName("K线数据流")
            .withMaxBarCount(256)
            .build();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        SMAIndicator sma10 = new SMAIndicator(closePrice, 10);
        SMAIndicator sma30 = new SMAIndicator(closePrice, 30);

//        Rule entryRule = new CrossedUpIndicatorRule(sma10, sma30);
//        Rule exitRule = new CrossedDownIndicatorRule(sma10, sma30);

        Rule entryRule = new CrossedUpIndicatorRule(sma30, sma10);
        Rule exitRule = new CrossedDownIndicatorRule(sma30, sma10);
        Strategy strategy = new BaseStrategy("均线相交策略", entryRule, exitRule, 30);

        TradingRecord tradingRecord = new BaseTradingRecord(
            Trade.TradeType.BUY,
            null,
            null,
            new LinearTransactionCostModel(0.00035),
            new ZeroCostModel()
        );

        String stockCode = "600998.SH";
        BaseDataSource.get1dkBar(stockCode, stockBarData -> {
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
            if (sma10.isStable() && sma30.isStable()) {
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
            }
        });

        // 创建绩效分析器
        AnalysisCriterion netReturn = new NetReturnCriterion(ReturnRepresentation.DECIMAL);
        AnalysisCriterion maxDrawdown = new MaximumDrawdownCriterion(EquityCurveMode.MARK_TO_MARKET, OpenPositionHandling.MARK_TO_MARKET);
        AnalysisCriterion netProfitLossRatio = new NetProfitLossRatioCriterion();
        AnalysisCriterion numberOfWinningPositions = new NumberOfWinningPositionsCriterion();
        AnalysisCriterion expectancy = new ExpectancyCriterion();
        AnalysisCriterion commissionImpact = new CommissionsImpactPercentageCriterion();
        AnalysisCriterion maxConsecutiveLoss = new MaxConsecutiveLossCriterion();
        AnalysisCriterion positionDuration = new PositionDurationCriterion(Statistics.P95);
        AnalysisCriterion sharpeRatio = new SharpeRatioCriterion();

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
        log.info("净收益率：{}%", returnVal.doubleValue() * 100);
        log.info("最大回撤：{}%", drawdownVal.doubleValue() * 100);
        log.info("盈亏比：{}%", netProfitLossRatioVal.doubleValue());
        log.info("胜率：{}%", numberOfWinningPositionsVal.doubleValue());
        log.info("期望值：{}%", expectancyVal.doubleValue() * 100);
        log.info("手续费影响：{}%", commissionVal.doubleValue());
        log.info("连续亏损：{}", maxConsecutiveLossVal.doubleValue());
        log.info("持仓周期：{}天", positionDurationVal.doubleValue() / (60 * 60 * 24));
        log.info("夏普比率：{}%", sharpeVal);
    }
}
