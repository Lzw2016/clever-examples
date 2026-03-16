package org.clever.quant;

/**
 * BarSeries 中的Bar数据变化监听器
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/28 10:00 <br/>
 */
@FunctionalInterface
public interface BarListener {
    /**
     * 新增Bar
     *
     * @param bar    新增的Bar
     * @param barIdx Bar的索引位置
     */
    void onAppendBar(Bar bar, long barIdx);

    /**
     * 移除Bar
     *
     * @param bar    新增的Bar
     * @param barIdx Bar的索引位置
     */
    default void onRemoveBar(Bar bar, long barIdx) {
    }

    /**
     * 分红配送(除权除息)
     * <p>
     * 注意：该事件会在除权除息日对应的Bar处理完成后触发
     *
     * @param dividend 分红配送数据
     * @param bar      Bar数据
     * @param barIdx   除权除息日期所对应的Bar的索引位置
     */
    default void onDividendEvent(Dividend dividend, Bar bar, long barIdx) {
    }
}
