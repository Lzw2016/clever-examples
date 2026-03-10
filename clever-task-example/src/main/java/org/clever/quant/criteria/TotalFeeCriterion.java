package org.clever.quant.criteria;

import org.clever.quant.Account;
import org.clever.quant.TradeLog;

/**
 * 总手续费
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/10 22:03 <br/>
 */
public class TotalFeeCriterion extends AbstractAnalysisCriterion<Double> {
    @Override
    protected Double getInitValue(Account account) {
        return 0.0;
    }

    @Override
    public synchronized void onEnter(TradeLog tradeLog, Account account, long barIdx) {
        value = value + tradeLog.getFee();
    }

    @Override
    public synchronized void onExit(TradeLog tradeLog, Account account, long barIdx) {
        value = value + tradeLog.getFee();
    }
}
