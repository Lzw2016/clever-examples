package org.clever.quant.position;

import lombok.Data;
import org.clever.core.Assert;
import org.clever.quant.Account;
import org.clever.quant.BarSeries;
import org.clever.quant.PositionStrategy;

/**
 * 固定买卖金额持仓策略 <br/>
 * 1.买入时: 按照指定的{@code volume}尽量执行买入<br/>
 * 2.卖出时: 按照指定的{@code volume}尽量执行卖出<br/>
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/02 22:49 <br/>
 */
@Data
public class FixedAmountPositionStrategy implements PositionStrategy {
    /**
     * 指定的买金额
     */
    private final double amount;

    /**
     * @param amount 指定的买金额
     */
    public FixedAmountPositionStrategy(double amount) {
        Assert.isTrue(amount > 0, "参数 amount 必须大于 0");
        this.amount = amount;
    }

    @Override
    public Integer calcEnterVolume(Account tradeAccount, BarSeries barSeries, int barIdx, double price) {
        return (int) Math.floor(amount / price);
    }

    @Override
    public Integer calcExitVolume(Account tradeAccount, BarSeries barSeries, int barIdx, double price) {
        return (int) Math.floor(amount / price);
    }
}
