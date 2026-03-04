package quant.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 回测交易日志
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/04 10:00 <br/>
 */
@Data
public class BacktestTradeLog {
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
     * 交易类型
     */
    private String tradeType;
    /**
     * 交易时间
     */
    private Date time;
    /**
     * 成交价
     */
    private BigDecimal price;
    /**
     * 成交量
     */
    private Long volume;
    /**
     * 手续费(交易成本)
     */
    private BigDecimal fee;
    /**
     * 成交额
     */
    private BigDecimal amount;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 删除标识(0正常 / 1删除)
     */
    private Integer delFlag;
}
