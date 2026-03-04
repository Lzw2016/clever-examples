package org.clever.quant.strategy;

import lombok.Getter;
import org.clever.core.Assert;
import org.clever.quant.Rule;
import org.clever.quant.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 14:38 <br/>
 */
@Getter
public class BaseStrategy implements Strategy {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final String name;
    protected final Rule entryRule;
    protected final Rule exitRule;

    public BaseStrategy(Rule entryRule, Rule exitRule) {
        this(entryRule, exitRule, null);
    }

    public BaseStrategy(Rule entryRule, Rule exitRule, String name) {
        Assert.notNull(entryRule, "参数 entryRule 不能为 null");
        Assert.notNull(exitRule, "参数 exitRule 不能为 null");
        this.name = name == null ? getClass().getSimpleName() : name;
        this.entryRule = entryRule;
        this.exitRule = exitRule;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Rule getEntryRule() {
        return entryRule;
    }

    @Override
    public Rule getExitRule() {
        return exitRule;
    }

    @Override
    public Strategy and(Strategy strategy, String name) {
        return new BaseStrategy(
            entryRule.and(strategy.getEntryRule()),
            exitRule.and(strategy.getExitRule()),
            name == null ? String.format("(%s and %s)", this.getName(), strategy.getName()) : name
        );
    }

    @Override
    public Strategy or(Strategy strategy, String name) {
        return new BaseStrategy(
            entryRule.or(strategy.getEntryRule()),
            exitRule.or(strategy.getExitRule()),
            name == null ? String.format("(%s or %s)", this.getName(), strategy.getName()) : name
        );
    }
}
