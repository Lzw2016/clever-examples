package org.clever.quant;

import org.clever.quant.rules.AndRule;
import org.clever.quant.rules.NotRule;
import org.clever.quant.rules.OrRule;

/**
 * 交易规则
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 13:58 <br/>
 */
public interface Rule {
    /**
     * 规则名称
     */
    String getName();

    /**
     * 是否满足当前规则
     *
     * @param barIdx          Bar的索引位置
     * @param accountSnapshot 交易账户快照
     * @return 如果瞒住当前规则返回 true
     */
    boolean isSatisfied(long barIdx, TradeAccountSnapshot accountSnapshot);

    default Rule and(Rule rule, String name) {
        return new AndRule(this, rule, name);
    }

    default Rule and(Rule rule) {
        return and(rule, null);
    }

    default Rule or(Rule rule, String name) {
        return new OrRule(this, rule, name);
    }

    default Rule or(Rule rule) {
        return or(rule, null);
    }

    default Rule not(String name) {
        return new NotRule(this, name);
    }

    default Rule not() {
        return not(null);
    }
}
