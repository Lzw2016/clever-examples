package org.clever.quant;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 行情Bar数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/26 18:13 <br/>
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class Bar extends AbstractExtData {
    /**
     * 金融产品编码
     */
    private final String code;
    /**
     * 周期
     */
    private final Period period;
    /**
     * 时间
     */
    private final Date time;
    /**
     * 开盘价
     */
    private double open;
    /**
     * 最高价
     */
    private double high;
    /**
     * 最低价
     */
    private double low;
    /**
     * 收盘价
     */
    private double close;
    /**
     * 成交量
     */
    private long volume;
    /**
     * 成交额
     */
    private double amount;


}
