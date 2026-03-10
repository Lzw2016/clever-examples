package org.clever.quant.indicators.helpers;

import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.indicators.AbstractIndicator;

/**
 * Bar 的amount(成交额)数值数据指标
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/01 11:25 <br/>
 */
public class AmountIndicator extends AbstractIndicator<Double> {
    public static final String DEF_NAME = "成交额";

    public AmountIndicator(BarSeries series) {
        super(series, 0, DEF_NAME);
    }

    @Override
    protected Double calculate(Bar bar, long barIdx) {
        return bar.getAmount();
    }
}
