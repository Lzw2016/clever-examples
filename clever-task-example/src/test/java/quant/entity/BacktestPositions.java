package quant.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 回测持仓数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/04 10:00 <br/>
 */
@Data
public class BacktestPositions {
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
     * 金融产品编码
     */
    private String code;
    /**
     * 持有总量
     */
    private Long volume;
    /**
     * 可用量
     */
    private Long availableVolume;
    /**
     * 平均成本价
     */
    private BigDecimal avgCostPrice;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 删除标识(0正常 / 1删除)
     */
    private Integer delFlag;
}
