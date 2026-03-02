package org.clever.quant.account;

import org.clever.quant.TradeLog;

/**
 * 模拟账号
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 20:16 <br/>
 */
public class SimulationAccount extends AbstractAccount {
    /**
     * @param totalAmount 初始总金额
     */
    public SimulationAccount(double totalAmount) {
        this(totalAmount, null);
    }

    /**
     * @param totalAmount 初始总金额
     * @param name        账户名
     */
    public SimulationAccount(double totalAmount, String name) {
        super(totalAmount, name);
    }

    @Override
    public TradeLog enter(long barIdx, double price, int volume, double fee) {
        return null;
    }

    @Override
    public TradeLog exit(long barIdx, double price, int volume, double fee) {
        return null;
    }
}
