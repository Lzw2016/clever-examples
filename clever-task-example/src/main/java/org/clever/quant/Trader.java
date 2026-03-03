package org.clever.quant;

/**
 * 交易者
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/02 13:47 <br/>
 */
public interface Trader {
    /**
     * 交易账户
     */
    Account getAccount();

    /**
     * 交易策略
     */
    Strategy getStrategy();

    /**
     * 持仓策略
     */
    PositionStrategy getPositionStrategy();

    /**
     * 交易手续费计算策略
     */
    TradeFeeStrategy getTradeFeeStrategy();

    /**
     * 开始交易
     *
     * @param mainBarSeries 交易的目标BarSeries
     */
    void start(BarSeries mainBarSeries);
}
