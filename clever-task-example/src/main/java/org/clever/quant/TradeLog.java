package org.clever.quant;

import lombok.Builder;
import lombok.Data;
import org.clever.core.id.SnowFlake;

import java.util.Date;

/**
 * 交易日志
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 14:57 <br/>
 */
@Builder
@Data
public class TradeLog {
    /**
     * 交易id
     */
    private final long id = SnowFlake.SNOW_FLAKE.nextId();
    /**
     * 金融产品编码
     */
    private final String code;
    /**
     * 交易类型
     */
    private final TradeType tradeType;
    /**
     * Bar的索引位置
     */
    private final long barIdx;
    /**
     * 交易时间
     */
    private final Date time;
    /**
     * 手续费(交易成本)
     */
    private final double fee;
    /**
     * 撮合成交价
     */
    private final double price;
    /**
     * 成交量
     */
    private final long volume;
    /**
     * 成交额
     */
    private final double amount;
}
