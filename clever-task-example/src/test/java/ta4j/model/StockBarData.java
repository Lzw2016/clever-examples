package ta4j.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/21 19:37 <br/>
 */
@Data
public class StockBarData {
    /** 日期时间 */
    private Date time;
    /** 股票代码 */
    private String stock_code;
    /** 开盘价 */
    private BigDecimal open;
    /** 最高价 */
    private BigDecimal high;
    /** 最低价 */
    private BigDecimal low;
    /** 收盘价 */
    private BigDecimal close;
    /** 成交量 */
    private Long volume;
    /** 成交额 */
    private BigDecimal amount;
    /** 今结算 */
    private BigDecimal settelementPrice;
    /** 持仓量 */
    private Long openInterest;
    /** 前收价 */
    private BigDecimal preClose;
    /** 停牌标记 (0 - 正常, 1 - 停牌, -1 - 当日起复牌) */
    private Integer suspendFlag;
}
