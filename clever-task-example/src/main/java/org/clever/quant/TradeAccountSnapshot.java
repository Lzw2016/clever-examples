package org.clever.quant;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 交易账户快照
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 17:07 <br/>
 */
@Builder
@Data
public class TradeAccountSnapshot {
    /**
     * 持仓状态 {@code Map<code, Position>}
     */
    private final Map<String, Position> positions;
    /**
     * 账户余额
     */
    private final double balance;

    /**
     * TODO 抽象成持仓信息相关接口
     * 获取持仓信息
     *
     * @param code 金融产品编码
     * @return 如果未持仓返回 null
     */
    public Position getPosition(String code) {
        return positions.get(code);
    }
}
