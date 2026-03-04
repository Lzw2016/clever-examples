package org.clever.quant.account;

import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.TradeLog;
import org.clever.quant.TradeType;

/**
 * 模拟账号
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 20:16 <br/>
 */
public class PaperAccount extends AbstractAccount {
    /**
     * @param initAmount 初始总金额
     */
    public PaperAccount(double initAmount) {
        this(initAmount, null);
    }

    /**
     * @param initAmount 初始总金额
     * @param name       账户名
     */
    public PaperAccount(double initAmount, String name) {
        super(initAmount, name);
    }

    @Override
    public TradeLog enter(BarSeries barSeries, Bar bar, long barIdx, double price, int volume, double fee) {
        return syncWrite(() -> {
            TradeLog tradeLog = new TradeLog(bar.getCode(), TradeType.BUY, barIdx, bar.getTime(), price, volume, fee);
            super.increase(barSeries, bar, price, volume, fee);
            tradeLogs.add(tradeLog);
            return tradeLog;
        });
    }

    @Override
    public TradeLog exit(BarSeries barSeries, Bar bar, long barIdx, double price, int volume, double fee) {
        return syncWrite(() -> {
            TradeLog tradeLog = new TradeLog(bar.getCode(), TradeType.SELL, barIdx, bar.getTime(), price, volume, fee);
            super.decrease(bar.getCode(), price, volume, fee);
            tradeLogs.add(tradeLog);
            return tradeLog;
        });
    }
}
