package org.clever.quant.account;

import org.clever.core.Assert;
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
     * @param totalAmount 初始总金额
     */
    public PaperAccount(double totalAmount) {
        this(totalAmount, null);
    }

    /**
     * @param totalAmount 初始总金额
     * @param name        账户名
     */
    public PaperAccount(double totalAmount, String name) {
        super(totalAmount, name);
    }

    @Override
    public TradeLog enter(BarSeries barSeries, Bar bar, long barIdx, double price, int volume, double fee) {
        return syncWrite(() -> {
            TradeLog tradeLog = new TradeLog(
                bar.getCode(),
                TradeType.BUY,
                barIdx,
                bar.getTime(),
                price,
                volume,
                fee
            );
            // 扣减余额
            double amount = volume * price + fee;
            double balance = this.balance;
            this.balance = balance - amount;
            Assert.isTrue(this.balance >= 0, String.format("开仓之后 balance 不能小于 0, balance=%s", String.format("%.4f", this.balance)));
            // TODO 更新持仓状态

            //  增加历史记录
            tradeLogs.add(tradeLog);
            return tradeLog;
        });
    }

    @Override
    public TradeLog exit(BarSeries barSeries, Bar bar, long barIdx, double price, int volume, double fee) {
        return syncWrite(() -> {
            TradeLog tradeLog = new TradeLog(
                bar.getCode(),
                TradeType.SELL,
                barIdx,
                bar.getTime(),
                price,
                volume,
                fee
            );
            //  增加余额
            double amount = volume * price - fee;
            double balance = this.balance;
            this.balance = balance + amount;
            // TODO 更新持仓状态

            // 增加历史记录
            tradeLogs.add(tradeLog);
            return tradeLog;
        });
    }
}
