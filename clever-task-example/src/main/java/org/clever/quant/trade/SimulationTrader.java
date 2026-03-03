package org.clever.quant.trade;

import org.clever.quant.*;

/**
 * 模拟交易
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/03 10:20 <br/>
 */
public class SimulationTrader extends AbstractTrader {
    /**
     * @param account          交易账户
     * @param strategy         交易策略
     * @param positionStrategy 持仓策略
     * @param tradeFeeStrategy 交易手续费计算策略
     */
    public SimulationTrader(Account account, Strategy strategy, PositionStrategy positionStrategy, TradeFeeStrategy tradeFeeStrategy) {
        super(account, strategy, positionStrategy, tradeFeeStrategy);
    }

    @Override
    protected boolean isLiveTrading() {
        return false;
    }

    @Override
    protected boolean isMarketOpen(String code, Bar bar) {
        return false;
    }

    @Override
    protected double calcEnterPrice(Bar bar, long barIdx) {
        return bar.getOpen();
    }

    @Override
    protected double calcExitPrice(Bar bar, long barIdx) {
        return bar.getOpen();
    }
}
