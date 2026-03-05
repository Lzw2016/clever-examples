package org.clever.quant.indicators.helpers;

import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.indicators.AbstractIndicator;

/**
 * Bar 的volume(成交量)数值数据指标
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/01 11:28 <br/>
 */
public class VolumeIndicator extends AbstractIndicator<Long> {
    private static final String DEF_NAME = "成交量";

    public VolumeIndicator(BarSeries series) {
        super(series, 0, DEF_NAME);
    }

    @Override
    protected Long calculate(Bar bar, long barIdx) {
        return bar.getVolume();
    }
}
