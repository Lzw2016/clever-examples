package org.clever.quant.indicators.helpers;

import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.indicators.AbstractIndicator;

/**
 * Bar 的close(收盘价)数值数据指标
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/01 10:45 <br/>
 */
public class ClosePriceIndicator extends AbstractIndicator<Double> {
    public static final String DEF_NAME = "收盘价";

    public ClosePriceIndicator(BarSeries series) {
        super(series, 0, DEF_NAME);
    }

    @Override
    protected Double calculate(Bar bar, long barIdx) {
        return bar.getClose();
    }
}
