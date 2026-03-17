package quant;

import lombok.Getter;
import org.clever.quant.AdjustPriceCalc;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

/**
 * 讯投xtquant数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/16 22:05 <br/>
 */
@Getter
public class XTAdjustPriceCalc implements AdjustPriceCalc {
    /**
     * 日期与复权因子的映射关系 {@code TreeMap<日期, 复权因子>}
     */
    private final TreeMap<Date, Double> dividends = new TreeMap<>();
    /**
     * 最后一个复权因子
     */
    private final Double lastAdjustFactor;

    public XTAdjustPriceCalc(List<DividendInfo> dividendInfos) {
        dividendInfos.sort(Comparator.comparing(DividendInfo::getTime));
        Double adjustFactor = 1D;
        for (DividendInfo dividend : dividendInfos) {
            adjustFactor = adjustFactor * dividend.getDr();
            dividends.put(dividend.getTime(), adjustFactor);
        }
        lastAdjustFactor = adjustFactor;
    }

    @Override
    public double front(Date time, double price) {
        double adjustFactor = findAdjustFactor(time);
        return price * adjustFactor / lastAdjustFactor;
    }

    @Override
    public double back(Date time, double price) {
        double adjustFactor = findAdjustFactor(time);
        return price * adjustFactor;
    }

    protected double findAdjustFactor(Date time) {
        // floorKey(): 返回≤time的最大key，无则返回null
        Date floorKey = dividends.floorKey(time);
        if (floorKey == null) {
            return 1;
        }
        return dividends.get(floorKey);
    }
}
