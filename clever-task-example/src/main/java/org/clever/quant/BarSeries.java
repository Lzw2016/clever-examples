package org.clever.quant;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.clever.core.Assert;
import org.clever.core.Conv;
import org.clever.core.DateUtils;
import org.clever.core.RingBuffer;
import org.clever.core.id.SnowFlake;
import org.clever.quant.utils.RingBufferUtils;

import java.time.LocalTime;
import java.util.*;

/**
 * 行情Bar的时间序列数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 13:02 <br/>
 */
@Slf4j
public class BarSeries extends AbstractExtData {
    private static final int MIN_SLIDING_WINDOW = 8;
    private static final int DEF_SLIDING_WINDOW = 5120;
    public static final String EXT_SOURCE = "source";
    public static final String EXT_TABLE_NAME = "tableName";
    /**
     * 最小持有天数
     */
    public static final String MIN_HOLDING_DAYS = "minHoldingDays";
    /**
     * 开盘日开始交易时间
     */
    public static final String TRADING_START_TIME = "tradingStartTime";
    /**
     * BarSeries 实例id
     */
    @Getter
    private final long id = SnowFlake.SNOW_FLAKE.nextId();
    /**
     * BarSeries 名称
     */
    @Getter
    private final String name;
    /**
     * 存储 Bar 的环形缓冲区
     */
    private final RingBuffer<Bar> buffer;
    /**
     * 所有的“分红配送”数据
     */
    private final LinkedList<Dividend> dividends;
    /**
     * Bar 数据监听器列表
     */
    private final List<BarListener> listeners = new ArrayList<>();
    /**
     * 最后一个 Bar
     */
    private volatile Bar lastBar;

    /**
     * @param dividends 历史“分红配送”数据
     */
    public BarSeries(List<Dividend> dividends) {
        this(dividends, DEF_SLIDING_WINDOW, null);
    }

    /**
     * @param dividends     历史“分红配送”数据
     * @param slidingWindow 存储Bar的滑动窗口大小
     */
    public BarSeries(List<Dividend> dividends, int slidingWindow) {
        this(dividends, slidingWindow, null);
    }

    /**
     * @param dividends     历史“分红配送”数据
     * @param slidingWindow 存储Bar的滑动窗口大小
     * @param name          BarSeries 名称
     */
    public BarSeries(List<Dividend> dividends, int slidingWindow, String name) {
        Assert.notNull(dividends, "参数 dividends 不能为空");
        Assert.isTrue(slidingWindow >= MIN_SLIDING_WINDOW, String.format("参数 slidingWindow 必须大于等于 %s", MIN_SLIDING_WINDOW));
        this.name = name == null ? getClass().getSimpleName() : name;
        this.buffer = new RingBuffer<>(slidingWindow);
        dividends.sort(Comparator.comparing(Dividend::getExDate));
        this.dividends = new LinkedList<>(dividends);
    }

    /**
     * 获取存储Bar的滑动窗口大小(只存储滑动窗口内的Bar, 滑动窗口之外的Bar直接丢弃)
     */
    public int getSlidingWindow() {
        return buffer.getBufferSize();
    }

