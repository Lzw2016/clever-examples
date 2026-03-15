package org.clever.quant;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.clever.core.Assert;

import java.util.Date;

/**
 * 行情Bar数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/26 18:13 <br/>
 */
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
public class Bar extends AbstractExtData {
    /**
     * 金融产品编码
     */
    @Getter
    private final String code;
    /**
     * 周期
     */
    @Getter
    private final Period period;
    /**
     * 时间
     */
    @Getter
    private final Date time;
    /**
     * 开盘价
     */
    private final double open;
    /**
     * 最高价
     */
    private final double high;
    /**
     * 最低价
     */
    private final double low;
    /**
     * 收盘价
     */
    private final double close;
    /**
     * 成交量
     */
    @Getter
    private final long volume;
    /**
     * 成交额
     */
    @Getter
    private final double amount;
    /**
     * 复权价格计算器
     */
    @Getter
    private final AdjustPriceCalc adjustPriceCalc;

    public Bar(String code,
               Period period,
               Date time,
               double open,
               double high,
               double low,
               double close,
               long volume,
               double amount,
               AdjustPriceCalc adjustPriceCalc) {
        Assert.isNotBlank(code, "参数 code 不能为空");
        Assert.notNull(period, "参数 period 不能为null");
        Assert.isTrue(open > 0, "参数 open 必须大于 0");
        Assert.isTrue(high > 0, "参数 high 必须大于 0");
        Assert.isTrue(low > 0, "参数 low 必须大于 0");
        Assert.isTrue(close > 0, "参数 close 必须大于 0");
        Assert.isTrue(volume >= 0, "参数 volume 必须大于等于 0");
        Assert.isTrue(amount >= 0, "参数 amount 必须大于等于 0");
        Assert.notNull(adjustPriceCalc, "参数 adjustPriceCalc 不能为null");
        this.code = code;
        this.period = period;
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.amount = amount;
        this.adjustPriceCalc = adjustPriceCalc;
    }

    public double getOpen(AdjustType adjustType) {
        return adjustPriceCalc(adjustType, open);
    }

    public double getHigh(AdjustType adjustType) {
        return adjustPriceCalc(adjustType, high);
    }

    public double getLow(AdjustType adjustType) {
        return adjustPriceCalc(adjustType, low);
    }

    public double getClose(AdjustType adjustType) {
        return adjustPriceCalc(adjustType, close);
    }

    protected double adjustPriceCalc(AdjustType adjustType, double price) {
        Assert.notNull(adjustType, "参数 adjustType 不能为 null");
        switch (adjustType) {
            case none:
                return price;
            case front:
                return adjustPriceCalc.front(time, price);
            case back:
                return adjustPriceCalc.back(time, price);
            default:
                throw new IllegalArgumentException("未知的 adjustType 值: " + adjustType);
        }
    }
}
