package org.clever.quant.archive;

import lombok.Getter;
import org.clever.core.Assert;
import org.clever.core.Conv;
import org.clever.core.id.SnowFlake;
import org.clever.core.reflection.ReflectionsUtils;
import org.clever.quant.*;
import org.clever.quant.account.TradeAccountSnapshot;
import org.clever.quant.archive.entity.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/03/05 13:00 <br/>
 */
@Getter
public abstract class AbstractBacktestArchiver implements BacktestArchiver, TradeListener {
    /**
     * 交易账户
     */
    private final Account account;
    /**
     * 交易的目标 BarSeries
     */
    private final BarSeries mainBarSeries;
    /**
     * 辅助 BarSeries
     */
    private final Set<BarSeries> auxBarSeries;
    /**
     * 行情Bar的指标
     */
    private final List<Indicator<?>> indicators;
    /**
     * 交易规则
     */
    private final List<Rule> rules;
    /**
     * 交易策略
     */
    private final List<Strategy> strategies;
    /**
     * 回测记录
     */
    private final BacktestRecord backtestRecord;
    /**
     * 回测bar序列配置
     */
    private final List<BacktestBarSeries> backtestBarSeries;
    /**
     * 资产价格表
     */
    private final Map<String, Double> priceTable = new ConcurrentHashMap<>();
    /**
     * 交易对象
     */
    private volatile Trader trader;

    /**
     * @param name          回测方案名称
     * @param tag           回测标签名
     * @param account       交易账户
     * @param mainBarSeries 交易的目标 BarSeries
     * @param auxBarSeries  辅助 BarSeries
     * @param indicators    行情Bar的指标
     * @param rules         交易规则
     * @param strategies    交易策略
     */
    public AbstractBacktestArchiver(String name,
                                    String tag,
                                    Account account,
                                    BarSeries mainBarSeries,
                                    Set<BarSeries> auxBarSeries,
                                    List<Indicator<?>> indicators,
                                    List<Rule> rules,
                                    List<Strategy> strategies) {
        Assert.isNotBlank(name, "参数 name 不能为空");
        Assert.isNotBlank(tag, "参数 tag 不能为空");
        Assert.notNull(account, "参数 account 不能为 null");
        Assert.notNull(mainBarSeries, "参数 mainBarSeries 不能为 null");
        Assert.notNull(auxBarSeries, "参数 auxBarSeries 不能为 null");
        Assert.notNull(indicators, "参数 indicators 不能为 null");
        Assert.notEmpty(rules, "参数 rules 不能为空集合");
        Assert.notEmpty(strategies, "参数 strategies 不能为空集合");
        auxBarSeries = new HashSet<>(auxBarSeries);
        auxBarSeries.remove(mainBarSeries);
        this.account = account;
        this.mainBarSeries = mainBarSeries;
        this.auxBarSeries = Collections.unmodifiableSet(auxBarSeries);
        this.indicators = Collections.unmodifiableList(indicators);
        this.rules = Collections.unmodifiableList(rules);
        this.strategies = Collections.unmodifiableList(strategies);
        this.backtestRecord = createBacktestRecord(name, tag, account);
        this.backtestBarSeries = this.auxBarSeries.stream()
            .map(barSeries -> createBacktestBarSeries(this.backtestRecord, barSeries, false))
            .collect(Collectors.toList());
        this.backtestBarSeries.add(createBacktestBarSeries(this.backtestRecord, this.mainBarSeries, true));
    }

    @Override
    public synchronized void start(Trader trader) {
        Assert.isTrue(trader.isStarted(), "trader 实例未调用 start");
        Assert.isNull(this.trader, "不能重复调用 start");
        this.trader = trader;
        trader.registerTradeListener(this);
        saveData(backtestRecord);
        saveData(backtestBarSeries);
    }

    @Override
    public synchronized void end() {
        updateBacktestRecord(backtestRecord, account, priceTable);
        saveData(backtestRecord);
        saveData(backtestBarSeries);
    }

