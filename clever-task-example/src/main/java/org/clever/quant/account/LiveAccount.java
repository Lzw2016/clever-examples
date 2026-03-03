package org.clever.quant.account;

import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.TradeLog;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/03/03 13:37 <br/>
 */
public class LiveAccount extends AbstractAccount {
    /**
     * @param name 账户名
     */
    public LiveAccount(String name) {
        super(0, name);
    }

    @Override
    public TradeLog enter(BarSeries barSeries, Bar bar, long barIdx, double price, int volume, double fee) {
        return null;
    }

    @Override
    public TradeLog exit(BarSeries barSeries, Bar bar, long barIdx, double price, int volume, double fee) {
        return null;
    }
}
