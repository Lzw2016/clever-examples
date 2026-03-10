package org.clever.quant.criteria;

import org.clever.quant.Account;
import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.TradeLog;

import java.util.Map;

/**
 * 历史最小总资产
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/10 21:19 <br/>
 */
public class HistoryMinTotalAssetsCriterion extends PriceTableAnalysisCriterion<Double> {
    @Override
    protected Double getInitValue(Account account) {
        return account.getInitAmount();
    }

    @Override
    public synchronized void onBars(Bar mainBar, Map<BarSeries, Bar> bars, long barIdx) {
        super.onBars(mainBar, bars, barIdx);
        double totalAssets = account.getTotalAssets(priceTable);
        if (value == null) {
            value = totalAssets;
        } else {
            value = Math.min(value, totalAssets);
        }
    }

    @Override
    public void onEnter(TradeLog tradeLog, Account account, long barIdx) {
    }

    @Override
    public void onExit(TradeLog tradeLog, Account account, long barIdx) {
    }
}
