package org.clever.quant;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 分红配送数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/15 13:13 <br/>
 */
@Builder
@Data
public class Dividend {
    /**
     * 金融产品编码
     */
    private final String code;
    /**
     * 除权除息日期
     */
    private final Date exDate;
    /**
     * 送股比例(如10送5 → 0.5)
     */
    private final double bonusRatio;
    /**
     * 转股比例(如10转5 → 0.5)
     */
    private final double transferRatio;
    /**
     * 现金分红(每股派现金额, 如10派2元 → 0.2)
     */
    private final double cashDividendPerShare;
    /**
     * 配股比例(如10配3 → 0.3, 无配股则为0)
     */
    private final double rationRatio;
    /**
     * 配股价格(元/股, 无配股则为0)
     */
    private final double rationPrice;
}
