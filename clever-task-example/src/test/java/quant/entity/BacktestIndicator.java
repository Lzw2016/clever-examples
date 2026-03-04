package quant.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 回测指标数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/04 10:00 <br/>
 */
@Data
public class BacktestIndicator {
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
     * 指标对应的bar数据下标
     */
    private Long barIdx;
    /**
     * 指标类名
     */
    private String name;
    /**
     * 指标值类型
     */
    private String valType;
    /**
     * 指标数值
     */
    private BigDecimal numberVal;
    /**
     * 指标Boolean值
     */
    private Boolean boolVal;
    /**
     * 指标字符串值
     */
    private String stringVal;
    /**
     * 指标对象值
     */
    private String objVal;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 删除标识(0正常 / 1删除)
     */
    private Integer delFlag;
}
