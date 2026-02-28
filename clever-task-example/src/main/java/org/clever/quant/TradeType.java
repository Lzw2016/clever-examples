package org.clever.quant;

import lombok.Getter;
import lombok.ToString;

/**
 * 交易类型(做多/做空)
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 14:57 <br/>
 */
@ToString
@Getter
public enum TradeType {
    /**
     * 买入(做多)
     */
    BUY("buy"),
    /**
     * 卖出(做空)
     */
    SELL("sell"),
    ;
    private final String name;

    TradeType(String name) {
        this.name = name;
    }
}
