package org.clever.quant.indicators.helpers;

import org.clever.core.Assert;
import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.Indicator;
import org.clever.quant.series.AbstractOneTimeBindableBarSeries;

import java.util.Collections;
import java.util.List;

/**
 * 常量指标,指标的值是固定的
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/01 20:09 <br/>
 */
public class ConstantIndicator<T> extends AbstractOneTimeBindableBarSeries implements Indicator<T> {
    /**
     * 指标常量值
     */
    private final T value;
    /**
     * 当前最大的 Bar 的索引位置
     */
    private volatile long maxBarIdx;

    /**
     * @param series 指标绑定的 BarSeries
     * @param value  指标常量值
     */
    public ConstantIndicator(BarSeries series, T value) {
        Assert.notNull(series, "参数 series 不能为 null");
        Assert.notNull(value, "参数 value 不能为 null");
        series.registerBarListener(this);
        this.series = series;
        this.value = value;
        this.series.registerBarListener(this);
    }

    @Override
    public T getValue(long barIdx) {
        return value;
    }

    @Override
    public List<T> getValues(long lastBarIdx, int size) {
        lastBarIdx = Math.min(lastBarIdx, maxBarIdx);
        int realSize = size;
        if ((lastBarIdx + 1) < size) {
            realSize = (int) lastBarIdx + 1;
        }
        return Collections.nCopies(realSize, value);
    }

    @Override
    public int getUnstableBarCount() {
        return 0;
    }

    @Override
    public void onAppendBar(Bar bar, long barIdx) {
        if (barIdx > maxBarIdx) {
            maxBarIdx = barIdx;
        }
    }
}
