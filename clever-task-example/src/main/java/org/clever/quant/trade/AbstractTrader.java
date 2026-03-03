package org.clever.quant.trade;

import lombok.Getter;
import org.clever.core.Assert;
import org.clever.quant.*;
import org.clever.quant.utils.TradeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/03/03 10:20 <br/>
 */
@Getter
public abstract class AbstractTrader implements Trader, BarListener {
    protected final transient Logger log = LoggerFactory.getLogger(getClass());
    /**
     * 交易账户
     */
    protected final Account account;
    /**
     * 交易策略
     */
    protected final Strategy strategy;
    /**
     * 持仓策略
     */
    protected final PositionStrategy positionStrategy;
    /**
     * 交易手续费计算策略
     */
    protected final TradeFeeStrategy tradeFeeStrategy;
    /**
     * 发生交易时的监听器列表
     */
    protected final List<TradeListener> listeners = new ArrayList<>();
    /**
     * 交易量的最小粒度,默认100
     */
    protected volatile int volumeStep = 100;
    /**
     * 交易的目标BarSeries
     */
    protected volatile BarSeries mainBarSeries;
    /**
     * BarSeries的总数量
     */
    protected volatile int barSeriesCount;
    /**
     * 用于记录最后一个 barIdx 的位置 {@code Map<bar.getCode(), barIdx>}
     */
    protected volatile Map<String, Long> barIdxMap;
    /**
     * 最后一个 Bar
     */
    protected volatile Bar lastBar;
    /**
     * Bar 的总数量
     */
    protected volatile long barCount = 0;
    /**
     * 最后一次计算策略的 barIdx
     */
    protected volatile long lastBarIdx = -1;
    /**
     * 模拟时, 下一个 Bar 是否开仓
     */
    protected volatile boolean nextBarEnter = false;
    /**
     * 模拟时, 下一个 Bar 是否平仓
     */
    protected volatile boolean nextBarExit = false;

    /**
     * @param account          交易账户
     * @param strategy         交易策略
     * @param positionStrategy 持仓策略
     * @param tradeFeeStrategy 交易手续费计算策略
     */
    public AbstractTrader(Account account, Strategy strategy, PositionStrategy positionStrategy, TradeFeeStrategy tradeFeeStrategy) {
        Assert.notNull(account, "参数 account 不能为 null");
        Assert.notNull(strategy, "参数 strategy 不能为 null");
        Assert.notNull(positionStrategy, "参数 positionStrategy 不能为 null");
        Assert.notNull(tradeFeeStrategy, "参数 tradeFeeStrategy 不能为 null");
        Set<BarSeries> allBarSeries = strategy.getAllBarSeries();
        Assert.notNull(allBarSeries, "allBarSeries 不能为 null");
        Assert.notEmpty(allBarSeries, "allBarSeries 不能为空集合");
        this.account = account;
        this.strategy = strategy;
        this.positionStrategy = positionStrategy;
        this.tradeFeeStrategy = tradeFeeStrategy;
    }

    @Override
    public void setVolumeStep(int volumeStep) {
        Assert.isTrue(volumeStep > 0, "参数 volumeStep 必须大于 0");
        this.volumeStep = volumeStep;
    }

    @Override
    public synchronized void registerTradeListener(TradeListener listener) {
        Assert.notNull(listener, "参数 listener 不能为 null");
        boolean exist = listeners.stream().anyMatch(item -> item == listener);
        if (exist) {
            return;
        }
        listeners.add(listener);
    }

