package ta4j.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/21 19:37 <br/>
 */
@Data
public class StockTickData {
    /** 日期时间 */
    private Date time;
    /** 股票代码 */
    private String stock_code;
    /** 最新价 */
    private BigDecimal lastPrice;
    /** 开盘价 */
    private BigDecimal open;
    /** 最高价 */
    private BigDecimal high;
    /** 最低价 */
    private BigDecimal low;
    /** 前收盘价 */
    private BigDecimal lastClose;
    /** 成交总额 */
    private BigDecimal amount;
    /** 成交总量 */
    private Long volume;
    /** 原始成交总量 */
    private Long pvolume;
    /** 证券状态 */
    private String stockStatus;
    /** 持仓量 */
    private Long openInt;
    /** 前结算 */
    private BigDecimal lastSettlementPrice;
    /** 委卖价 */
    private List<BigDecimal> askPrice;
    /** 委买价 */
    private List<BigDecimal> bidPrice;
    /** 委卖量 */
    private List<Long> askVol;
    /** 委买量 */
    private List<Long> bidVol;
}
