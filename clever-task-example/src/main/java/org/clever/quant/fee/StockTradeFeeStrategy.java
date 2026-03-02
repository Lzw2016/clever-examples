package org.clever.quant.fee;

import lombok.Data;
import org.clever.core.Assert;
import org.clever.quant.TradeFeeStrategy;

/**
 * 常规的股票交易手续费计算
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/02 22:16 <br/>
 */
@Data
public class StockTradeFeeStrategy implements TradeFeeStrategy {
    /**
     * 佣金率
     */
    private final double commissionRate;
    /**
     * 最低佣金
     */
    private final double minCommission;
    /**
     * 印花税税率
     */
    private final double stampDutyRate;
    /**
     * 过户费
     */
    private final double transferFeeRate;

    /**
     * 佣金：买卖双向收取，通常为成交金额的 0.03%(万分之三)，最低 5 元 <br/>
     * 印花税：仅卖出时收取，为成交金额的 0.1%(千分之一) <br/>
     * 过户费：买卖双向收取，为成交金额的 0.001%(十万分之一) <br/>
     */
    public StockTradeFeeStrategy() {
        this(0.0003, 5.0, 0.001, 0.00001);
    }

    /**
     * @param commissionRate  佣金率, 如: 买卖双向收取，通常为成交金额的 0.03%(万分之三)
     * @param minCommission   最低佣金, 如: 最低 5 元
     * @param stampDutyRate   印花税税率, 如: 仅卖出时收取，为成交金额的 0.1%(千分之一)
     * @param transferFeeRate 过户费, 如: 买卖双向收取，为成交金额的 0.001%(十万分之一)
     */
    public StockTradeFeeStrategy(double commissionRate, double minCommission, double stampDutyRate, double transferFeeRate) {
        Assert.isTrue(commissionRate >= 0, "参数 commissionRate 必须大于等于 0");
        Assert.isTrue(minCommission >= 0, "参数 minCommission 必须大于等于 0");
        Assert.isTrue(stampDutyRate >= 0, "参数 stampDutyRate 必须大于等于 0");
        Assert.isTrue(transferFeeRate >= 0, "参数 transferFeeRate 必须大于等于 0");
        this.commissionRate = commissionRate;
        this.minCommission = minCommission;
        this.stampDutyRate = stampDutyRate;
        this.transferFeeRate = transferFeeRate;
    }

    @Override
    public double calcEnterFee(double price, long volume) {
        double amount = price * volume;
        double commission = Math.max(amount * commissionRate, minCommission);
        double transferFee = amount * transferFeeRate;
        return commission + transferFee;
    }

    @Override
    public double calcExitFee(double price, long volume) {
        double amount = price * volume;
        double commission = Math.max(amount * commissionRate, minCommission);
        double stampDuty = amount * stampDutyRate;
        double transferFee = amount * transferFeeRate;
        return commission + stampDuty + transferFee;
    }
}
