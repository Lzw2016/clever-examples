package org.clever.quant.account;

import lombok.Getter;
import org.clever.core.Assert;
import org.clever.quant.Account;
import org.clever.quant.Position;
import org.clever.quant.TradeAccountSnapshot;
import org.clever.quant.TradeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 20:17 <br/>
 */
@Getter
public abstract class AbstractAccount implements Account {
    protected final transient Logger log = LoggerFactory.getLogger(getClass());
    /**
     * 账户名
     */
    protected final String name;
    /**
     * 初始总金额
     */
    protected final double totalAmount;
    /**
     * 持仓状态
     */
    private final List<Position> positions = new ArrayList<>();
    /**
     * 所有的历史交易日志
     */
    protected final List<TradeLog> tradeLogs = new ArrayList<>();

    public AbstractAccount(double totalAmount, String name) {
        Assert.isTrue(totalAmount > 0, "参数 totalAmount 必须大于等于0");
        this.name = name == null ? getClass().getSimpleName() : name;
        this.totalAmount = totalAmount;
    }

    @Override
    public double getBalance() {
        return 0;
    }

    @Override
    public List<TradeLog> getTradeLogs() {
        return List.of();
    }

    @Override
    public TradeAccountSnapshot getSnapshot() {
        return null;
    }
}
