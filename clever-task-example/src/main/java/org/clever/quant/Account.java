package org.clever.quant;

import java.util.List;
import java.util.Map;

/**
 * 交易账户
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 16:51 <br/>
 */
public interface Account {
    /**
     * 账户名称
     */
    String getName();

    /**
     * 初始资金
     */
    double getInitAmount();

    /**
     * 账户余额
     */
    double getBalance();

    /**
     * 所有的分红配送日志
     */
    List<DividendLog> getDividendLogs();

    /**
     * 指定的“金融产品”的分红配送日志
     *
     * @param code 金融产品编码
     */
    List<DividendLog> getDividendLogs(String code);

    /**
     * 所有的历史交易日志
     */
    List<TradeLog> getTradeLogs();

    /**
     * 指定的“金融产品”历史交易日志
     *
     * @param code 金融产品编码
     */
    List<TradeLog> getTradeLogs(String code);

    /**
     * 获取持仓信息
     *
     * @param code 金融产品编码
     * @return 如果未持仓返回 null
     */
    Position getPosition(String code);

    /**
     * 当前账户快照
     */
    TradeAccountSnapshot getSnapshot();

    /**
     * 开仓
     *
     * @param barSeries 交易的目标 BarSeries
     * @param bar       交易的目标 Bar
     * @param barIdx    Bar的索引位置
     * @param price     撮合成交价
     * @param volume    成交量
     * @param fee       手续费(交易成本)
     * @return 如果没有交易就返回 null
     */
    TradeLog enter(BarSeries barSeries, Bar bar, long barIdx, double price, int volume, double fee);

    /**
     * 平仓
     *
     * @param barSeries 交易的目标 BarSeries
     * @param bar       交易的目标 Bar
     * @param barIdx    Bar的索引位置
     * @param price     撮合成交价
     * @param volume    成交量
     * @param fee       手续费(交易成本)
     * @return 如果没有交易就返回 null
     */
    TradeLog exit(BarSeries barSeries, Bar bar, long barIdx, double price, int volume, double fee);

    /**
     * 处理分红配送(除权除息)
     *
     * @param dividend 分红配送数据
     * @param bar      交易的目标 Bar
     * @param barIdx   除权除息日期所对应的Bar的索引位置
     * @return 如果需求处理分红配送返回对应的日志，否则返回 null
     */
    DividendLog processDividend(Dividend dividend, Bar bar, long barIdx);

    /**
     * 获取当前账户的总资产
     *
     * @param priceTable 资产价格表 {@code Map<code, price>}
     */
    double getTotalAssets(Map<String, Double> priceTable);
}
