package org.clever.quant.archive;

import lombok.Getter;
import org.clever.core.Assert;
import org.clever.core.Conv;
import org.clever.core.id.SnowFlake;
import org.clever.core.reflection.ReflectionsUtils;
import org.clever.quant.*;
import org.clever.quant.archive.entity.*;
import org.clever.quant.criteria.*;

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
     * 第一个 Bar 数据
     */
    private final Map<BarSeries, Bar> firstBars = new HashMap<>();
    /**
     * 最后一个 Bar 数据
     */
    private final Map<BarSeries, Bar> lastBars = new HashMap<>();
    // 计算指标
    private final HistoryMinTotalAssetsCriterion historyMinTotalAssets = new HistoryMinTotalAssetsCriterion();
    private final ProfitAmountCriterion profitAmount = new ProfitAmountCriterion();
    private final TotalFeeCriterion totalFee = new TotalFeeCriterion();
    private final AvgAnnualReturnRateCriterion avgAnnualReturnRate = new AvgAnnualReturnRateCriterion();
    private final ReturnVolatilityCriterion returnVolatility = new ReturnVolatilityCriterion();
    private final HistoryMaxDrawdownCriterion maxDrawdown = new HistoryMaxDrawdownCriterion();
    private final LossStdDevCriterion lossStdDev = new LossStdDevCriterion();
    private final MaxConsecutiveLossDaysCriterion maxConsecutiveLossDays = new MaxConsecutiveLossDaysCriterion();
    private final TotalTradeCountCriterion totalTradeCount = new TotalTradeCountCriterion();
    private final ProfitTradeCountCriterion profitTradeCount = new ProfitTradeCountCriterion();
    private final HoldingTimeP90Criterion holdingTimeP90 = new HoldingTimeP90Criterion();
    private final MaxConsecutiveProfitCountCriterion maxConsecutiveProfitCount = new MaxConsecutiveProfitCountCriterion();
    private final MaxConsecutiveLossCountCriterion maxConsecutiveLossCount = new MaxConsecutiveLossCountCriterion();

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
        historyMinTotalAssets.startCalc(trader);
        profitAmount.startCalc(trader);
        totalFee.startCalc(trader);
        avgAnnualReturnRate.startCalc(trader);
        returnVolatility.startCalc(trader);
        maxDrawdown.startCalc(trader);
        lossStdDev.startCalc(trader);
        maxConsecutiveLossDays.startCalc(trader);
        totalTradeCount.startCalc(trader);
        profitTradeCount.startCalc(trader);
        holdingTimeP90.startCalc(trader);
        maxConsecutiveProfitCount.startCalc(trader);
        maxConsecutiveLossCount.startCalc(trader);
        saveData(backtestRecord);
        saveData(backtestBarSeries);
    }

    @Override
    public synchronized void end() {
        // 更新 backtestRecord
        updateBacktestRecord(backtestRecord, account, priceTable);
        saveData(backtestRecord);
        // 更新 backtestBarSeries
        Set<BarSeries> allBarSeries = new HashSet<>(auxBarSeries);
        allBarSeries.add(mainBarSeries);
        for (BacktestBarSeries item : backtestBarSeries) {
            BarSeries barSeries = allBarSeries.stream()
                .filter(itm -> Objects.equals(item.getId(), itm.getId()))
                .findFirst().orElse(null);
            if (barSeries == null) {
                continue;
            }
            updateBacktestBarSeries(item, barSeries, firstBars.get(barSeries), lastBars.get(barSeries));
        }
        saveData(backtestBarSeries);
    }

    @Override
    public synchronized void onBars(Bar mainBar, Map<BarSeries, Bar> bars, long barIdx) {
        if (firstBars.isEmpty()) {
            firstBars.putAll(bars);
            firstBars.put(mainBarSeries, mainBar);
        }
        lastBars.clear();
        lastBars.putAll(bars);
        lastBars.put(mainBarSeries, mainBar);
        final Set<Bar> allBar = new HashSet<>(bars.values());
        allBar.add(mainBar);
        for (Bar bar : allBar) {
            priceTable.put(bar.getCode(), bar.getClose(AdjustType.none));
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
        backtestBarSeries.setSource(Conv.asString(barSeries.getExtData(BarSeries.EXT_SOURCE), null));
        backtestBarSeries.setTableName(Conv.asString(barSeries.getExtData(BarSeries.EXT_TABLE_NAME), null));
        backtestBarSeries.setCode(barSeries.getCode());
        backtestBarSeries.setName(barSeries.getName());
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
        backtestBar.setOpen(Conv.asDecimal(bar.getOpen(AdjustType.front)));
        backtestBar.setHigh(Conv.asDecimal(bar.getHigh(AdjustType.front)));
        backtestBar.setLow(Conv.asDecimal(bar.getHigh(AdjustType.front)));
        backtestBar.setClose(Conv.asDecimal(bar.getClose(AdjustType.front)));
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
        double totalAssets = account.getTotalAssets(priceTable);
        backtestRecord.setFinalTotalAssets(Conv.asDecimal(totalAssets));
        backtestRecord.setMinTotalAssets(Conv.asDecimal(historyMinTotalAssets.getValue(), null));
        backtestRecord.setProfitAmount(Conv.asDecimal(profitAmount.getValue(), null));
        backtestRecord.setTotalFee(Conv.asDecimal(totalFee.getValue(), null));
        if (profitAmount.getValue() > 0) {
            backtestRecord.setFeeRatio(Conv.asDecimal(totalFee.getValue() / profitAmount.getValue() * 100));
        }
        backtestRecord.setCumulativeReturnRate(Conv.asDecimal((totalAssets - account.getInitAmount()) / account.getInitAmount() * 100));
        backtestRecord.setAvgAnnualReturnRate(Conv.asDecimal(avgAnnualReturnRate.getValue(), null));
        backtestRecord.setReturnVolatility(Conv.asDecimal(returnVolatility.getValue(), null));
        backtestRecord.setMaxDrawdown(Conv.asDecimal(maxDrawdown.getValue(), null));
        backtestRecord.setLossStdDev(Conv.asDecimal(lossStdDev.getValue(), null));
        backtestRecord.setMaxConsecutiveLossDays(maxConsecutiveLossDays.getValue());
        backtestRecord.setTotalTradeCount(totalTradeCount.getValue());
        backtestRecord.setProfitTradeCount(profitTradeCount.getValue());
        if (totalTradeCount.getValue() > 0) {
            backtestRecord.setWinRate(Conv.asDecimal(profitTradeCount.getValue() * 1.0 / totalTradeCount.getValue() * 100.0));
        }
        backtestRecord.setHoldingTimeP90(Conv.asDecimal(holdingTimeP90.getValue(), null));
        backtestRecord.setMaxConsecutiveProfitCount(maxConsecutiveProfitCount.getValue());
        backtestRecord.setMaxConsecutiveLossCount(maxConsecutiveLossCount.getValue());
    }

    protected void updateBacktestBarSeries(BacktestBarSeries backtestBarSeries, BarSeries barSeries, Bar firstBar, Bar lastBar) {
        backtestBarSeries.setCode(barSeries.getCode());
        backtestBarSeries.setCount(barSeries.getCount());
        if (firstBar != null) {
            backtestBarSeries.setStartTime(firstBar.getTime());
            backtestBarSeries.setPeriod(firstBar.getPeriod().getName());
        }
        if (lastBar != null) {
            backtestBarSeries.setStartTime(lastBar.getTime());
        }
    }
}
