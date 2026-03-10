package org.clever.quant;

/**
 * 计算策略指标接口
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 14:57 <br/>
 */
public interface AnalysisCriterion<T> {
    /**
     * 开始计算策略指标
     *
     * @param trader 交易账户(已经完成了所有交易之后)
     */
    void calculate(Trader trader);

    /**
     * 获取计算值
     */
    T getValue();
}