    @Override
    public synchronized void onBars(Bar mainBar, Map<BarSeries, Bar> bars, long barIdx) {
        final Set<Bar> allBar = new HashSet<>(bars.values());
        allBar.add(mainBar);
        for (Bar bar : allBar) {
            priceTable.put(bar.getCode(), bar.getClose());
        }
        final Set<BacktestBar> backtestBars = bars.entrySet().stream()
            .map(entry -> createBacktestBar(backtestRecord, entry.getKey(), entry.getValue(), barIdx))
            .collect(Collectors.toSet());
        saveData(backtestBars);
        final Set<BacktestIndicator> backtestIndicators = indicators.stream()
            .map(indicator -> createBacktestIndicator(backtestRecord, indicator.getBarSeries(), indicator, barIdx))
            .collect(Collectors.toSet());
        saveData(backtestIndicators);
        final Set<BacktestRule> backtestRules = rules.stream()
            .map(rule -> createBacktestRule(backtestRecord, account, rule, barIdx))
            .collect(Collectors.toSet());
        saveData(backtestRules);
        final Set<BacktestStrategy> backtestStrategies = strategies.stream()
            .map(strategy -> createBacktestStrategy(backtestRecord, strategy, barIdx))
            .collect(Collectors.toSet());
        saveData(backtestStrategies);
        final TradeAccountSnapshot accountSnapshot = account.getSnapshot();
        final BacktestAccountSnapshot backtestAccountSnapshot = createBacktestAccountSnapshot(backtestRecord, accountSnapshot, priceTable, barIdx);
        saveData(backtestAccountSnapshot);
        final List<BacktestPositions> backtestPositions = accountSnapshot.getPositions().values().stream()
            .map(position -> createBacktestPositions(backtestRecord, position, barIdx))
            .sorted(Comparator.comparing(BacktestPositions::getCode))
            .toList();
        saveData(backtestPositions);
    }

    @Override
    public synchronized void onEnter(TradeLog tradeLog, Account account, long barIdx) {
        final BacktestTradeLog backtestTradeLog = createBacktestTradeLog(backtestRecord, tradeLog, barIdx);
        saveData(backtestTradeLog);
    }

    @Override
    public synchronized void onExit(TradeLog tradeLog, Account account, long barIdx) {
        final BacktestTradeLog backtestTradeLog = createBacktestTradeLog(backtestRecord, tradeLog, barIdx);
        saveData(backtestTradeLog);
    }

    /**
     * 保存单个数据
     */
    protected abstract void saveData(Object data);

    protected void saveData(Collection<?> datas) {
        if (datas == null || datas.isEmpty()) {
            return;
        }
        datas.forEach(this::saveData);
    }

    protected void saveData(Object[] datas) {
        saveData(Arrays.asList(datas));
    }

    protected BacktestRecord createBacktestRecord(String name, String tag, Account account) {
        BacktestRecord backtestRecord = new BacktestRecord();
        backtestRecord.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestRecord.setName(name);
        backtestRecord.setTag(tag);
        backtestRecord.setStartTime(new Date());
        // backtestRecord.setConfig();
        backtestRecord.setInitialCapital(Conv.asDecimal(account.getInitAmount()));
        backtestRecord.setCreateAt(new Date());
        backtestRecord.setDelFlag(0);
        return backtestRecord;
    }

    protected BacktestBarSeries createBacktestBarSeries(BacktestRecord backtestRecord, BarSeries barSeries, boolean main) {
        BacktestBarSeries backtestBarSeries = new BacktestBarSeries();
        backtestBarSeries.setId(barSeries.getId());
        backtestBarSeries.setBacktestRecordId(backtestRecord.getId());
        backtestBarSeries.setMain(main);
        // TODO createBacktestBarSeries
        backtestBarSeries.setSource(null);
        backtestBarSeries.setTableName(null);
        backtestBarSeries.setStartTime(null);
        backtestBarSeries.setEndTime(null);
        backtestBarSeries.setCode(null);
        backtestBarSeries.setName(barSeries.getName());
        backtestBarSeries.setPeriod(null);
        backtestBarSeries.setCount(null);
        backtestBarSeries.setSlidingWindow(barSeries.getSlidingWindow());
        backtestBarSeries.setExtData(barSeries.getExtData());
        backtestBarSeries.setCreateAt(new Date());
        backtestBarSeries.setDelFlag(0);
        return backtestBarSeries;
    }

