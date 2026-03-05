package org.clever.quant;

import org.clever.quant.archive.entity.BacktestBarSeries;
import org.clever.quant.archive.entity.BacktestRecord;

import java.util.List;
import java.util.Set;

/**
 * 归档回测数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/05 11:49 <br/>
 */
public interface BacktestArchiver {
    /**
     * 交易账户
     */
    Account getAccount();

    /**
     * 交易的目标 BarSeries
     */
    BarSeries getMainBarSeries();

    /**
     * 辅助 BarSeries
     */
    Set<BarSeries> getAuxBarSeries();

    /**
     * 行情Bar的指标
     */
    List<Indicator<?>> getIndicators();

    /**
     * 交易规则
     */
    List<Rule> getRules();

    /**
     * 交易策略
     */
    List<Strategy> getStrategies();

    /**
     * 回测记录
     */
    BacktestRecord getBacktestRecord();

    /**
     * 回测bar序列配置
     */
    List<BacktestBarSeries> getBacktestBarSeries();

    /**
     * 开始监听BarSeries数据变化, 并归档保存数据(只能start一次)
     *
     * @param trader 交易对象
     */
    void start(Trader trader);

    /**
     * 回测结束
     */
    void end();
}
