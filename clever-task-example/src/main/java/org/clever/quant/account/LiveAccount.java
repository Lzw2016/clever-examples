package org.clever.quant.account;

import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.TradeLog;
import org.clever.quant.TradeType;

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
        return syncWrite(() -> {
            // TODO 调用QMT交易下单接口完成下单交易
            TradeLog tradeLog = new TradeLog(bar.getCode(), TradeType.BUY, barIdx, bar.getTime(), price, volume, fee);
            super.increase(barSeries, bar, price, volume, fee);
            tradeLogs.add(tradeLog);
            return tradeLog;
        });
    }

    @Override
    public TradeLog exit(BarSeries barSeries, Bar bar, long barIdx, double price, int volume, double fee) {
        return syncWrite(() -> {
            // TODO 调用QMT交易下单接口完成下单交易
            TradeLog tradeLog = new TradeLog(bar.getCode(), TradeType.SELL, barIdx, bar.getTime(), price, volume, fee);
            super.decrease(bar.getCode(), price, volume, fee);
            tradeLogs.add(tradeLog);
            return tradeLog;
        });
    }
}
