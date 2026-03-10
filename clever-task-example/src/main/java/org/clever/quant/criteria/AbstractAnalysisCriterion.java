package org.clever.quant.criteria;

import org.clever.core.Assert;
import org.clever.quant.Account;
import org.clever.quant.AnalysisCriterion;
import org.clever.quant.TradeListener;
import org.clever.quant.Trader;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/03/10 21:34 <br/>
 */
public abstract class AbstractAnalysisCriterion<T> implements AnalysisCriterion<T>, TradeListener {
    /**
     * 指标值
     */
    protected volatile T value;
    /**
     * 交易账号
     */
    protected Account account;

    @Override
    public void calculate(Trader trader) {
        Assert.notNull(trader, "参数 trader 不能为 null");
        account = trader.getAccount();
        if (value == null) {
            value = getInitValue(account);
        }
        trader.registerTradeListener(this);
    }

    @Override
    public T getValue() {
        return value;
    }

    /**
     * 指标初始值
     */
    protected abstract T getInitValue(Account account);
}
