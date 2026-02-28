package org.clever.quant;

import java.util.List;

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
     * 从指定索引位置开始向前获取指标数据
     *
     * @param lastBarIdx 指定索引位置
     * @param size       向前获取的数据量
     * @return 返回指定数据集合(数据量可能不够)
     */
    List<T> getValues(long lastBarIdx, int size);

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
