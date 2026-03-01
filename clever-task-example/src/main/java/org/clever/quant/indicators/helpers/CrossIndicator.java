package org.clever.quant.indicators.helpers;

import lombok.Getter;
import org.clever.core.Assert;
import org.clever.quant.Bar;
import org.clever.quant.Indicator;
import org.clever.quant.indicators.AbstractIndicator;

/**
 * 判断指标是否交叉,监视两个指标是否交叉的指标 <br/>
 * <pre>
 *  指标为true:  up下穿low之时(barIdx之前up >= low, barIdx时up < low)
 *  指标为false: 当前up未下穿low
 * </pre>
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/01 13:23 <br/>
 */
@Getter
public class CrossIndicator extends AbstractIndicator<Boolean> {
    /**
     * Upper indicator
     */
    private final Indicator<? extends Number> up;
    /**
     * Lower indicator
     */
    private final Indicator<? extends Number> low;

    /**
     * 指标为true:  up下穿low之时(barIdx之前up >= low, barIdx时up < low)
     *
     * @param up  up指标
     * @param low low指标
     */
    public CrossIndicator(Indicator<? extends Number> up, Indicator<? extends Number> low) {
        super(up.getBarSeries(), Math.max(up.getUnstableBarCount(), low.getUnstableBarCount()));
        Assert.isTrue(up.getBarSeries() == low.getBarSeries(), String.format("指标up=%s与指标low=%s关联的BarSeries不一致", up, low));
        this.up = up;
        this.low = low;
    }

    @Override
    protected Boolean getDefValue() {
        return false;
    }

    @Override
    protected Boolean calculate(Bar bar, long barIdx) {
        Number upVal = up.getValue(barIdx);
        Assert.notNull(upVal, String.format("指标%s在位置%s的值为null", up, barIdx));
        Number lowVal = low.getValue(barIdx);
        Assert.notNull(lowVal, String.format("指标%s在位置%s的值为null", low, barIdx));
        if (upVal.doubleValue() >= lowVal.doubleValue()) {
            return false;
        }
        long idx = barIdx;
        while (true) {
            idx--;
            if (idx <= unstableBarCount) {
                return getValue(idx);
            }
            Number preUpVal = up.getValue(idx);
            Assert.notNull(preUpVal, String.format("指标%s在位置%s的值为null", up, idx));
            Number preLowVal = low.getValue(idx);
            Assert.notNull(preLowVal, String.format("指标%s在位置%s的值为null", low, idx));
            if (preUpVal.equals(preLowVal)) {
                continue;
            }
            return preUpVal.doubleValue() > preLowVal.doubleValue();
        }
    }
}
