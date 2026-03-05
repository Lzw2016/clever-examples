package org.clever.quant.archive.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 回测账户快照
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/04 10:00 <br/>
 */
@Data
public class BacktestAccountSnapshot {
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
     * 账户余额
     */
    private BigDecimal balance;
    /**
     * 总资产
     */
    private BigDecimal totalAssets;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 删除标识(0正常 / 1删除)
     */
    private Integer delFlag;
}
