package org.clever.quant;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 行情Bar数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/26 18:13 <br/>
 */
@Builder
@Data
public class Bar {
    /**
     * 金融产品编码
     */
    private final String code;
    /**
     * 周期
     */
    private final Period period;
    /**
     * 开始时间(不包含)
     */
    private final Date beginTime;
    /**
     * 结束时间(包含)
     */
    private final Date endTime;
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
