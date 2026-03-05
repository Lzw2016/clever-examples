package org.clever.quant.indicators.averages;

import lombok.Getter;
import org.clever.core.Assert;
import org.clever.quant.Bar;
import org.clever.quant.Indicator;
import org.clever.quant.indicators.AbstractIndicator;
import org.clever.quant.indicators.helpers.RollingSumIndicator;

/**
 * 简单移动平均线(SMA)指标
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/01 13:00 <br/>
 */
@Getter
public class SMAIndicator extends AbstractIndicator<Double> {
    private final int barCount;
    private final Indicator<? extends Number> indicator;
    private final RollingSumIndicator previousSum;

    public SMAIndicator(Indicator<? extends Number> indicator, int barCount) {
        this(indicator, barCount, String.format("SMA%s", barCount));
    }

    public SMAIndicator(Indicator<? extends Number> indicator, int barCount, String name) {
        super(indicator.getBarSeries(), barCount, false, name);
        Assert.isTrue(barCount > 0, "参数 barCount 必须大于0");
        this.indicator = indicator;
        this.previousSum = new RollingSumIndicator(indicator, barCount);
        this.barCount = barCount;
        this.series.registerBarListener(this.previousSum);
        this.series.registerBarListener(this);
    }

    @Override
    protected Double calculate(Bar bar, long barIdx) {
        Double sum = previousSum.getValue(barIdx);
        Assert.notNull(sum, String.format("指标%s在位置%s的值为null", indicator, barIdx));
        return sum / barCount;
    }
}
