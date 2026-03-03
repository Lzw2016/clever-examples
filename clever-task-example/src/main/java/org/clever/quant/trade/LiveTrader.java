package org.clever.quant.trade;

import org.clever.quant.*;

/**
 * 实盘交易
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/03 17:17 <br/>
 */
public class LiveTrader extends AbstractTrader {
    /**
     * @param account          交易账户
     * @param strategy         交易策略
     * @param positionStrategy 持仓策略
     * @param tradeFeeStrategy 交易手续费计算策略
     */
    public LiveTrader(Account account, Strategy strategy, PositionStrategy positionStrategy, TradeFeeStrategy tradeFeeStrategy) {
        super(account, strategy, positionStrategy, tradeFeeStrategy);
    }

    @Override
    protected boolean isLiveTrading() {
        return true;
    }

    @Override
    protected boolean isMarketOpen(String code, Bar bar) {
        // TODO ???
        return false;
    }

    @Override
    protected double calcEnterPrice(Bar bar, long barIdx) {
        // TODO calcEnterPrice
        return 0;
    }

    @Override
    protected double calcExitPrice(Bar bar, long barIdx) {
        // TODO calcExitPrice
        return 0;
    }
}
