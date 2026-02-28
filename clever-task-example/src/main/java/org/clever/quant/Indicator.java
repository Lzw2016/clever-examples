package org.clever.quant;

/**
 * 行情Bar的指标数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 13:22 <br/>
 */
public interface Indicator<T> extends BarListener, BindBarSeries {
    /**
     * 获取指定 Bar 索引位置的指标数据
     *
     * @param barIdx Bar的索引位置
     * @return 如果无法计算返回 null
     */
    T getValue(long barIdx);

    /**
     * 能计算当前指标需要的最小Bar数量
     */
    int getUnstableBarCount();

    /**
     * 当前指标是否以及稳定可用了(Bar 数量足够计算出指标值了)
     */
    default boolean isStable() {
        return getBarSeries().getCount() >= getUnstableBarCount();
    }
}
