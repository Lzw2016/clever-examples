package org.clever.quant;

import lombok.Builder;
import lombok.Data;
import org.clever.core.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 交易者
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/02 13:47 <br/>
 */
@Builder
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

    public void start() {
        Set<BarSeries> allBarSeries = strategy.getAllBarSeries();
        Assert.notNull(allBarSeries, "allBarSeries 不能为 null");
        Assert.notEmpty(allBarSeries, "allBarSeries 不能为空集合");


    }
}