    /**
     * BarSeries 对应的金融产品编码
     */
    public String getCode() {
        if (lastBar == null) {
            return null;
        }
        return lastBar.getCode();
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
     * 从指定索引位置开始向前获取 Bar 数据(包含{@code lastBarIdx}位置数据)
     *
     * @param lastBarIdx 指定索引位置
     * @param size       向前获取的数据量
     * @return 返回指定数据集合(数据量可能不够)
     */
    public List<Bar> getBars(long lastBarIdx, int size) {
        Assert.isTrue(lastBarIdx >= 0, "参数 lastBarIdx 必须大于等于 0");
        Assert.isTrue(size > 0, "参数 size 必须大于等于 0");
        return RingBufferUtils.getContent(buffer, lastBarIdx, size).getContent();
    }

    /**
     * 获取 Bar 的总数量
     */
    public long getCount() {
        return buffer.totalCount();
    }

    /**
     * 最小持有天数, 默认: 1 (T + 1)
     */
    public int getMinHoldingDays() {
        final Integer defDays = 1;
        return Conv.asInteger(getExtData(MIN_HOLDING_DAYS), defDays);
    }

    /**
     * 最小持有天数
     */
    public void setMinHoldingDays(int minHoldingDays) {
        Assert.isTrue(minHoldingDays >= 0, "参数 minHoldingDays 必须大于等于 0");
        addExtData(MIN_HOLDING_DAYS, minHoldingDays);
    }

    /**
     * 开盘日开始交易时间, 如: 09:30:00
     */
    public String getTradingStartTime() {
        final String defTime = "09:30:00";
        String tradingStartTime = Conv.asString(getExtData(TRADING_START_TIME), defTime);
        try {
            LocalTime.parse(tradingStartTime);
            return tradingStartTime;
        } catch (Exception e) {
            return defTime;
        }
    }

    /**
     * 开盘日开始交易时间
     *
     * @param tradingStartTime 时间, 如: 09:30:00
     */
    public void setTradingStartTime(String tradingStartTime) {
        Assert.isNotBlank(tradingStartTime, "参数 tradingStartTime 不能为空");
        try {
            LocalTime.parse(tradingStartTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("参数 tradingStartTime 格式错误, 必须是 09:30:00 格式");
        }
        addExtData(TRADING_START_TIME, tradingStartTime);
    }

    /**
     * 在末尾追加一个 Bar
     */
    public void appendBar(Bar bar) {
        Assert.notNull(bar, "参数 bar 不能为 null");
        Assert.isNotBlank(bar.getCode(), "参数 bar.code 不能为 null");
        Assert.isTrue(
            lastBar == null || Objects.equals(lastBar.getCode(), bar.getCode()),
            () -> String.format("参数 bar.code 值必须为 %s", lastBar.getCode())
        );
        Assert.isTrue(
            lastBar == null || Objects.equals(lastBar.getPeriod(), bar.getPeriod()),
            () -> String.format("参数 bar.period 值必须为 %s", lastBar.getPeriod())
        );
        synchronized (buffer) {
            if (lastBar == null) {
                lastBar = bar;
            } else {
                Assert.isTrue(
                    bar.getTime().compareTo(lastBar.getTime()) >= 0,
                    () -> String.format("bar的时间只能在%s之后", DateUtils.formatToString(lastBar.getTime()))
                );
            }
            // 处理“分红转送”
            Dividend dividend = dividends.peek();
            if (dividend != null && Objects.equals(DateUtils.formatToString(bar.getTime(), DateUtils.yyyy_MM_dd), DateUtils.formatToString(dividend.getExDate(), DateUtils.yyyy_MM_dd))) {
                emitDividendEvent(dividend);
                dividends.poll();
            }
            // 新增 bar 驱动交易
            long barIdx = buffer.add(bar, this::emitRemoveBarEvent);
            Assert.isTrue(barIdx >= 0, "追加 Bar 失败");
            lastBar = bar;
            emitAppendBarEvent(bar, barIdx);
        }
    }

    /**
     * 注册 Bar 数据变化事件
     */
    public void registerBarListener(BarListener listener) {
        Assert.notNull(listener, "参数 listener 不能为 null");
        OneTimeBindableBarSeries bindBarSeries = null;
        if (listener instanceof OneTimeBindableBarSeries) {
            bindBarSeries = (OneTimeBindableBarSeries) listener;
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
                // System.exit(-1);
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
                // System.exit(-1);
            }
        }
    }

    /**
     * 触发分红配送(除权除息)事件
     */
    protected void emitDividendEvent(Dividend dividend) {
        for (BarListener listener : listeners) {
            try {
                listener.onDividendEvent(dividend);
            } catch (Exception err) {
                log.error("onDividendEvent事件回调异常, listener={}", listener, err);
                // System.exit(-1);
            }
        }
    }
}
