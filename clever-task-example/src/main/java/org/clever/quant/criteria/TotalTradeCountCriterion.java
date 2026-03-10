package org.clever.quant.criteria;

import org.clever.quant.Account;
import org.clever.quant.TradeLog;

/**
 * 总交易次数
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/10 23:05 <br/>
 */
public class TotalTradeCountCriterion extends AbstractAnalysisCriterion<Integer> {
    @Override
    protected Integer getInitValue(Account account) {
        return 0;
    }

    @Override
    public synchronized void onEnter(TradeLog tradeLog, Account account, long barIdx) {
        value++;
    }

    @Override
    public synchronized void onExit(TradeLog tradeLog, Account account, long barIdx) {
        value++;
    }

    @Override
    public Integer getValue() {
        if (value <= 0) {
            return 0;
        }
        return value / 2 + 1;
    }
}
