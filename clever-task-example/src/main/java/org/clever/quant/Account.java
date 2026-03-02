package org.clever.quant;

import java.util.List;

/**
 * 交易账户
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 16:51 <br/>
 */
public interface Account {
    /**
     * 初始总金额
     */
    double getTotalAmount();

    /**
     * 账户余额
     */
    double getBalance();

    /**
     * 所有的历史交易日志
     */
    List<TradeLog> getTradeLogs();

    /**
     * 当前账户快照
     */
    TradeAccountSnapshot getSnapshot();

    /**
     * 开仓
     *
     * @param barIdx Bar的索引位置
     * @param price  撮合成交价
     * @param volume 成交量
     * @param fee    手续费(交易成本)
     * @return 如果没有交易就返回 null
     */
    TradeLog enter(long barIdx, double price, long volume, double fee);

    /**
     * 平仓
     *
     * @param barIdx Bar的索引位置
     * @param price  撮合成交价
     * @param volume 成交量
     * @param fee    手续费(交易成本)
     * @return 如果没有交易就返回 null
     */
    TradeLog exit(long barIdx, double price, long volume, double fee);
}
