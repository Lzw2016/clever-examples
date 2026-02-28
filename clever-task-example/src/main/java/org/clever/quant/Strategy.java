package org.clever.quant;

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
     * 买入规则(做多)
     */
    Rule getEntryRule();

    /**
     * 卖出规则(做空)
     */
    Rule getExitRule();

    /**
     * 当前barIdx位置是否应该买入(做多)
     *
     * @param barIdx          Bar的索引位置
     * @param accountSnapshot 交易账户快照
     */
    default boolean shouldEnter(long barIdx, TradeAccountSnapshot accountSnapshot) {
        return getEntryRule().isSatisfied(barIdx, accountSnapshot);
    }

    /**
     * 当前barIdx位置是否应该卖出(做空)
     *
     * @param barIdx          Bar的索引位置
     * @param accountSnapshot 交易账户快照
     */
    default boolean shouldExit(long barIdx, TradeAccountSnapshot accountSnapshot) {
        return getExitRule().isSatisfied(barIdx, accountSnapshot);
    }

    Strategy and(Strategy strategy, String name);

    default Strategy and(Strategy strategy) {
        return and(strategy, null);
    }

    Strategy or(Strategy strategy, String name);

    default Strategy or(Strategy strategy) {
        return or(strategy, null);
    }
}
