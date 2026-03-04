package org.clever.quant.utils;

import org.clever.core.Assert;
import org.clever.core.DateUtils;
import org.clever.quant.*;

import java.util.Date;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/03/03 13:54 <br/>
 */
public class TradeUtils {
    /**
     * 计算出能完成交易的开仓量
     *
     * @param positionStrategy 持仓策略
     * @param tradeFeeStrategy 交易手续费计算策略
     * @param volumeStep       交易量的最小粒度
     * @param account          交易账户
     * @param price            成交价
     * @param barSeries        BarSeries
     * @param bar              Bar数据
     * @param barIdx           Bar的索引位置
     * @return 返回交易量, 如果不交易就返回 null
     */
    public static Integer calcEnterVolume(PositionStrategy positionStrategy,
                                          TradeFeeStrategy tradeFeeStrategy,
                                          int volumeStep,
                                          Account account,
                                          double price,
                                          BarSeries barSeries,
                                          Bar bar,
                                          long barIdx) {
        Assert.notNull(positionStrategy, "参数 positionStrategy 不能为 null");
        Assert.notNull(tradeFeeStrategy, "参数 tradeFeeStrategy 不能为 null");
        Assert.isTrue(volumeStep > 0, "参数 volumeStep 必须大于 0");
        Assert.notNull(account, "参数 account 不能为 null");
        Assert.isTrue(price > 0, "参数 price 必须大于 0");
        Assert.notNull(barSeries, "参数 barSeries 不能为 null");
        Assert.notNull(bar, "参数 bar 不能为 null");
        Assert.isTrue(barIdx >= 0, "参数 barIdx 必须大于等于 0");
        Integer volume = positionStrategy.calcEnterVolume(account, price, barSeries, bar, barIdx);
        if (volume == null) {
            return null;
        }
        volume = volume - (volume % volumeStep);
        while (true) {
            if (volume <= 0) {
                return null;
            }
            double balance = account.getBalance();
            double fee = tradeFeeStrategy.calcEnterFee(price, volume);
            if ((balance - (volume * price + fee)) >= 0) {
                return volume;
            }
            volume = volume - volumeStep;
        }
    }

    /**
     * 计算出能完成交易的平仓量
     *
     * @param positionStrategy 持仓策略
     * @param tradeFeeStrategy 交易手续费计算策略
     * @param account          交易账户
     * @param price            成交价
     * @param barSeries        BarSeries
     * @param bar              Bar数据
     * @param barIdx           Bar的索引位置
     * @return 返回交易量, 如果不交易就返回 null
     */
    public static Integer calcExitVolume(PositionStrategy positionStrategy,
                                         TradeFeeStrategy tradeFeeStrategy,
                                         Account account,
                                         double price,
                                         BarSeries barSeries,
                                         Bar bar,
                                         long barIdx) {
        Assert.notNull(positionStrategy, "参数 positionStrategy 不能为 null");
        Assert.notNull(tradeFeeStrategy, "参数 tradeFeeStrategy 不能为 null");
        Assert.notNull(account, "参数 account 不能为 null");
        Assert.isTrue(price > 0, "参数 price 必须大于 0");
        Assert.notNull(barSeries, "参数 barSeries 不能为 null");
        Assert.notNull(bar, "参数 bar 不能为 null");
        Assert.isTrue(barIdx >= 0, "参数 barIdx 必须大于等于 0");
        Integer volume = positionStrategy.calcExitVolume(account, price, barSeries, bar, barIdx);
        if (volume == null) {
            return null;
        }
        Position position = account.getPosition(bar.getCode());
        if (position == null) {
            return null;
        }
        return Math.min(position.getAvailableVolume(), volume);
    }

    /**
     * 计算如果开仓当前 Bar 之后的平仓时间
     */
    public static Date calcUnlockTime(BarSeries barSeries, Bar bar) {
        Assert.notNull(barSeries, "参数 barSeries 不能为 null");
        int days = barSeries.getMinHoldingDays();
        Date date = DateUtils.addDays(bar.getTime(), days);
        return DateUtils.parseDate(
            DateUtils.formatToString(date, DateUtils.yyyy_MM_dd) + " " + barSeries.getTradingStartTime(),
            DateUtils.yyyy_MM_dd_HH_mm_ss
        );
    }
}
