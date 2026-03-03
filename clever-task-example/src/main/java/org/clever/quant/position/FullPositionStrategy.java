package org.clever.quant.position;

import org.clever.quant.*;

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
    public Integer calcEnterVolume(Account account, double price, BarSeries barSeries, Bar bar, long barIdx) {
        return (int) Math.floor(account.getBalance() / price);
    }

    @Override
    public Integer calcExitVolume(Account account, double price, BarSeries barSeries, Bar bar, long barIdx) {
        Position position = account.getPosition(bar.getCode());
        if (position == null) {
            return null;
        }
        return position.getAvailableVolume();
    }
}
