package org.clever.quant;

import lombok.extern.slf4j.Slf4j;
import org.clever.core.Assert;
import org.clever.core.RingBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 的行情Bar时间序列数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 13:02 <br/>
 */
@Slf4j
public class BarSeries {
    private static final int MIN_SLIDING_WINDOW = 8;
    private static final int DEF_SLIDING_WINDOW = 5120;
    /**
     * 存储 Bar 的环形缓冲区
     */
    private final RingBuffer<Bar> buffer;
    /**
     * Bar 数据监听器列表
     */
    private final List<BarListener> listeners = new ArrayList<>();
    /**
     * 金融产品编码
     */
    private volatile String code = null;

    /**
     * @param slidingWindow 存储Bar的滑动窗口大小
     */
    public BarSeries(int slidingWindow) {
        Assert.isTrue(slidingWindow >= MIN_SLIDING_WINDOW, String.format("参数 slidingWindow 必须大于等于 %s", MIN_SLIDING_WINDOW));
        this.buffer = new RingBuffer<>(slidingWindow);
    }

    public BarSeries() {
        this(DEF_SLIDING_WINDOW);
    }

    /**
     * 获取存储Bar的滑动窗口大小(只存储滑动窗口内的Bar, 滑动窗口之外的Bar直接丢弃)
     */
    public int getSlidingWindow() {
        return buffer.getBufferSize();
    }

    /**
     * 获取指定 Bar
     *
     * @param barIdx Bar的索引位置
     * @return 如果不存在返回 null
     */
    public Bar getBar(long barIdx) {
        RingBuffer.BufferContent<Bar> content = buffer.getBuffer(barIdx, 1);
        List<Bar> bars = content.getContent();
        if (bars.isEmpty()) {
            return null;
        }
        return bars.get(0);
    }

    /**
     * 从指定索引位置开始向前获取 Bar 数据
     *
     * @param lastBarIdx 指定索引位置
     * @param size       向前获取的数据量
     * @return 返回指定数据集合(数据量可能不够)
     */
    public List<Bar> getBars(long lastBarIdx, int size) {
        Assert.isTrue(lastBarIdx > 0, "参数 lastBarIdx 必须大于等于 0");
        Assert.isTrue(size > 0, "参数 size 必须大于等于 0");
        final long startIndex = lastBarIdx - size + 1;
        RingBuffer.BufferContent<Bar> content = buffer.getBuffer(startIndex, size);
        return content.getContent();
    }

    /**
     * 获取 Bar 的总数量
     */
    public long getCount() {
        return buffer.totalCount();
    }

    /**
     * 在末尾追加一个 Bar
     */
    public void appendBar(Bar bar) {
        Assert.notNull(bar, "参数 bar 不能为 null");
        Assert.isNotBlank(bar.getCode(), "参数 bar.code 不能为 null");
        Assert.isTrue(code == null || Objects.equals(code, bar.getCode()), String.format("参数 bar.code 值必须为 %s", code));
        synchronized (buffer) {
            if (code == null) {
                code = bar.getCode();
            }
            long barIdx = buffer.add(bar, this::emitRemoveBarEvent);
            Assert.isTrue(barIdx >= 0, "追加 Bar 失败");
            emitAppendBarEvent(bar, barIdx);
            // TODO ???
        }
    }

    /**
     * 注册 Bar 数据变化事件
     */
    public void registerBarListener(BarListener listener) {
        Assert.notNull(listener, "参数 listener 不能为 null");
        BindBarSeries bindBarSeries = null;
        if (listener instanceof BindBarSeries) {
            bindBarSeries = (BindBarSeries) listener;
            BarSeries series = bindBarSeries.getBarSeries();
            Assert.isTrue(
                series == null || series == this,
                String.format("当前BindBarSeries已经绑定了BarSeries, listener=%s, series=%s", bindBarSeries, series)
            );
        }
        synchronized (listeners) {
            if (bindBarSeries != null) {
                BarSeries series = bindBarSeries.getBarSeries();
                Assert.isTrue(
                    series == null || series == this,
                    String.format("当前BindBarSeries已经绑定了BarSeries, listener=%s, series=%s", bindBarSeries, series)
                );
                bindBarSeries.bind(this);
            }
            boolean exist = listeners.stream().anyMatch(item -> item == listener);
            if (exist) {
                return;
            }
            listeners.add(listener);
        }
    }

    /**
     * 触发新增Bar事件
     */
    protected void emitAppendBarEvent(Bar bar, long barIdx) {
        for (BarListener listener : listeners) {
            try {
                listener.onAppendBar(bar, barIdx);
            } catch (Exception err) {
                log.error("onAppendBar事件回调异常, listener={}", listener, err);
            }
        }
    }

    /**
     * 触发移除Bar事件
     */
    protected void emitRemoveBarEvent(Bar bar, long barIdx) {
        for (BarListener listener : listeners) {
            try {
                listener.onRemoveBar(bar, barIdx);
            } catch (Exception err) {
                log.error("onRemoveBar事件回调异常, listener={}", listener, err);
            }
        }
    }

//    public int getFirst
//    public int getLast

//    public int getStart
//    public int getEnd
}
