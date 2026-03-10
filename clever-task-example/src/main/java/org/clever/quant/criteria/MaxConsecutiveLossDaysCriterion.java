package org.clever.quant.criteria;

import org.clever.core.DateUtils;
import org.clever.quant.Account;
import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.TradeLog;

import java.util.Date;
import java.util.Map;

/**
 * 最大连续亏损时间(天)
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/10 22:57 <br/>
 */
public class MaxConsecutiveLossDaysCriterion extends PriceTableAnalysisCriterion<Integer> {
    private volatile Date startTime;

    @Override
    protected Integer getInitValue(Account account) {
        return 0;
    }

    @Override
    public void onBars(Bar mainBar, Map<BarSeries, Bar> bars, long barIdx) {
        super.onBars(mainBar, bars, barIdx);
        double totalAssets = account.getTotalAssets(priceTable);
        if (totalAssets >= account.getInitAmount()) {
            startTime = null;
        } else if (startTime == null) {
            startTime = mainBar.getTime();
        }
        if (startTime != null) {
            int consecutiveLossDays = DateUtils.pastDays(startTime, mainBar.getTime()) + 1;
            if (value < consecutiveLossDays) {
                value = consecutiveLossDays;
            }
        }
    }

    @Override
    public void onEnter(TradeLog tradeLog, Account account, long barIdx) {
    }

    @Override
    public void onExit(TradeLog tradeLog, Account account, long barIdx) {
    }
}
