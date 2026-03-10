package org.clever.quant.criteria;

import org.clever.quant.Account;
import org.clever.quant.TradeLog;

/**
 * TODO 盈利交易次数
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/10 23:07 <br/>
 */
public class ProfitTradeCountCriterion extends PriceTableAnalysisCriterion<Integer> {
    @Override
    protected Integer getInitValue(Account account) {
        return 0;
    }

    @Override
    public void onEnter(TradeLog tradeLog, Account account, long barIdx) {

    }

    @Override
    public void onExit(TradeLog tradeLog, Account account, long barIdx) {

    }
}
