package org.clever.quant.criteria;

import org.clever.quant.Account;
import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.TradeLog;

import java.util.Map;

/**
 * 最大回撤
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/10 22:45 <br/>
 */
public class HistoryMaxDrawdownCriterion extends PriceTableAnalysisCriterion<Double> {
    private volatile double maxTotalAssets;

    @Override
    protected Double getInitValue(Account account) {
        maxTotalAssets = account.getInitAmount();
        return 0.0;
    }

    @Override
    public void onBars(Bar mainBar, Map<BarSeries, Bar> bars, long barIdx) {
        super.onBars(mainBar, bars, barIdx);
        double totalAssets = account.getTotalAssets(priceTable);
        if (totalAssets >= maxTotalAssets) {
            maxTotalAssets = totalAssets;
            return;
        }
        double drawdown = (maxTotalAssets - totalAssets) / maxTotalAssets * 100;
        if (value < drawdown) {
            value = drawdown;
        }
    }

    @Override
    public void onEnter(TradeLog tradeLog, Account account, long barIdx) {
    }

    @Override
    public void onExit(TradeLog tradeLog, Account account, long barIdx) {
    }
}
