package org.clever.quant;

/**
 * 交易手续费计算策略
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 16:02 <br/>
 */
public interface TradeFeeStrategy {
    /**
     * 计算开仓手续费
     *
     * @param price  撮合成交价
     * @param volume 成交量
     */
    double calcEnterFee(double price, long volume);

    /**
     * 计算平仓手续费
     *
     * @param price  撮合成交价
     * @param volume 成交量
     */
    double calcExitFee(double price, long volume);
}
