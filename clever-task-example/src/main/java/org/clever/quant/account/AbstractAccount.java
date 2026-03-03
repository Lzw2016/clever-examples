package org.clever.quant.account;

import lombok.Getter;
import org.clever.core.Assert;
import org.clever.quant.Account;
import org.clever.quant.Position;
import org.clever.quant.TradeAccountSnapshot;
import org.clever.quant.TradeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
     * 账户名
     */
    protected final String name;
    /**
     * 初始总金额
     */
    protected final double totalAmount;
    /**
     * 账户余额
     */
    protected volatile double balance;
    /**
     * 持仓状态 {@code Map<code, Position>}
     */
    protected final Map<String, Position> positions = new HashMap<>();
    /**
     * 所有的历史交易日志
     */
    protected final List<TradeLog> tradeLogs = new ArrayList<>();

    /**
     * @param totalAmount 初始总金额
     * @param name        账户名
     */
    public AbstractAccount(double totalAmount, String name) {
        Assert.isTrue(totalAmount > 0, "参数 totalAmount 必须大于等于0");
        this.name = name == null ? getClass().getSimpleName() : name;
        this.totalAmount = totalAmount;
        this.balance = totalAmount;
    }

    @Override
    public double getBalance() {
        return balance;
    }

    @Override
    public List<TradeLog> getTradeLogs() {
        return syncRead(() -> Collections.unmodifiableList(tradeLogs));
    }

    @Override
    public TradeAccountSnapshot getSnapshot() {
        return syncRead(() -> TradeAccountSnapshot.builder()
            .positions(Collections.unmodifiableMap(positions))
            .balance(getBalance())
            .build()
        );
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
}
