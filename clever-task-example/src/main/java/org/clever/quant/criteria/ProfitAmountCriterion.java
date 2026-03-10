package org.clever.quant.criteria;

import org.clever.quant.Account;
import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.TradeLog;

import java.util.Map;

/**
 * 盈利金额
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/10 21:55 <br/>
 */
public class ProfitAmountCriterion extends PriceTableAnalysisCriterion<Double> {
    @Override
    protected Double getInitValue(Account account) {
        return 0.0;
    }

    @Override
    public void onBars(Bar mainBar, Map<BarSeries, Bar> bars, long barIdx) {
        super.onBars(mainBar, bars, barIdx);
    }

    @Override
    public void onEnter(TradeLog tradeLog, Account account, long barIdx) {
    }

    @Override
    public void onExit(TradeLog tradeLog, Account account, long barIdx) {
    }

    @Override
    public Double getValue() {
        double totalAssets = account.getTotalAssets(priceTable);
        value = totalAssets - account.getInitAmount();
        return value;
    }
}
