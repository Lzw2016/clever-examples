package org.clever.quant.rules;

import org.clever.core.Assert;
import org.clever.quant.Indicator;
import org.clever.quant.TradeAccountSnapshot;
import org.clever.quant.indicators.helpers.ConstantIndicator;
import org.clever.quant.indicators.helpers.CrossIndicator;

import java.util.Optional;

/**
 * 规则满足: first上穿second之时
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/01 20:03 <br/>
 */
public class CrossedUpIndicatorRule extends AbstractRule {
    /**
     * 判断指标是否交叉,监视两个指标是否交叉的指标
     */
    private final CrossIndicator cross;

    /**
     * 规则满足: "指标"上穿"阈值常量"之时
     *
     * @param indicator 指标
     * @param threshold 阈值常量
     */
    public CrossedUpIndicatorRule(Indicator<? extends Number> indicator, Number threshold) {
        this(indicator, new ConstantIndicator<>(indicator.getBarSeries(), threshold));
    }

    /**
     * 规则满足: first上穿second之时
     *
     * @param first  first指标
     * @param second second指标
     */
    public CrossedUpIndicatorRule(Indicator<? extends Number> first, Indicator<? extends Number> second) {
        Assert.notNull(first, "参数 first 不能为 null");
        Assert.notNull(second, "参数 second 不能为 null");
        this.cross = new CrossIndicator(second, first);
        addBarSeries(first, second);
    }

    @Override
    public boolean isSatisfied(long barIdx, TradeAccountSnapshot accountSnapshot) {
        return Optional.ofNullable(cross.getValue(barIdx)).orElse(false);
    }

    public Indicator<? extends Number> getLow() {
        return cross.getLow();
    }

    public Indicator<? extends Number> getUp() {
        return cross.getUp();
    }
}
