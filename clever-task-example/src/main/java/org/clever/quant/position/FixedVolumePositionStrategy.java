package org.clever.quant.position;

import lombok.Data;
import org.clever.core.Assert;
import org.clever.quant.Bar;
import org.clever.quant.BarSeries;
import org.clever.quant.PositionStrategy;
import org.clever.quant.TradeAccountSnapshot;

/**
 * 固定买卖量持仓策略 <br/>
 * 1.买入时: 按照指定的{@code volume}尽量执行买入<br/>
 * 2.卖出时: 按照指定的{@code volume}尽量执行卖出<br/>
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/02 22:30 <br/>
 */
@Data
public class FixedVolumePositionStrategy implements PositionStrategy {
    /**
     * 指定的买卖量(股)
     */
    private final int volume;

    /**
     * 指定的买卖量为: 1手(100股)
     */
    public FixedVolumePositionStrategy() {
        this(100);
    }

    /**
     * @param volume 指定的买卖量(股)
     */
    public FixedVolumePositionStrategy(int volume) {
        Assert.isTrue(volume > 0, "参数 volume 必须大于 0");
        this.volume = volume;
    }

    @Override
    public Integer calcEnterVolume(TradeAccountSnapshot accountSnapshot, double price, BarSeries barSeries, Bar bar, long barIdx) {
        return volume;
    }

    @Override
    public Integer calcExitVolume(TradeAccountSnapshot accountSnapshot, double price, BarSeries barSeries, Bar bar, long barIdx) {
        return volume;
    }
}
