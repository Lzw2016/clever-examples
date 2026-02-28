package org.clever.quant;

/**
 * 持仓策略
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 16:38 <br/>
 */
public interface PositionStrategy {
    /**
     * 买入(做多)
     *
     * @param tradeAccount 交易账户
     * @param barIdx       Bar的索引位置
     * @param price        预期成交价
     * @return 如果没有交易就返回 null
     */
    TradeLog enter(Account tradeAccount, int barIdx, double price);

    /**
     * 卖出(做空)
     *
     * @param tradeAccount 交易账户
     * @param barIdx       Bar的索引位置
     * @param price        预期成交价
     * @return 如果没有交易就返回 null
     */
    TradeLog exit(Account tradeAccount, int barIdx, double price);
}
