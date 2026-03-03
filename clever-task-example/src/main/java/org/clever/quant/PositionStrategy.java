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
     * @param tradeAccount 交易账户
     * @param barSeries    BarSeries
     * @param barIdx       Bar的索引位置
     * @param price        预期成交价
     * @return 返回交易量, 如果不交易就返回 null
     */
    Integer calcEnterVolume(Account tradeAccount, BarSeries barSeries, int barIdx, double price);

    /**
     * 计算平仓量
     *
     * @param tradeAccount 交易账户
     * @param barSeries    BarSeries
     * @param barIdx       Bar的索引位置
     * @param price        预期成交价
     * @return 返回交易量, 如果不交易就返回 null
     */
    Integer calcExitVolume(Account tradeAccount, BarSeries barSeries, int barIdx, double price);
}
