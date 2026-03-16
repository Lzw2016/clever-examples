package org.clever.quant;

import java.util.Map;

/**
 * 发生实际交易时的监听
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/02 13:26 <br/>
 */
public interface TradeListener {
    /**
     * Bar 数据更新
     *
     * @param mainBar 交易的目标 Bar
     * @param bars    辅助 Bar
     * @param barIdx  Bar的索引位置
     */
    default void onBars(Bar mainBar, Map<BarSeries, Bar> bars, long barIdx) {
    }

    /**
     * 处理“分红配送”之后的回调通知
     *
     * @param dividendLog 分红配送日志
     * @param account     交易账户
     * @param barIdx      Bar的索引位置
     */
    default void onDividend(DividendLog dividendLog, Account account, long barIdx) {
    }

    /**
     * 开仓时回调通知
     *
     * @param tradeLog 交易日志
     * @param account  交易账户
     * @param barIdx   Bar的索引位置
     */
    void onEnter(TradeLog tradeLog, Account account, long barIdx);

    /**
     * 平仓时回调通知
     *
     * @param tradeLog 交易日志
     * @param account  交易账户
     * @param barIdx   Bar的索引位置
     */
    void onExit(TradeLog tradeLog, Account account, long barIdx);
}
