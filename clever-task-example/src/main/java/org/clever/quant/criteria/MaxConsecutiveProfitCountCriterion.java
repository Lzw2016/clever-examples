package org.clever.quant.criteria;

import org.clever.quant.Account;
import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.TradeLog;

import java.util.Map;

/**
 * TODO 最大连续盈利次数
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/10 23:21 <br/>
 */
public class MaxConsecutiveProfitCountCriterion extends PriceTableAnalysisCriterion<Integer> {
    private volatile int consecutiveProfitCount = 0;

    @Override
    protected Integer getInitValue(Account account) {
        return 0;
    }

    @Override
    public synchronized void onBars(Bar mainBar, Map<BarSeries, Bar> bars, long barIdx) {
        super.onBars(mainBar, bars, barIdx);
        double totalAssets = account.getTotalAssets(priceTable);
        if (totalAssets > account.getInitAmount()) {
            consecutiveProfitCount++;
        } else {
            if (value < consecutiveProfitCount) {
                value = consecutiveProfitCount;
            }
            consecutiveProfitCount = 0;
        }
    }

    @Override
    public void onEnter(TradeLog tradeLog, Account account, long barIdx) {
    }

    @Override
    public void onExit(TradeLog tradeLog, Account account, long barIdx) {
    }
}
