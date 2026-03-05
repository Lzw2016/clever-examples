package org.clever.quant;

/**
 * 交易员
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/02 13:47 <br/>
 */
public interface Trader {
    /**
     * 设置交易量的最小粒度
     */
    void setVolumeStep(int volumeStep);

    /**
     * 交易量的最小粒度,默认100
     */
    default int getVolumeStep() {
        return 100;
    }

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
     * 注册交易监听器,用于监听 开仓/平仓 事件
     */
    void registerTradeListener(TradeListener listener);

    /**
     * 开始监听BarSeries数据变化, 如果满足策略条件就交易(只能start一次)
     *
     * @param mainBarSeries 交易的目标BarSeries
     */
    void start(BarSeries mainBarSeries);

    /**
     * 是否已经启动了 {@link #start(BarSeries)}
     */
    boolean isStarted();
}
