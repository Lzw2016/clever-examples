package org.clever.quant.account;

import lombok.Getter;
import org.clever.core.Assert;
import org.clever.core.DateUtils;
import org.clever.quant.*;
import org.clever.quant.utils.TradeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 20:17 <br/>
 */
@Getter
public abstract class AbstractAccount implements Account {
    protected final transient Logger log = LoggerFactory.getLogger(getClass());
    /**
     * 账户操作锁
     */
    protected final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    /**
     * 账户名称
     */
    protected final String name;
    /**
     * 初始资金
     */
    protected final double initAmount;
    /**
     * 账户余额
     */
    protected volatile double balance;
    /**
     * 持仓状态 {@code Map<code, Position>}
     */
    protected final Map<String, Position> positions = new ConcurrentHashMap<>();
    /**
     * 除权除息日志
     */
    protected final List<DividendLog> dividendLogs = new ArrayList<>();
    /**
     * 所有的历史交易日志
     */
    protected final List<TradeLog> tradeLogs = new ArrayList<>();

    /**
     * @param initAmount 初始资金
     * @param name       账户名
     */
    public AbstractAccount(double initAmount, String name) {
        Assert.isTrue(initAmount > 0, "参数 initAmount 必须大于等于0");
        this.name = name == null ? getClass().getSimpleName() : name;
        this.initAmount = initAmount;
        this.balance = initAmount;
    }

    @Override
    public double getBalance() {
        return syncRead(() -> balance);
    }

    @Override
    public List<TradeLog> getTradeLogs() {
        return syncRead(() -> Collections.unmodifiableList(tradeLogs));
    }

    @Override
    public List<TradeLog> getTradeLogs(String code) {
        Assert.isNotBlank(code, "参数 code 不能为空");
        return syncRead(() -> tradeLogs.stream().filter(tradeLog -> Objects.equals(tradeLog.getCode(), code)).toList());
    }

    @Override
    public Position getPosition(String code) {
        Assert.isNotBlank(code, "参数 code 不能为空");
        return syncRead(() -> positions.get(code));
    }

    @Override
    public TradeAccountSnapshot getSnapshot() {
        return syncRead(() -> TradeAccountSnapshot.builder()
            .name(name)
            .positions(Collections.unmodifiableMap(positions))
            .balance(getBalance())
            .build()
        );
    }

    @Override
    public void processDividend(Dividend dividend, Bar bar, long barIdx) {
        Position position = positions.get(dividend.getCode());
        if (position == null) {
            return;
        }
        syncWrite(() -> {
            // 计算红利税
            // 持股 ≤ 1个月	20%	应纳税额 = 送股数量 × 1元 × 20%
            // 1个月 < 持股 ≤ 1年	10%	应纳税额 = 送股数量 × 1元 × 10%
            // 持股 > 1年	免税	应纳税额 = 0
            final int holdDays = DateUtils.pastDays(position.getFirstEntryTime(), bar.getTime());
            double dividendTaxRate;
            if (holdDays > 365) {
                dividendTaxRate = 0;
            } else if (holdDays > 30) {
                dividendTaxRate = 0.1;
            } else {
                dividendTaxRate = 0.2;
            }
            // 现金分红金额
            double cashAmount = 0;
            // 现金分红交税金额
            double cashTax = 0;
            // 送股交税
            double bonusTax = 0;
            // 计算新的持仓量
            int newVolume = position.getVolume();
            // 现金分红(征税)
            if (dividend.getCashDividendPerShare() > 0) {
                cashAmount = position.getVolume() * dividend.getCashDividendPerShare();
                cashTax = cashAmount * dividendTaxRate;
            }
            // 处理送股(征税)
            if (dividend.getBonusRatio() > 0) {
                int addVolume = (int) Math.round(position.getVolume() * dividend.getBonusRatio());
                newVolume = addVolume + position.getVolume();
                bonusTax = addVolume * bar.getClose(AdjustType.none) * dividendTaxRate;
            }
            // 处理转股(免税)
            if (dividend.getTransferRatio() > 0) {
                int addVolume = (int) Math.round(position.getVolume() * dividend.getTransferRatio());
                newVolume = addVolume + position.getVolume();
            }
            // 处理配股
            // if (dividend.getRationRatio() > 0 && dividend.getRationPrice() > 0) {
            //     // TODO 暂时不处理配股
            // }
            // 计算新的可用量
            final int newAvailableVolume = position.getAvailableVolume() + (newVolume - position.getVolume());
            // 计算新的总成本
            final double addBalance = cashAmount - cashTax - bonusTax;
            final double newAmount = position.getVolume() * position.getAvgCostPrice() - addBalance;
            // 计算新的平均成本价
            double newAvgCostPrice = newAmount / newVolume;
            // 更新余额
            final double balance = this.balance;
            this.balance = balance + addBalance;
            // 更新持仓信息
            position.updateForDividend(newVolume, newAvailableVolume, newAvgCostPrice);
            // 分红配送日志
            DividendLog dividendLog = new DividendLog(
                bar.getCode(),
                barIdx,
                dividend.getExDate(),
                dividend.getBonusRatio(),
                dividend.getTransferRatio(),
                dividend.getCashDividendPerShare(),
                dividend.getRationRatio(),
                dividend.getRationPrice(),
                cashTax + bonusTax
            );
            dividendLogs.add(dividendLog);
            return null;
        });
    }

    @Override
    public double getTotalAssets(Map<String, Double> priceTable) {
        return getSnapshot().getTotalAssets(priceTable);
    }

    /**
     * 同步多次读取
     */
    public <T> T syncRead(Supplier<T> sync) {
        Assert.notNull(sync, "参数 sync 不能为 null");
        lock.readLock().lock();
        try {
            return sync.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 同步多次写数据
     */
    public <T> T syncWrite(Supplier<T> sync) {
        Assert.notNull(sync, "参数 sync 不能为 null");
        lock.writeLock().lock();
        try {
            return sync.get();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 增持
     *
     * @param barSeries 行情Bar的时间序列数据
     * @param bar       Bar数据
     * @param price     撮合成交价
     * @param volume    增加的持仓量
     * @param fee       手续费(交易成本)
     */
    protected void increase(BarSeries barSeries, Bar bar, double price, int volume, double fee) {
        syncWrite(() -> {
            // 扣减余额
            double amount = volume * price;
            double balance = this.balance;
            this.balance = balance - amount - fee;
            Assert.isTrue(this.balance >= 0, String.format("开仓之后 balance 不能小于 0, balance=%s", String.format("%.4f", this.balance)));
            // 更新持仓状态
            Position position = positions.computeIfAbsent(bar.getCode(), code -> new Position(code, bar.getTime()));
            position.increase(volume, price, fee, TradeUtils.calcUnlockTime(barSeries, bar));
            return null;
        });
    }

    /**
     * 减持
     *
     * @param code   金融产品编码
     * @param price  撮合成交价
     * @param volume 减少的持仓量
     * @param fee    手续费(交易成本)
     */
    protected void decrease(String code, double price, int volume, double fee) {
        syncWrite(() -> {
            //  增加余额
            double amount = volume * price;
            double balance = this.balance;
            this.balance = balance + amount - fee;
            // 更新持仓状态
            Position position = positions.get(code);
            Assert.notNull(position, String.format("未持仓当前品种“%s”无法平仓", code));
            int positionVolume = position.decrease(volume, price, fee);
            if (positionVolume <= 0) {
                positions.remove(code);
            }
            return null;
        });
    }
}
