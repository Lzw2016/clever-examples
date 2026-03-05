package org.clever.quant.archive.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 回测记录
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/04 21:05 <br/>
 */
@Data
public class BacktestRecord {
    /**
     * 回测id
     */
    private long id;
    /**
     * 回测方案名称
     */
    private String name;
    /**
     * 回测标签名
     */
    private String tag;
    /**
     * 是否执行成功
     */
    private Boolean success;
    /**
     * 执行开始时间
     */
    private Date startTime;
    /**
     * 执行结束时间
     */
    private Date endTime;
    /**
     * 回测配置
     */
    private String config;
    /**
     * 初始本金
     */
    private BigDecimal initialCapital;
    /**
     * 结束总资产
     */
    private BigDecimal finalTotalAssets;
    /**
     * 历史最小总资产
     */
    private BigDecimal minTotalAssets;
    /**
     * 盈利金额
     */
    private BigDecimal profitAmount;
    /**
     * 总手续费
     */
    private BigDecimal totalFee;
    /**
     * 手续费占比
     */
    private BigDecimal feeRatio;
    /**
     * 累计收益率
     */
    private BigDecimal cumulativeReturnRate;
    /**
     * 平均年化收益率
     */
    private BigDecimal avgAnnualReturnRate;
    /**
     * 收益波动率
     */
    private BigDecimal returnVolatility;
    /**
     * 最大回撤
     */
    private BigDecimal maxDrawdown;
    /**
     * 亏损标准差
     */
    private BigDecimal lossStdDev;
    /**
     * 最大连续亏损时间(天)
     */
    private Integer maxConsecutiveLossDays;
    /**
     * 总交易次数
     */
    private Integer totalTradeCount;
    /**
     * 盈利交易次数
     */
    private Integer profitTradeCount;
    /**
     * 胜率
     */
    private BigDecimal winRate;
    /**
     * 盈亏比
     */
    private BigDecimal profitLossRatio;
    /**
     * 持仓时间P90
     */
    private BigDecimal holdingTimeP90;
    /**
     * 最大连续盈利次数
     */
    private Integer maxConsecutiveProfitCount;
    /**
     * 最大连续亏损次数
     */
    private Integer maxConsecutiveLossCount;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 删除标识(0正常 / 1删除)
     */
    private Integer delFlag;
}
