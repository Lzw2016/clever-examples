package org.clever.quant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.clever.core.Assert;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 仓位数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 15:55 <br/>
 */
@ToString
@EqualsAndHashCode
@Getter
public class Position {
    /**
     * 金融产品编码
     */
    private final String code;
    /**
     * 持有总量
     */
    private int volume;
    /**
     * 可用量
     */
    private int availableVolume;
    /**
     * 平均成本价
     */
    private double avgCostPrice;
    /**
     * 锁定的持有量 {@code Map<解除锁定的时间, 锁定的数量>}
     */
    private final Map<Date, Integer> lockVolumes = new ConcurrentHashMap<>();

    public Position(String code) {
        Assert.isNotBlank(code, "参数 code 不能为空");
        this.code = code;
        this.volume = 0;
        this.availableVolume = 0;
        this.avgCostPrice = 0;
    }

    /**
     * 锁定的持有量 {@code Map<解除锁定的时间, 锁定的数量>}
     */
    public Map<Date, Integer> getLockVolumes() {
        return Collections.unmodifiableMap(lockVolumes);
    }

    /**
     * 解除锁定持有量
     *
     * @param unlockTime 解锁时间(小于等于这个时间的都能解锁)
     * @return 解除锁定之后的持有可用量
     */
    public synchronized int unlockVolumes(Date unlockTime) {
        Assert.notNull(unlockTime, "参数 unlockTime 不能为 null");
        int unlockedVolume = lockVolumes.entrySet().stream()
            .filter(entry -> !entry.getKey().after(unlockTime))
            .mapToInt(Map.Entry::getValue)
            .sum();
        lockVolumes.entrySet().removeIf(entry -> !entry.getKey().after(unlockTime));
        availableVolume = availableVolume + unlockedVolume;
        return availableVolume;
    }

    /**
     * 增持
     *
     * @param volume 增加的持仓量
     * @param price  撮合成交价
     * @param fee    手续费(交易成本)
     * @return 增持之后的持有总量
     */
    public int increase(int volume, double price, double fee) {
        return increase(volume, price, fee, null);
    }

    /**
     * 增持
     *
     * @param volume     增加的持仓量
     * @param price      撮合成交价
     * @param fee        手续费(交易成本)
     * @param unlockTime 解锁时间
     * @return 增持之后的持有总量
     */
    public synchronized int increase(int volume, double price, double fee, Date unlockTime) {
        Assert.isTrue(volume > 0, "参数 volume 必须大于 0");
        Assert.isTrue(price > 0, "参数 price 必须大于 0");
        if (unlockTime == null) {
            // 无锁定期
            availableVolume = availableVolume + volume;
        } else {
            // T + n
            lockVolumes.compute(unlockTime, (date, oldVolume) -> {
                if (oldVolume == null) {
                    return volume;
                }
                return oldVolume + volume;
            });
        }
        final int sumVolume = this.volume + volume;
        this.avgCostPrice = (this.volume * this.avgCostPrice + volume * price + fee) / sumVolume;
        this.volume = sumVolume;
        return this.volume;
    }

    /**
     * 减持
     *
     * @param volume 减少的持仓量
     * @param price  撮合成交价
     * @param fee    手续费(交易成本)
     * @return 减持之后的持有总量
     */
    public synchronized int decrease(int volume, double price, double fee) {
        Assert.isTrue(volume > 0, "参数 volume 必须大于 0");
        Assert.isTrue(availableVolume >= volume, String.format("参数 volume 必须小于等于 availableVolume, availableVolume=%s", availableVolume));
        Assert.isTrue(price > 0, "参数 price 必须大于 0");
        availableVolume = availableVolume - volume;
        final int sumVolume = this.volume - volume;
        this.avgCostPrice = ((this.volume * this.avgCostPrice) - (volume * price) + fee) / sumVolume;
        this.volume = sumVolume;
        return this.volume;
    }
}
