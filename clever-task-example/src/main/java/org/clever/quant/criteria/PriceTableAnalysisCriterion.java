package org.clever.quant.criteria;

import org.clever.quant.Bar;
import org.clever.quant.BarSeries;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/03/10 21:59 <br/>
 */
public abstract class PriceTableAnalysisCriterion<T> extends AbstractAnalysisCriterion<T> {
    /**
     * 资产价格表
     */
    protected final Map<String, Double> priceTable = new ConcurrentHashMap<>();

    @Override
    public void onBars(Bar mainBar, Map<BarSeries, Bar> bars, long barIdx) {
        final Set<Bar> allBar = new HashSet<>(bars.values());
        allBar.add(mainBar);
        for (Bar bar : allBar) {
            priceTable.put(bar.getCode(), bar.getClose());
        }
    }
}
