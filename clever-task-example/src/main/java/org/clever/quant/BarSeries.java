package org.clever.quant;

import lombok.extern.slf4j.Slf4j;
import org.clever.core.Assert;
import org.clever.core.RingBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * “固定金融产品”的行情Bar时间序列数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 13:02 <br/>
 */
@Slf4j
public class BarSeries {
    private static final int MIN_SLIDING_WINDOW = 10240;
    private static final int DEF_SLIDING_WINDOW = 102400;
    /**
     * 存储 Bar 的环形缓冲区
     */
    private final RingBuffer<Bar> buffer;
    private final List<BarListener> listeners = new ArrayList<>();

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
     * 获取 Bar 的总数量
     */
    public long getCount() {
        return buffer.totalCount();
    }

    /**
     * 在末尾追加一个 Bar
     */
    public void appendBar(Bar bar) {
        synchronized (buffer) {
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
        synchronized (listeners) {
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
