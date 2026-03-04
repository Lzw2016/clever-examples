package org.clever.quant;

import lombok.Builder;
import lombok.Data;
import org.clever.core.Assert;
import org.clever.core.Conv;

import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 行情Bar数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/26 18:13 <br/>
 */
@Builder
@Data
public class Bar {
    /**
     * 最小持有天数
     */
    public static final String MIN_HOLDING_DAYS = "minHoldingDays";
    /**
     * 开盘日开始交易时间
     */
    public static final String TRADING_START_TIME = "tradingStartTime";
    /**
     * 金融产品编码
     */
    private final String code;
    /**
     * 周期
     */
    private final Period period;
    /**
     * 时间
     */
    private final Date time;
    /**
     * 开盘价
     */
    private double open;
    /**
     * 最高价
     */
    private double high;
    /**
     * 最低价
     */
    private double low;
    /**
     * 收盘价
     */
    private double close;
    /**
     * 成交量
     */
    private long volume;
    /**
     * 成交额
     */
    private double amount;
    /**
     * 其它扩展数据
     */
    private final Map<String, Object> extData = new HashMap<>();

    /**
     * 增加扩展数据
     *
     * @param name 数据名称
     * @param val  数据值
     */
    public void addExtData(String name, Object val) {
        Assert.isNotBlank(name, "参数 name 不能为空");
        if (val == null) {
            extData.remove(name);
        } else {
            extData.put(name, val);
        }
    }

    /**
     * 移除扩展数据
     *
     * @param name 数据名称
     */
    public void removeExtData(String name) {
        extData.remove(name);
    }

    /**
     * 获取扩展数据
     *
     * @param name 数据名称
     */
    public Object getExtData(String name) {
        return extData.get(name);
    }

    /**
     * 最小持有天数, 默认: 1 (T + 1)
     */
    public int getMinHoldingDays() {
        return Conv.asInteger(getExtData(MIN_HOLDING_DAYS), 1);
    }

    /**
     * 最小持有天数
     */
    public void setMinHoldingDays(int minHoldingDays) {
        Assert.isTrue(minHoldingDays >= 0, "参数 minHoldingDays 必须大于等于 0");
        addExtData(MIN_HOLDING_DAYS, minHoldingDays);
    }

    /**
     * 开盘日开始交易时间, 如: 09:30:00
     */
    public String getTradingStartTime() {
        final String defTime = "09:30:00";
        String tradingStartTime = Conv.asString(getExtData(TRADING_START_TIME), defTime);
        try {
            LocalTime.parse(tradingStartTime);
            return tradingStartTime;
        } catch (Exception e) {
            return defTime;
        }
    }

    /**
     * 开盘日开始交易时间
     *
     * @param tradingStartTime 时间, 如: 09:30:00
     */
    public void setTradingStartTime(String tradingStartTime) {
        Assert.isNotBlank(tradingStartTime, "参数 tradingStartTime 不能为空");
        try {
            LocalTime.parse(tradingStartTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("参数 tradingStartTime 格式错误, 必须是 09:30:00 格式");
        }
        addExtData(TRADING_START_TIME, tradingStartTime);
    }
}
