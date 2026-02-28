package org.clever.quant.indicators;

import org.clever.core.Assert;
import org.clever.quant.AbstractBindBarSeries;
import org.clever.quant.BarSeries;
import org.clever.quant.Indicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 13:52 <br/>
 */
public abstract class AbstractIndicator<T> extends AbstractBindBarSeries implements Indicator<T> {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final int unstableBarCount;

    protected AbstractIndicator(BarSeries series, int unstableBarCount) {
        Assert.notNull(series, "参数 series 不能为 null");
        Assert.isTrue(unstableBarCount >= 0, "参数 unstableBarCount 必须大于等于0");
        series.registerBarListener(this);
        this.series = series;
        this.unstableBarCount = unstableBarCount;
        this.series.registerBarListener(this);
    }

    @Override
    public int getUnstableBarCount() {
        return unstableBarCount;
    }
}
