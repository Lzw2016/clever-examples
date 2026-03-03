package org.clever.quant.rules;

import lombok.Getter;
import org.clever.core.Assert;
import org.clever.quant.Account;
import org.clever.quant.Rule;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 14:19 <br/>
 */
@Getter
public class NotRule extends AbstractRule {
    private final Rule rule;

    public NotRule(Rule rule) {
        this(rule, null);
    }

    public NotRule(Rule rule, String name) {
        super(name == null ? String.format("not %s", rule.getName()) : name);
        Assert.notNull(rule, "参数 rule 不能为 null");
        this.rule = rule;
        addBarSeries(rule);
    }

    @Override
    public boolean isSatisfied(long index, Account account) {
        return !rule.isSatisfied(index, account);
    }
}
