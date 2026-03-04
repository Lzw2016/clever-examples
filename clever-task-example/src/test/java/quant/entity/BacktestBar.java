package quant.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 回测bar数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/04 21:08 <br/>
 */
@Data
public class BacktestBar {
    /**
     * 数据id
     */
    private Long id;
    /**
     * 回撤id
     */
    private Long backtestRecordId;
    /**
     * bar_series_id
     */
    private Long barSeriesId;
    /**
     * bar数据下标
     */
    private Long barIdx;
    /**
     * 时间
     */
    private Date time;
    /**
     * 开盘价
     */
    private BigDecimal open;
    /**
     * 最高价
     */
    private BigDecimal high;
    /**
     * 最低价
     */
    private BigDecimal low;
    /**
     * 收盘价
     */
    private BigDecimal close;
    /**
     * 成交量
     */
    private Long volume;
    /**
     * 成交额
     */
    private BigDecimal amount;
    /**
     * 扩展数据
     */
    private String extData;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 删除标识(0正常 / 1删除)
     */
    private Integer delFlag;
}
