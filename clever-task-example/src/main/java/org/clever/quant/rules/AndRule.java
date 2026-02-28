package org.clever.quant.rules;

import lombok.Getter;
import org.clever.core.Assert;
import org.clever.quant.Rule;
import org.clever.quant.TradeAccountSnapshot;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 14:11 <br/>
 */
@Getter
public class AndRule extends AbstractRule {
    private final Rule rule1;
    private final Rule rule2;

    public AndRule(Rule rule1, Rule rule2) {
        this(rule1, rule2, null);
    }

    public AndRule(Rule rule1, Rule rule2, String name) {
        super(name == null ? String.format("(%s and %s)", rule1.getName(), rule2.getName()) : name);
        Assert.notNull(rule1, "参数 rule1 不能为 null");
        Assert.notNull(rule2, "参数 rule2 不能为 null");
        this.rule1 = rule1;
        this.rule2 = rule2;
    }

    @Override
    public boolean isSatisfied(long barIdx, TradeAccountSnapshot accountSnapshot) {
        return rule1.isSatisfied(barIdx, accountSnapshot) && rule2.isSatisfied(barIdx, accountSnapshot);
    }
}