    protected BacktestBar createBacktestBar(BacktestRecord backtestRecord, BarSeries barSeries, Bar bar, long barIdx) {
        BacktestBar backtestBar = new BacktestBar();
        backtestBar.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestBar.setBacktestRecordId(backtestRecord.getId());
        backtestBar.setBarSeriesId(barSeries.getId());
        backtestBar.setBarIdx(barIdx);
        backtestBar.setTime(bar.getTime());
        backtestBar.setOpen(Conv.asDecimal(bar.getOpen()));
        backtestBar.setHigh(Conv.asDecimal(bar.getHigh()));
        backtestBar.setLow(Conv.asDecimal(bar.getHigh()));
        backtestBar.setClose(Conv.asDecimal(bar.getClose()));
        backtestBar.setVolume(bar.getVolume());
        backtestBar.setAmount(Conv.asDecimal(bar.getAmount()));
        backtestBar.setExtData(bar.getExtData());
        backtestBar.setCreateAt(new Date());
        backtestBar.setDelFlag(0);
        return backtestBar;
    }

    protected BacktestIndicator createBacktestIndicator(BacktestRecord backtestRecord, BarSeries barSeries, Indicator<?> indicator, long barIdx) {
        BacktestIndicator backtestIndicator = new BacktestIndicator();
        backtestIndicator.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestIndicator.setBacktestRecordId(backtestRecord.getId());
        backtestIndicator.setBarSeriesId(barSeries.getId());
        backtestIndicator.setBarIdx(barIdx);
        backtestIndicator.setName(indicator.getName());
        // 使用反射获取指标值类型
        Class<?> clazz = ReflectionsUtils.getClassGenericType(indicator.getClass());
        Object val = indicator.getValue(barIdx);
        if (Number.class.isAssignableFrom(clazz)) {
            backtestIndicator.setValType(Constant.indicator_val_type_1);
            backtestIndicator.setNumberVal(Conv.asDecimal(val, null));
        } else if (Boolean.class.isAssignableFrom(clazz)) {
            backtestIndicator.setValType(Constant.indicator_val_type_2);
            backtestIndicator.setBoolVal(Conv.asBoolean(val, null));
        } else if (CharSequence.class.isAssignableFrom(clazz)) {
            backtestIndicator.setValType(Constant.indicator_val_type_3);
            backtestIndicator.setStringVal(Conv.asString(val, null));
        } else if (!Object.class.equals(clazz)) {
            backtestIndicator.setValType(Constant.indicator_val_type_4);
            backtestIndicator.setObjVal(val);
        } else {
            if (val == null) {
                backtestIndicator.setValType(Constant.indicator_val_type_0);
            } else if (val instanceof Number) {
                backtestIndicator.setValType(Constant.indicator_val_type_1);
                backtestIndicator.setNumberVal(Conv.asDecimal(val, null));
            } else if (val instanceof Boolean) {
                backtestIndicator.setValType(Constant.indicator_val_type_2);
                backtestIndicator.setBoolVal(Conv.asBoolean(val, null));
            } else if (val instanceof CharSequence) {
                backtestIndicator.setValType(Constant.indicator_val_type_3);
                backtestIndicator.setStringVal(Conv.asString(val, null));
            } else {
                backtestIndicator.setValType(Constant.indicator_val_type_4);
                backtestIndicator.setObjVal(val);
            }
        }
        backtestIndicator.setCreateAt(new Date());
        backtestIndicator.setDelFlag(0);
        return backtestIndicator;
    }

    protected BacktestRule createBacktestRule(BacktestRecord backtestRecord, Account account, Rule rule, long barIdx) {
        BacktestRule backtestRule = new BacktestRule();
        backtestRule.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestRule.setBacktestRecordId(backtestRecord.getId());
        backtestRule.setBarSeriesIds(rule.getAllBarSeries().stream().map(BarSeries::getId).toList());
        backtestRule.setBarIdx(barIdx);
        backtestRule.setName(rule.getName());
        backtestRule.setSatisfied(rule.isSatisfied(barIdx, account));
        backtestRule.setCreateAt(new Date());
        backtestRule.setDelFlag(0);
        return backtestRule;
    }

    protected BacktestStrategy createBacktestStrategy(BacktestRecord backtestRecord, Strategy strategy, long barIdx) {
        BacktestStrategy backtestStrategy = new BacktestStrategy();
        backtestStrategy.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestStrategy.setBacktestRecordId(backtestRecord.getId());
        backtestStrategy.setBarSeriesIds(strategy.getAllBarSeries().stream().map(BarSeries::getId).toList());
        backtestStrategy.setEntryRuleId(strategy.getEntryRule().getId());
        backtestStrategy.setExitRuleId(strategy.getExitRule().getId());
        backtestStrategy.setBarIdx(barIdx);
        backtestStrategy.setName(strategy.getName());
        backtestStrategy.setCreateAt(new Date());
        backtestStrategy.setDelFlag(0);
        return backtestStrategy;
    }

