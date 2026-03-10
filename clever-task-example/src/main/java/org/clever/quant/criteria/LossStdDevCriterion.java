package org.clever.quant.criteria;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.clever.quant.Account;
import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.TradeLog;

import java.util.Map;

/**
 * 亏损标准差
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/04 14:04 <br/>
 */
public class LossStdDevCriterion extends PriceTableAnalysisCriterion<Double> {
    private volatile double preTotalAssets;
    private final DescriptiveStatistics stats = new DescriptiveStatistics();

    @Override
    protected Double getInitValue(Account account) {
        preTotalAssets = account.getInitAmount();
        return 0.0;
    }

    @Override
    public void onBars(Bar mainBar, Map<BarSeries, Bar> bars, long barIdx) {
        super.onBars(mainBar, bars, barIdx);
        double totalAssets = account.getTotalAssets(priceTable);
        double returnRate = (totalAssets - preTotalAssets) / preTotalAssets * 100;
        if (returnRate < 0) {
            stats.addValue(returnRate);
        }
    }

    @Override
    public void onEnter(TradeLog tradeLog, Account account, long barIdx) {
    }

    @Override
    public void onExit(TradeLog tradeLog, Account account, long barIdx) {
    }

    @Override
    public Double getValue() {
        value = stats.getStandardDeviation();
        return value;
    }
}
