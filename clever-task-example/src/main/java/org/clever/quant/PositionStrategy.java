package org.clever.quant;

/**
 * 持仓策略
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 16:38 <br/>
 */
public interface PositionStrategy {
    /**
     * 计算开仓量
     *
     * @param accountSnapshot 交易账户快照
     * @param price           预期成交价
     * @param barSeries       BarSeries
     * @param bar             Bar数据
     * @param barIdx          Bar的索引位置
     * @return 返回交易量, 如果不交易就返回 null
     */
    Integer calcEnterVolume(TradeAccountSnapshot accountSnapshot, double price, BarSeries barSeries, Bar bar, long barIdx);

    /**
     * 计算平仓量
     *
     * @param accountSnapshot 交易账户快照
     * @param price           预期成交价
     * @param barSeries       BarSeries
     * @param bar             Bar数据
     * @param barIdx          Bar的索引位置
     * @return 返回交易量, 如果不交易就返回 null
     */
    Integer calcExitVolume(TradeAccountSnapshot accountSnapshot, double price, BarSeries barSeries, Bar bar, long barIdx);
}
