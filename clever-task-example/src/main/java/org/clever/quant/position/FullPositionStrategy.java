package org.clever.quant.position;

import org.clever.quant.Account;
import org.clever.quant.BarSeries;
import org.clever.quant.PositionStrategy;

/**
 * 满仓策略 <br/>
 * 1.买入时: 尽可能多的买入<br/>
 * 2.卖出时: 尽可能多的卖出<br/>
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/02 22:40 <br/>
 */
public class FullPositionStrategy implements PositionStrategy {
    @Override
    public Integer calcEnterVolume(Account tradeAccount, BarSeries barSeries, int barIdx, double price) {
        return (int) Math.floor(tradeAccount.getBalance() / price);
    }

    @Override
    public Integer calcExitVolume(Account tradeAccount, BarSeries barSeries, int barIdx, double price) {
        return (int) Math.floor(tradeAccount.getBalance() / price);
    }
}
