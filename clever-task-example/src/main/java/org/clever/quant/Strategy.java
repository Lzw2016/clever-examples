package org.clever.quant;

import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 * 交易策略，计算买卖点
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 14:21 <br/>
 */
public interface Strategy {
    /**
     * 策略名称
     */
    String getName();

    /**
     * 返回与当前策略相关的所有 BarSeries 对象
     */
    default Set<BarSeries> getAllBarSeries() {
        Rule entryRule = getEntryRule();
        Assert.notNull(entryRule, "entryRule 不能为 null");
        Rule exitRule = getExitRule();
        Assert.notNull(exitRule, "exitRule 不能为 null");
        Set<BarSeries> allBarSeries = new HashSet<>();
        allBarSeries.addAll(entryRule.getAllBarSeries());
        allBarSeries.addAll(exitRule.getAllBarSeries());
        return allBarSeries;
    }

    /**
     * 开仓规则
     */
    Rule getEntryRule();

    /**
     * 平仓规则
     */
    Rule getExitRule();

    /**
     * 当前barIdx位置是否应该开仓
     *
     * @param barIdx  Bar的索引位置
     * @param account 交易账户
     */
    default boolean shouldEnter(long barIdx, Account account) {
        Assert.notNull(account, "参数 account 不能为 null");
        Rule entryRule = getEntryRule();
        Assert.notNull(entryRule, "entryRule 不能为 null");
        return entryRule.isSatisfied(barIdx, account);
    }

    /**
     * 当前barIdx位置是否应该平仓
     *
     * @param barIdx  Bar的索引位置
     * @param account 交易账户
     */
    default boolean shouldExit(long barIdx, Account account) {
        Assert.notNull(account, "参数 account 不能为 null");
        Rule exitRule = getExitRule();
        Assert.notNull(exitRule, "exitRule 不能为 null");
        return exitRule.isSatisfied(barIdx, account);
    }
}
