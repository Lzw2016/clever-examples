package tmp;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.bars.TimeBarBuilder;
import org.ta4j.core.criteria.drawdown.MaximumDrawdownCriterion;
import org.ta4j.core.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.DoubleNumFactory;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.time.ZoneId;
import java.time.ZonedDateTime;


/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/07 10:54 <br/>
 */
@Slf4j
public class Ta4jTest {

    // 模拟大量数据（实际中可替换为文件流、数据库游标等）
    private static double[] simulateLargeData() {
        double[] data = new double[100_000];
        double price = 100.0;
        for (int i = 0; i < data.length; i++) {
            price += (Math.random() - 0.5) * 2; // 随机游走
            data[i] = Math.max(price, 1.0);
        }
        return data;
    }

    @SneakyThrows
    @Test
    public void test01() {
        // 1. 创建带最大容量的 BarSeries（只保留最近 100 根K线）
        BarSeries series = new BaseBarSeriesBuilder()
            .withName("流数据")
            .withMaxBarCount(100)
            // .setConstrained()
            // .withBars()
            // .withNumFactory()
            // .withBarBuilderFactory()
            .build();
        // series.addBar();

        log.info("series -> {}", series);
    }


    @SneakyThrows
    @Test
    public void test02() {
        // 1. 创建带最大容量的 BarSeries（只保留最近 100 根K线）
        BarSeries series = new BaseBarSeriesBuilder()
            .withName("Streaming Series")
            .withMaxBarCount(100) // 关键：限制内存占用
            .withNumFactory(DoubleNumFactory.getInstance())
            .build();

        // 2. 初始化策略和指标（指标会随新数据自动更新）
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, 10);
        SMAIndicator longSma = new SMAIndicator(closePrice, 30);

        Rule entryRule = new CrossedUpIndicatorRule(shortSma, longSma);
        Rule exitRule = new CrossedDownIndicatorRule(shortSma, longSma);
        Strategy strategy = new BaseStrategy(entryRule, exitRule);

        // 3. 创建 TradingRecord 和回测管理器
        TradingRecord tradingRecord = new BaseTradingRecord();
        BarSeriesManager manager = new BarSeriesManager(series);

        // 4. 模拟流式数据输入（例如从文件、Kafka、数据库逐条读取）
        ZoneId zone = ZoneId.systemDefault();
        double[] prices = simulateLargeData(); // 假设有 100,000 条数据
        ZonedDateTime time = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, zone);

        for (int i = 0; i < prices.length; i++) {
//            Bar bar = new TimeBarBuilder(DecimalNumFactory.getInstance())
            Bar bar = new TimeBarBuilder()
                .beginTime(time.plusMinutes(i).toInstant())
                .endTime(time.plusMinutes(i + 1).toInstant())
                .openPrice(prices[i])
                .highPrice(prices[i])
                .lowPrice(prices[i])
                .closePrice(prices[i])
                .volume(100)
                .build();
            series.addBar(bar);

            // 执行策略判断
            int lastIndex = series.getEndIndex();
            if (lastIndex >= longSma.getCountOfUnstableBars()) { // 等待指标稳定
                boolean shouldEnter = strategy.shouldEnter(lastIndex, tradingRecord);
                boolean shouldExit = strategy.shouldExit(lastIndex, tradingRecord);

                if (shouldEnter) {
                    tradingRecord.enter(lastIndex, series.getBar(lastIndex).getClosePrice(), DoubleNum.valueOf(1));
                    log.info("买入 @ {} 价格: {}", time, bar.getClosePrice());
                } else if (shouldExit) {
                    tradingRecord.exit(lastIndex, series.getBar(lastIndex).getClosePrice(), DoubleNum.valueOf(1));
                    log.info("卖出 @ {} 价格: {}", time, bar.getClosePrice());
                }
            }
        }

        // 5. 输出结果
        GrossReturnCriterion grossReturn = new GrossReturnCriterion();
        var returnPct = grossReturn.calculate(series, tradingRecord);
        log.info("\n总收益率: {}%", returnPct.doubleValue() * 100);
        log.info("交易次数: {}", tradingRecord.getPositionCount());

        // 计算最大回撤（返回值为正数，表示回撤比例）
        MaximumDrawdownCriterion maxDD = new MaximumDrawdownCriterion();
        Num maxDrawdown = maxDD.calculate(series, tradingRecord);
        log.info("最大回撤: {}%", maxDrawdown.doubleValue() * 100);
    }
}
