package org.clever.quant.archive.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 回测分红送股日志
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/16 21:41 <br/>
 */
@Data
public class BacktestDividendLog {
    /**
     * 数据id
     */
    private Long id;
    /**
     * 回测id
     */
    private Long backtestRecordId;
    /**
     * 指标对应的bar数据下标
     */
    private Long barIdx;
    /**
     * 金融产品编码
     */
    private String code;
    /**
     * 除权除息日期
     */
    private Date exDate;
    /**
     * 送股比例(如10送5 → 0.5)
     */
    private BigDecimal bonusRatio;
    /**
     * 转股比例(如10转5 → 0.5)
     */
    private BigDecimal transferRatio;
    /**
     * 现金分红(每股派现金额, 如10派2元 → 0.2)
     */
    private BigDecimal cashDividendPerShare;
    /**
     * 配股比例(如10配3 → 0.3, 无配股则为0)
     */
    private BigDecimal rationRatio;
    /**
     * 配股价格(元/股, 无配股则为0)
     */
    private BigDecimal rationPrice;
    /**
     * 红利税
     */
    private BigDecimal dividendTax;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 删除标识(0正常 / 1删除)
     */
    private Integer delFlag;
}
