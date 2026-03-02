package org.clever.quant;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.clever.core.Assert;
import org.clever.core.DateUtils;

import java.util.*;

/**
 * 交易者
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/02 13:47 <br/>
 */
@Builder
@Slf4j
@Data
public class Trader {
    /**
     * 交易账户
     */
    private final Account account;
    /**
     * 交易策略
     */
    private final Strategy strategy;
    /**
     * 持仓策略
     */
    private final PositionStrategy positionStrategy;
    /**
     * 交易手续费计算策略
     */
    private final TradeFeeStrategy tradeFeeStrategy;
    /**
     * 发生交易时的监听器列表
     */
    private final List<TradeListener> listeners = new ArrayList<>();

    /**
     * @param account          交易账户
     * @param strategy         交易策略
     * @param positionStrategy 持仓策略
     * @param tradeFeeStrategy 交易手续费计算策略
     */
    public Trader(Account account, Strategy strategy, PositionStrategy positionStrategy, TradeFeeStrategy tradeFeeStrategy) {
        Assert.notNull(account, "参数 account 不能为 null");
        Assert.notNull(strategy, "参数 strategy 不能为 null");
        //Assert.notNull(positionStrategy, "参数 positionStrategy 不能为 null");
        //Assert.notNull(tradeFeeStrategy, "参数 tradeFeeStrategy 不能为 null");
        this.account = account;
        this.strategy = strategy;
        this.positionStrategy = positionStrategy;
        this.tradeFeeStrategy = tradeFeeStrategy;
    }

    /**
     * 开始交易
     *
     * @param mainBarSeries 交易的目标BarSeries
     */
    public void start(BarSeries mainBarSeries) {
        Assert.notNull(mainBarSeries, "参数 mainBarSeries 不能为 null");
        Set<BarSeries> allBarSeries = strategy.getAllBarSeries();
        Assert.notNull(allBarSeries, "allBarSeries 不能为 null");
        Assert.notEmpty(allBarSeries, "allBarSeries 不能为空集合");
        allBarSeries.add(mainBarSeries);
        BarListener barListener = new MultipleBarListener(this, mainBarSeries, allBarSeries.size());
        for (BarSeries barSeries : allBarSeries) {
            barSeries.registerBarListener(barListener);
        }
    }

    protected static class MultipleBarListener implements BarListener {
        private final Trader trader;
        /**
         * 交易的目标BarSeries
         */
        private final BarSeries mainBarSeries;
        /**
         * BarSeries的总数量
         */
        private final int barSeriesCount;
        /**
         * 用于记录最后一个 barIdx 的位置 {@code Map<bar.getCode(), barIdx>}
         */
        private final Map<String, Long> barIdxMap;
        /**
         * 最后一个 Bar
         */
        private volatile Bar lastBar = null;
        /**
         * Bar 的总数量
         */
        private volatile long barCount = 0;
        /**
         * 最后一次计算策略的 barIdx
         */
        private volatile long lastBarIdx = -1;

        public MultipleBarListener(Trader trader, BarSeries mainBarSeries, int barSeriesCount) {
            this.trader = trader;
            this.mainBarSeries = mainBarSeries;
            this.barSeriesCount = barSeriesCount;
            this.barIdxMap = new HashMap<>(barSeriesCount);
        }

        @Override
        public synchronized void onAppendBar(Bar bar, long barIdx) {
            barCount++;
            Assert.isTrue(
                lastBar == null || Objects.equals(lastBar.getPeriod(), bar.getPeriod()),
                () -> String.format("参数 bar.period 值必须为 %s", lastBar.getPeriod())
            );
            if (lastBar == null) {
                lastBar = bar;
            }
            barIdxMap.put(bar.getCode(), barIdx);
            if (barIdxMap.size() < barSeriesCount) {
                Assert.isTrue(
                    barCount < (barSeriesCount * 32L),
                    String.format("请检查BarSeries的数据接入是否有问题, Bar数据总量为%s, 还存在未出现Bar数据的品种", barCount)
                );
                return;
            }
            long minBarIdx = Collections.min(barIdxMap.values());
            if (minBarIdx <= lastBarIdx) {
                return;
            }
            lastBarIdx = minBarIdx;
            Bar mainBar = mainBarSeries.getBar(lastBarIdx);
            doTrading(mainBar, barIdx);
        }

        protected void doTrading(final Bar mainBar, final long barIdx) {
            String date = DateUtils.formatToString(mainBar.getTime(), DateUtils.yyyy_MM_dd);
            String price = String.format("%.4f", mainBar.getClose());
            if (trader.strategy.shouldEnter(lastBarIdx, trader.account.getSnapshot())) {
                log.info("买入 @ {} 价格: {}", date, price);
                trader.account.enter(barIdx, mainBar.getClose(), 1000, 5);
            }
            if (trader.strategy.shouldExit(lastBarIdx, trader.account.getSnapshot())) {
                log.info("卖出 @ {} 价格: {}", date, price);
                trader.account.exit(barIdx, mainBar.getClose(), 1000, 5);
            }
        }
    }
}
