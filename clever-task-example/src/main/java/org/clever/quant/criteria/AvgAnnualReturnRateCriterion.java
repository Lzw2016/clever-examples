package org.clever.quant.criteria;

import org.clever.core.DateUtils;
import org.clever.quant.Account;
import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.TradeLog;

import java.util.Map;

/**
 * 平均年化收益率
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/10 22:17 <br/>
 */
public class AvgAnnualReturnRateCriterion extends PriceTableAnalysisCriterion<Double> {
    private volatile Bar firstBar;
    private volatile Bar lastBar;

    @Override
    protected Double getInitValue(Account account) {
        return 0.0;
    }

    @Override
    public void onBars(Bar mainBar, Map<BarSeries, Bar> bars, long barIdx) {
        super.onBars(mainBar, bars, barIdx);
        if (firstBar == null) {
            firstBar = mainBar;
        }
        lastBar = mainBar;
    }

    @Override
    public void onEnter(TradeLog tradeLog, Account account, long barIdx) {
    }

    @Override
    public void onExit(TradeLog tradeLog, Account account, long barIdx) {
    }

    @Override
    public Double getValue() {
        if (firstBar == null) {
            return value;
        }
        double totalAssets = account.getTotalAssets(priceTable);
        double returnRate = (totalAssets - account.getInitAmount()) / account.getInitAmount() * 100;
        int years = DateUtils.pastYears(firstBar.getTime(), lastBar.getTime()) + 1;
        value = returnRate / years;
        return value;
    }
}
