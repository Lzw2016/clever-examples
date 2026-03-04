package quant.entity;

import lombok.Data;

import java.util.Date;

/**
 * 回测备注
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/04 10:00 <br/>
 */
@Data
public class BacktestRemark {
    /**
     * 数据id
     */
    private Long id;
    /**
     * 回撤id
     */
    private Long backtestRecordId;
    /**
     * 指标对应的bar数据下标
     */
    private Long barIdx;
    /**
     * 备注信息
     */
    private String remark;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 删除标识(0正常 / 1删除)
     */
    private Integer delFlag;
}
