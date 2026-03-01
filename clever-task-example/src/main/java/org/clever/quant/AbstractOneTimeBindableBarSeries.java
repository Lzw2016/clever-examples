package org.clever.quant;

import org.clever.core.Assert;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/28 13:11 <br/>
 */
public abstract class AbstractOneTimeBindableBarSeries implements OneTimeBindableBarSeries {
    /**
     * 当前绑定的 BarSeries
     */
    protected volatile BarSeries series = null;

    @Override
    public synchronized void bind(BarSeries series) {
        Assert.notNull(series, "参数 series 不能为 null");
        Assert.isTrue(
            this.series == null || this.series == series,
            String.format("当前BindBarSeries已经绑定了BarSeries, series=%s", this.series)
        );
        this.series = series;
    }

    @Override
    public BarSeries getBarSeries() {
        return series;
    }
}
