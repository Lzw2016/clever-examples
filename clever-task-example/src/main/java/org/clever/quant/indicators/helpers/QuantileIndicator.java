package org.clever.quant.indicators.helpers;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.clever.core.Assert;
import org.clever.quant.Bar;
import org.clever.quant.Indicator;
import org.clever.quant.indicators.AbstractIndicator;

/**
 * 计算指定指标的分位数
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/11 21:38 <br/>
 */
public class QuantileIndicator extends AbstractIndicator<Double> {
    private final int barCount;
    private final Indicator<? extends Number> indicator;
    private final Percentile percentile;

    public QuantileIndicator(Indicator<? extends Number> indicator, int barCount, double quantile) {
        this(indicator, barCount, quantile, String.format("%s个%s的P%s", barCount, indicator.getName(), quantile));
    }

    /**
     * @param indicator 目标指标
     * @param barCount  bar数量
     * @param quantile  分位数，1-100
     * @param name      指标名称
     */
    public QuantileIndicator(Indicator<? extends Number> indicator, int barCount, double quantile, String name) {
        super(indicator.getBarSeries(), barCount, name);
        Assert.isTrue(barCount > 0, "参数 barCount 必须大于0");
        Assert.isTrue(quantile >= 0 && quantile <= 100, "参数 barCount 必须在 0 ~ 100 范围内");
        this.indicator = indicator;
        this.barCount = barCount;
        this.percentile = new Percentile(quantile);
    }

    @Override
    protected Double calculate(Bar bar, long barIdx) {
        double[] values = indicator.getValues(barIdx, barCount).stream().mapToDouble(Number::doubleValue).toArray();
        return percentile.evaluate(values);
    }
}
