package org.clever.quant;

/**
 * 只能关联绑定一个 BarListener 的对象<br/>
 * 绑定对应的BarSeries之后，只能与绑定BarSeries对象产生如下关系:<br/>
 * 1. {@link BarSeries#registerBarListener(BarListener)}<br/>
 * 2.
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/28 11:51 <br/>
 */
public interface OneTimeBindableBarSeries {
    /**
     * 绑定 BarListener 对象(只能绑定一次)
     */
    void bind(BarSeries series);

    /**
     * 当前实例绑定的 BarSeries
     *
     * @return 如果未绑定返回 null
     */
    BarSeries getBarSeries();
}
