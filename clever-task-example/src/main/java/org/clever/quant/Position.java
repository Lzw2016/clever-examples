package org.clever.quant;

import lombok.Data;

/**
 * 仓位数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 15:55 <br/>
 */
@Data
public class Position {
    /**
     * 金融产品编码
     */
    private final String code;
    /**
     * 持有总量
     */
    private long volume;
    /**
     * 可用量
     */
    private long availableVolume;
    /**
     * 平均成本价
     */
    private double avgCostPrice;
}
