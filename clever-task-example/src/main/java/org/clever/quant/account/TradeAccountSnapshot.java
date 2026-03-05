package org.clever.quant.account;

import lombok.Builder;
import lombok.Data;
import org.clever.core.Assert;
import org.clever.quant.Position;

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
     * 账户名称
     */
    private final String name;
    /**
     * 持仓状态 {@code Map<code, Position>}
     */
    private final Map<String, Position> positions;
    /**
     * 账户余额
     */
    private final double balance;

    /**
     * 获取当前账户的总资产
     *
     * @param priceTable 资产价格表 {@code Map<code, price>}
     */
    public double getTotalAssets(Map<String, Double> priceTable) {
        Assert.notNull(priceTable, "参数 priceTable 不能为 null");
        double totalAssets = balance;
        for (Map.Entry<String, Position> entry : positions.entrySet()) {
            String code = entry.getKey();
            Position position = entry.getValue();
            Double price = priceTable.get(code);
            Assert.notNull(price, String.format("参数 priceTable 中不存在key=%s", code));
            Assert.isTrue(price > 0, String.format("参数 priceTable 中key=%s的值price=%s必须大于 0", code, price));
            totalAssets = totalAssets + price * position.getVolume();
        }
        return totalAssets;
    }
}
