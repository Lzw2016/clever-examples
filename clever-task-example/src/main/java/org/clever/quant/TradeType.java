package org.clever.quant;

import lombok.Getter;
import lombok.ToString;

/**
 * 交易类型(开仓/平仓)
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 14:57 <br/>
 */
@ToString
@Getter
public enum TradeType {
    /**
     * 开仓
     */
    BUY("buy"),
    /**
     * 平仓
     */
    SELL("sell"),
    ;
    private final String name;

    TradeType(String name) {
        this.name = name;
    }
}
