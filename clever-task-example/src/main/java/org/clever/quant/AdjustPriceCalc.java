package org.clever.quant;

import java.util.Date;

/**
 * 复权价格计算器
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/13 21:08 <br/>
 */
public interface AdjustPriceCalc {
    /**
     * 计算“前复权”价格
     *
     * @param time  时间(精确到日)
     * @param price 价格
     */
    double front(Date time, double price);

    /**
     * 计算“后复权”价格
     *
     * @param time  时间(精确到日)
     * @param price 价格
     */
    double back(Date time, double price);
}
