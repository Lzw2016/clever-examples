package quant;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.clever.core.Assert;
import org.clever.core.NamingUtils;
import org.clever.core.mapper.BeanCopyUtils;
import org.clever.core.mapper.JacksonMapper;
import org.clever.quant.*;
import org.clever.quant.archive.AbstractBacktestArchiver;
import org.clever.quant.archive.entity.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 归档回测数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/05 10:59 <br/>
 */
@Slf4j
@Getter
public class KafkaBacktestArchiver extends AbstractBacktestArchiver {
    private final Admin admin;
    private final KafkaProducer<String, String> kafkaProducer;
    private final String topic;

    /**
     * @param name          回测方案名称
     * @param tag           回测标签名
     * @param account       交易账户
     * @param mainBarSeries 交易的目标 BarSeries
     * @param auxBarSeries  辅助 BarSeries
     * @param indicators    行情Bar的指标
     * @param rules         交易规则
     * @param strategies    交易策略
     * @param admin         kafka admin
     * @param kafkaProducer kafka生产者
     * @param topic         数据保存队列
     */
    public KafkaBacktestArchiver(String name,
                                 String tag,
                                 Account account,
                                 BarSeries mainBarSeries,
                                 Set<BarSeries> auxBarSeries,
                                 List<Indicator<?>> indicators,
                                 List<Rule> rules,
                                 List<Strategy> strategies,
                                 Admin admin,
                                 KafkaProducer<String, String> kafkaProducer,
                                 String topic) {
        super(name, tag, account, mainBarSeries, auxBarSeries, indicators, rules, strategies);
        Assert.notNull(admin, "参数 admin 不能为 null");
        Assert.notNull(kafkaProducer, "参数 kafkaProducer 不能为 null");
        Assert.isNotBlank(topic, "参数 topic 不能为空");
        this.admin = admin;
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
    }

    @Override
    public synchronized void start(Trader trader) {
        BaseDataSource.createTopic(admin, topic);
        super.start(trader);
    }

    @Override
    public synchronized void end() {
        super.end();
        kafkaProducer.flush();
    }

    @Override
    protected void saveData(Object data) {
        if (data == null) {
            return;
        }
        String tableName;
        if (data instanceof BacktestRecord) {
            tableName = "backtest_record";
        } else if (data instanceof BacktestBarSeries) {
            tableName = "backtest_bar_series";
        } else if (data instanceof BacktestBar) {
            tableName = "backtest_bar";
        } else if (data instanceof BacktestIndicator) {
            tableName = "backtest_indicator";
        } else if (data instanceof BacktestRule) {
            tableName = "backtest_rule";
        } else if (data instanceof BacktestStrategy) {
            tableName = "backtest_strategy";
        } else if (data instanceof BacktestAccountSnapshot) {
            tableName = "backtest_account_snapshot";
        } else if (data instanceof BacktestPositions) {
            tableName = "backtest_positions";
        } else if (data instanceof BacktestTradeLog) {
            tableName = "backtest_trade_log";
        } else {
            throw new IllegalArgumentException(String.format("不支持保存类型: %s", data.getClass().getName()));
        }
        Map<String, Object> map = BeanCopyUtils.toMap(data);
        Map<String, Object> renameMap = NamingUtils.camelToUnderline(map);
        String value = tableName + "|" + JacksonMapper.getInstance().toJson(renameMap);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, tableName, value);
        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("保存数据失败，data=[{}]", value, exception);
            }
        });
    }
}
