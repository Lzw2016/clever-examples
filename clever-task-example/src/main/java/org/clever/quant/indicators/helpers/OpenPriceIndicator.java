package org.clever.quant.indicators.helpers;

import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.indicators.AbstractIndicator;

/**
 * Bar 的open(开盘价)数值数据指标
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/01 11:10 <br/>
 */
public class OpenPriceIndicator extends AbstractIndicator<Double> {
    private static final String DEF_NAME = "开盘价";

    public OpenPriceIndicator(BarSeries series) {
        super(series, 0, DEF_NAME);
    }

    @Override
    protected Double calculate(Bar bar, long barIdx) {
        return bar.getOpen();
    }
}
