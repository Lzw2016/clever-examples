package org.clever.quant.account;

import lombok.Getter;
import org.clever.core.Assert;
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

    public void increase(BarSeries barSeries, Bar bar, double price, int volume, double fee) {
        syncWrite(() -> {
            // 扣减余额
            double amount = volume * price;
            double balance = this.balance;
            this.balance = balance - amount - fee;
            Assert.isTrue(this.balance >= 0, String.format("开仓之后 balance 不能小于 0, balance=%s", String.format("%.4f", this.balance)));
            // 更新持仓状态
            Position position = positions.computeIfAbsent(bar.getCode(), Position::new);
            position.increase(volume, price, fee, TradeUtils.calcUnlockTime(barSeries, bar));
            return null;
        });
    }

    /**
     * 减持
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