    @Override
    public void start(BarSeries mainBarSeries) {
        Assert.notNull(mainBarSeries, "参数 mainBarSeries 不能为 null");
        Assert.isNull(this.mainBarSeries, "不能重复调用 start");
        this.mainBarSeries = mainBarSeries;
        final Set<BarSeries> allBarSeries = new HashSet<>(strategy.getAllBarSeries());
        allBarSeries.add(mainBarSeries);
        this.barSeriesCount = allBarSeries.size();
        this.barIdxMap = new HashMap<>(barSeriesCount);
        for (BarSeries barSeries : allBarSeries) {
            barSeries.registerBarListener(this);
        }
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
        // 等待 barIdx 对齐
        final long minBarIdx = Collections.min(barIdxMap.values());
        if (minBarIdx <= lastBarIdx) {
            return;
        }
        // TODO 更新 account 中 Position 的 availableVolume
        // 交易逻辑
        lastBarIdx = minBarIdx;
        final boolean liveTrading = isLiveTrading();
        final Bar mainBar = mainBarSeries.getBar(lastBarIdx);
        if (!liveTrading) {
            // 模拟
            if (nextBarEnter) {
                doEnter(mainBar, lastBarIdx);
            }
            if (nextBarExit) {
                doExit(mainBar, lastBarIdx);
            }
        }
        final boolean enter = strategy.shouldEnter(lastBarIdx, account);
        final boolean exit = strategy.shouldExit(lastBarIdx, account);
        nextBarEnter = false;
        nextBarExit = false;
        if (liveTrading) {
            // 实盘
            if (isMarketOpen(mainBar.getCode(), mainBar)) {
                // 开市状态
                if (enter) {
                    doEnter(mainBar, lastBarIdx);
                }
                if (exit) {
                    doExit(mainBar, lastBarIdx);
                }
            }
        } else {
            // 模拟
            nextBarEnter = enter;
            nextBarExit = exit;
        }
    }

    /**
     * 开仓
     */
    protected void doEnter(final Bar mainBar, final long barIdx) {
        final double price = calcEnterPrice(mainBar, barIdx);
        Integer volume = TradeUtils.calcEnterVolume(positionStrategy, tradeFeeStrategy, volumeStep, account, price, mainBarSeries, mainBar, barIdx);
        if (volume == null) {
            return;
        }
        volume = volume - (volume % volumeStep);
        if (volume <= 0) {
            return;
        }
        final double fee = tradeFeeStrategy.calcEnterFee(price, volume);
        final TradeLog tradeLog = account.enter(mainBarSeries, mainBar, barIdx, price, volume, fee);
        if (tradeLog == null) {
            return;
        }
        emitEnterEvent(tradeLog, account);
    }

    /**
     * 平仓
     */
    protected void doExit(final Bar mainBar, final long barIdx) {
        final double price = calcExitPrice(mainBar, barIdx);
        Integer volume = positionStrategy.calcExitVolume(account, price, mainBarSeries, mainBar, barIdx);
        if (volume == null) {
            return;
        }
        volume = volume - (volume % volumeStep);
        if (volume <= 0) {
            return;
        }
        // TODO 判断当前有没有这么多持仓量
        final double fee = tradeFeeStrategy.calcEnterFee(price, volume);
        final TradeLog tradeLog = account.exit(mainBarSeries, mainBar, barIdx, price, volume, fee);
        if (tradeLog == null) {
            return;
        }
        emitExitEvent(tradeLog, account);
    }

    /**
     * 开仓事件
     */
    protected void emitEnterEvent(TradeLog tradeLog, Account account) {
        for (TradeListener listener : listeners) {
            try {
                listener.onEnter(tradeLog, account);
            } catch (Exception err) {
                log.error("onEnter事件回调异常, listener={}", listener, err);
                // System.exit(-1);
            }
        }
    }

    /**
     * 平仓事件
     */
    protected void emitExitEvent(TradeLog tradeLog, Account account) {
        for (TradeListener listener : listeners) {
            try {
                listener.onExit(tradeLog, account);
            } catch (Exception err) {
                log.error("onExit事件回调异常, listener={}", listener, err);
                // System.exit(-1);
            }
        }
    }

    /**
     * 是否实盘交易(返回一个固定值)
     */
    protected abstract boolean isLiveTrading();

    /**
     * 当前市场是否开市(仅{@link #isLiveTrading()}返回true时有用)
     *
     * @param code 金融产品编码
     * @param bar  金融产品相关的Bar数据
     */
    protected abstract boolean isMarketOpen(String code, Bar bar);

    /**
     * 计算开仓价格
     *
     * @param bar    Bar数据
     * @param barIdx Bar的索引位置
     */
    protected abstract double calcEnterPrice(Bar bar, long barIdx);

    /**
     * 计算平仓价格
     *
     * @param bar    Bar数据
     * @param barIdx Bar的索引位置
     */
    protected abstract double calcExitPrice(Bar bar, long barIdx);
}

