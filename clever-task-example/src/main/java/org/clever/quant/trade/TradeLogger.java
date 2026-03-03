package org.clever.quant.trade;

import lombok.extern.slf4j.Slf4j;
import org.clever.core.DateUtils;
import org.clever.quant.Account;
import org.clever.quant.TradeListener;
import org.clever.quant.TradeLog;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/03/03 13:50 <br/>
 */
@Slf4j
public class TradeLogger implements TradeListener {
    @Override
    public void onEnter(TradeLog tradeLog, Account account) {
        log.info(
            "{} 买入 [{}] 价格:{} 成交量:{} 手续费:{} 成交额:{}",
            DateUtils.formatToString(tradeLog.getTime(), DateUtils.yyyy_MM_dd),
            tradeLog.getCode(),
            String.format("%.4f", tradeLog.getPrice()),
            tradeLog.getVolume(),
            String.format("%.4f", tradeLog.getFee()),
            String.format("%.4f", tradeLog.getAmount())
        );
    }

    @Override
    public void onExit(TradeLog tradeLog, Account account) {
        log.info(
            "{} 卖出 [{}] 价格:{} 成交量:{} 手续费:{} 成交额:{}",
            DateUtils.formatToString(tradeLog.getTime(), DateUtils.yyyy_MM_dd),
            tradeLog.getCode(),
            String.format("%.4f", tradeLog.getPrice()),
            tradeLog.getVolume(),
            String.format("%.4f", tradeLog.getFee()),
            String.format("%.4f", tradeLog.getAmount())
        );
    }
}
