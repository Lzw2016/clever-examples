package org.clever.quant;

/**
 * 发生实际交易时的监听
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/02 13:26 <br/>
 */
public interface TradeListener {
    /**
     * 开仓时回调通知
     *
     * @param tradeLog 交易日志
     * @param account  交易账户
     */
    void onEnter(TradeLog tradeLog, Account account);

    /**
     * 平仓时回调通知
     *
     * @param tradeLog 交易日志
     * @param account  交易账户
     */
    void onExit(TradeLog tradeLog, Account account);
}
