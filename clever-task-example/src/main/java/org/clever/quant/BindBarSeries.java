package org.clever.quant;

/**
 * 只能关联绑定一个 BarListener 的对象
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/28 11:51 <br/>
 */
public interface BindBarSeries {
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
