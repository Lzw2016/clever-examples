package org.clever.quant;

import lombok.Data;
import org.clever.core.Assert;
import org.clever.core.id.SnowFlake;

import java.util.Date;

/**
 * 分红配送日志
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/16 16:15 <br/>
 */
@Data
public class DividendLog {
    /**
     * 日志id
     */
    private final long id = SnowFlake.SNOW_FLAKE.nextId();
    /**
     * 金融产品编码
     */
    private final String code;
    /**
     * 除权除息日期所对应的Bar的索引位置
     */
    private final long barIdx;
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
    /**
     * 红利税
     */
    private final double dividendTax;

    public DividendLog(String code,
                       long barIdx,
                       Date exDate,
                       double bonusRatio,
                       double transferRatio,
                       double cashDividendPerShare,
                       double rationRatio,
                       double rationPrice,
                       double dividendTax) {
        Assert.isNotBlank(code, "参数 code 不能为空");
        Assert.isTrue(barIdx >= 0, "参数 barIdx 必须大于等于0");
        Assert.notNull(exDate, "参数 code 不能为 null");
        Assert.isTrue(bonusRatio >= 0, "参数 bonusRatio 必须大于等于0");
        Assert.isTrue(transferRatio >= 0, "参数 transferRatio 必须大于等于0");
        Assert.isTrue(cashDividendPerShare >= 0, "参数 cashDividendPerShare 必须大于等于0");
        Assert.isTrue(rationRatio >= 0, "参数 rationRatio 必须大于等于0");
        Assert.isTrue(rationPrice >= 0, "参数 rationPrice 必须大于等于0");
        Assert.isTrue(dividendTax >= 0, "参数 dividendTax 必须大于等于0");
        this.code = code;
        this.barIdx = barIdx;
        this.exDate = exDate;
        this.bonusRatio = bonusRatio;
        this.transferRatio = transferRatio;
        this.cashDividendPerShare = cashDividendPerShare;
        this.rationRatio = rationRatio;
        this.rationPrice = rationPrice;
        this.dividendTax = dividendTax;
    }
}
