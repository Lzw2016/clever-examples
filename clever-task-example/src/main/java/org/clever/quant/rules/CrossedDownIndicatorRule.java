package org.clever.quant.rules;

import org.clever.core.Assert;
import org.clever.quant.Account;
import org.clever.quant.Indicator;
import org.clever.quant.indicators.helpers.ConstantIndicator;
import org.clever.quant.indicators.helpers.CrossIndicator;

import java.util.Optional;

/**
 * 规则满足: first下穿second之时
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/01 20:52 <br/>
 */
public class CrossedDownIndicatorRule extends AbstractRule {
    private static final String DEF_NAME = "指标下穿(死叉)";
    /**
     * 判断指标是否交叉,监视两个指标是否交叉的指标
     */
    private final CrossIndicator cross;

    /**
     * 规则满足: "指标"下穿"阈值常量"之时
     *
     * @param indicator 指标
     * @param threshold 阈值常量
     */
    public CrossedDownIndicatorRule(Indicator<? extends Number> indicator, Number threshold) {
        this(indicator, new ConstantIndicator<>(indicator.getBarSeries(), threshold), DEF_NAME);
    }

    /**
     * 规则满足: "指标"下穿"阈值常量"之时
     *
     * @param indicator 指标
     * @param threshold 阈值常量
     * @param name      规则名称
     */
    public CrossedDownIndicatorRule(Indicator<? extends Number> indicator, Number threshold, String name) {
        this(indicator, new ConstantIndicator<>(indicator.getBarSeries(), threshold), name);
    }

    /**
     * 规则满足: first下穿second之时
     *
     * @param first  first指标
     * @param second second指标
     */
    public CrossedDownIndicatorRule(Indicator<? extends Number> first, Indicator<? extends Number> second) {
        this(first, second, DEF_NAME);
    }

    /**
     * 规则满足: first下穿second之时
     *
     * @param first  first指标
     * @param second second指标
     * @param name   规则名称
     */
    public CrossedDownIndicatorRule(Indicator<? extends Number> first, Indicator<? extends Number> second, String name) {
        super(name);
        Assert.notNull(first, "参数 first 不能为 null");
        Assert.notNull(second, "参数 second 不能为 null");
        this.cross = new CrossIndicator(first, second);
        dependencies(first, second);
    }

    @Override
    public boolean isSatisfied(long barIdx, Account account) {
        return Optional.ofNullable(cross.getValue(barIdx)).orElse(false);
    }

    public Indicator<? extends Number> getLow() {
        return cross.getLow();
    }

    public Indicator<? extends Number> getUp() {
        return cross.getUp();
    }
}
