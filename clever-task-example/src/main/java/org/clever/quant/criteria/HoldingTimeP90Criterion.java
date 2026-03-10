package org.clever.quant.criteria;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.clever.quant.Account;
import org.clever.quant.TradeLog;

/**
 * TODO 持仓时间P90
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/10 23:15 <br/>
 */
public class HoldingTimeP90Criterion extends AbstractAnalysisCriterion<Double> {
    private final Percentile percentile;

    public HoldingTimeP90Criterion() {
        this.percentile = new Percentile(90);
    }

    @Override
    protected Double getInitValue(Account account) {
        return 0.0;
    }

    @Override
    public void onEnter(TradeLog tradeLog, Account account, long barIdx) {
    }

    @Override
    public void onExit(TradeLog tradeLog, Account account, long barIdx) {
        // TODO 持仓时间P90
    }

    @Override
    public Double getValue() {
        // percentile.setData();
        // value = percentile.evaluate();
        return value;
    }
}
