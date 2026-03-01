package org.clever.quant.indicators;

import org.clever.core.Assert;
import org.clever.core.RingBuffer;
import org.clever.quant.AbstractBindBarSeries;
import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.Indicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 13:52 <br/>
 */
public abstract class AbstractIndicator<T> extends AbstractBindBarSeries implements Indicator<T> {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * 计算指标需要的最少 Bar 数据数量
     */
    protected final int unstableBarCount;
    /**
     * 指标缓存
     */
    protected final RingBuffer<T> cache;

    public AbstractIndicator(BarSeries series, int unstableBarCount) {
        Assert.notNull(series, "参数 series 不能为 null");
        Assert.isTrue(unstableBarCount >= 0, "参数 unstableBarCount 必须大于等于0");
        series.registerBarListener(this);
        this.series = series;
        this.unstableBarCount = unstableBarCount;
        this.cache = new RingBuffer<>(series.getSlidingWindow());
        this.series.registerBarListener(this);
    }

    /**
     * 从缓存中获取指标值
     *
     * @param barIdx Bar的索引位置
     * @return 不存在返回 null
     */
    protected T getValueFromCache(long barIdx) {
        RingBuffer.BufferContent<T> content = cache.getBuffer(barIdx, 1);
        List<T> list = content.getContent();
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public T getValue(long barIdx) {
        if (!isStable()) {
            return null;
        }
        return getValueFromCache(barIdx);
    }

    @Override
    public List<T> getValues(long lastBarIdx, int size) {
        Assert.isTrue(lastBarIdx > 0, "参数 lastBarIdx 必须大于等于 0");
        Assert.isTrue(size > 0, "参数 size 必须大于等于 0");
        final long startIndex = lastBarIdx - size + 1;
        RingBuffer.BufferContent<T> content = cache.getBuffer(startIndex, size);
        return content.getContent();
    }

    @Override
    public int getUnstableBarCount() {
        return unstableBarCount;
    }

    @Override
    public void onAppendBar(Bar bar, long barIdx) {
        T value = getDefValue();
        if (isStable()) {
            value = calculate(bar, barIdx);
        }
        cache.add(value);
    }

    /**
     * 当指标值未稳定之前(Bar数量不满足最小计算数量)的默认值
     */
    protected T getDefValue() {
        return null;
    }

    /**
     * 计算指标数据
     *
     * @param bar    最新的Bar
     * @param barIdx Bar的最新索引位置
     * @return 指标数据
     */
    protected abstract T calculate(Bar bar, long barIdx);
}
