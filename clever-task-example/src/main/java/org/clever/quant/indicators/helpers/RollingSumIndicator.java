package org.clever.quant.indicators.helpers;

import org.clever.core.Assert;
import org.clever.quant.Bar;
import org.clever.quant.Indicator;
import org.clever.quant.indicators.AbstractIndicator;

import java.util.List;

/**
 * 计算指定指标的前n个周期内值的和
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/01 12:17 <br/>
 */
public class RollingSumIndicator extends AbstractIndicator<Double> {
    private final Indicator<? extends Number> indicator;
    private final int barCount;

    /**
     * @param indicator 需要求和的指标对象
     * @param barCount  前n个周期内值的合
     */
    public RollingSumIndicator(Indicator<? extends Number> indicator, int barCount) {
        super(indicator.getBarSeries(), barCount, String.format("前%s个周期%s的和", barCount, indicator.getName()));
        Assert.isTrue(barCount > 0, "参数 barCount 必须大于0");
        this.indicator = indicator;
        this.barCount = barCount;
    }

    @Override
    protected Double calculate(Bar bar, long barIdx) {
        Number value = indicator.getValue(barIdx);
        Assert.notNull(value, String.format("指标%s在位置%s的值为null", indicator, barIdx));
        Double preSum = getValue(barIdx - 1);
        if (preSum != null) {
            long overflowBarIdx = barIdx - barCount;
            Number overflowValue = indicator.getValue(overflowBarIdx);
            Assert.notNull(overflowValue, String.format("指标%s在位置%s的值为null", indicator, overflowBarIdx));
            return preSum + value.doubleValue() - overflowValue.doubleValue();
        }
        List<? extends Number> values = indicator.getValues(barIdx, barCount);
        Assert.isTrue(values.size() == barCount, String.format("指标%s获取前%s个值, 只获取到了%s个", indicator, barCount, values.size()));
        return values.stream().mapToDouble(Number::doubleValue).sum();
    }
}