    protected BacktestAccountSnapshot createBacktestAccountSnapshot(BacktestRecord backtestRecord, TradeAccountSnapshot accountSnapshot, Map<String, Double> priceTable, long barIdx) {
        BacktestAccountSnapshot backtestAccountSnapshot = new BacktestAccountSnapshot();
        backtestAccountSnapshot.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestAccountSnapshot.setBacktestRecordId(backtestRecord.getId());
        backtestAccountSnapshot.setBarIdx(barIdx);
        backtestAccountSnapshot.setBalance(Conv.asDecimal(accountSnapshot.getBalance()));
        backtestAccountSnapshot.setTotalAssets(Conv.asDecimal(accountSnapshot.getTotalAssets(priceTable)));
        backtestAccountSnapshot.setCreateAt(new Date());
        backtestAccountSnapshot.setDelFlag(0);
        return backtestAccountSnapshot;
    }

    protected BacktestPositions createBacktestPositions(BacktestRecord backtestRecord, Position position, long barIdx) {
        BacktestPositions backtestPositions = new BacktestPositions();
        backtestPositions.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestPositions.setBacktestRecordId(backtestRecord.getId());
        backtestPositions.setBarIdx(barIdx);
        backtestPositions.setCode(position.getCode());
        backtestPositions.setVolume(position.getVolume());
        backtestPositions.setAvailableVolume(position.getAvailableVolume());
        backtestPositions.setAvgCostPrice(Conv.asDecimal(position.getAvgCostPrice()));
        backtestPositions.setCreateAt(new Date());
        backtestPositions.setDelFlag(0);
        return backtestPositions;
    }

    protected BacktestTradeLog createBacktestTradeLog(BacktestRecord backtestRecord, TradeLog tradeLog, long barIdx) {
        BacktestTradeLog backtestTradeLog = new BacktestTradeLog();
        backtestTradeLog.setId(SnowFlake.SNOW_FLAKE.nextId());
        backtestTradeLog.setBacktestRecordId(backtestRecord.getId());
        backtestTradeLog.setBarIdx(barIdx);
        backtestTradeLog.setCode(tradeLog.getCode());
        backtestTradeLog.setTradeType(tradeLog.getTradeType().getName());
        backtestTradeLog.setTime(tradeLog.getTime());
        backtestTradeLog.setPrice(Conv.asDecimal(tradeLog.getPrice()));
        backtestTradeLog.setVolume(tradeLog.getVolume());
        backtestTradeLog.setFee(Conv.asDecimal(tradeLog.getFee()));
        backtestTradeLog.setAmount(Conv.asDecimal(tradeLog.getAmount()));
        backtestTradeLog.setCreateAt(new Date());
        backtestTradeLog.setDelFlag(0);
        return backtestTradeLog;
    }

    protected void updateBacktestRecord(BacktestRecord backtestRecord, Account account, Map<String, Double> priceTable) {
        backtestRecord.setSuccess(true);
        backtestRecord.setEndTime(new Date());
        backtestRecord.setFinalTotalAssets(Conv.asDecimal(account.getTotalAssets(priceTable)));
        backtestRecord.setMinTotalAssets(null);
        backtestRecord.setProfitAmount(null);
        backtestRecord.setTotalFee(null);
        backtestRecord.setFeeRatio(null);
        backtestRecord.setCumulativeReturnRate(null);
        backtestRecord.setAvgAnnualReturnRate(null);
        backtestRecord.setReturnVolatility(null);
        backtestRecord.setMaxDrawdown(null);
        backtestRecord.setLossStdDev(null);
        backtestRecord.setMaxConsecutiveLossDays(null);
        backtestRecord.setTotalTradeCount(null);
        backtestRecord.setProfitTradeCount(null);
        backtestRecord.setWinRate(null);
        backtestRecord.setWinRate(null);
        backtestRecord.setHoldingTimeP90(null);
        backtestRecord.setMaxConsecutiveProfitCount(null);
        backtestRecord.setMaxConsecutiveLossCount(null);
    }
}
