package org.clever.quant.archive.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 回测规则数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/04 21:11 <br/>
 */
@Data
public class BacktestRule {
    /**
     * 数据id
     */
    private Long id;
    /**
     * 回测id
     */
    private Long backtestRecordId;
    /**
     * bar_series数据id集合
     */
    private List<Long> barSeriesIds;
    /**
     * 指标对应的bar数据下标
     */
    private Long barIdx;
    /**
     * 规则名称
     */
    private String name;
    /**
     * 是否满足规则
     */
    private Boolean satisfied;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 删除标识(0正常 / 1删除)
     */
    private Integer delFlag;
}
