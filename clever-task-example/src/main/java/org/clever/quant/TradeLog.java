package org.clever.quant;

import lombok.Data;
import org.clever.core.Assert;
import org.clever.core.id.SnowFlake;

import java.util.Date;

/**
 * 交易日志
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 14:57 <br/>
 */
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
     * 撮合成交价
     */
    private final double price;
    /**
     * 成交量
     */
    private final int volume;
    /**
     * 手续费(交易成本)
     */
    private final double fee;
    /**
     * 成交额
     */
    private final double amount;

    /**
     * @param code      金融产品编码
     * @param tradeType 交易类型
     * @param barIdx    Bar的索引位置
     * @param time      交易时间
     * @param price     撮合成交价
     * @param volume    成交量
     * @param fee       手续费(交易成本)
     */
    public TradeLog(String code, TradeType tradeType, long barIdx, Date time, double price, int volume, double fee) {
        Assert.isNotBlank(code, "参数 code 不能为空");
        Assert.notNull(tradeType, "参数 tradeType 不能为 null");
        Assert.isTrue(barIdx >= 0, "参数 barIdx 必须大于等于 0");
        Assert.notNull(time, "参数 time 不能为 null");
        Assert.isTrue(price > 0, "参数 price 必须大于 0");
        Assert.isTrue(volume > 0, "参数 volume 必须大于 0");
        Assert.isTrue(fee >= 0, "参数 fee 必须大于等于 0");
        this.code = code;
        this.tradeType = tradeType;
        this.barIdx = barIdx;
        this.time = time;
        this.fee = fee;
        this.price = price;
        this.volume = volume;
        if (TradeType.BUY.equals(tradeType)) {
            this.amount = price * volume + fee;
        } else {
            this.amount = price * volume - fee;
        }
    }
}
