package quant;

import lombok.Getter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.clever.core.Assert;
import org.clever.quant.*;
import org.clever.quant.archive.AbstractBacktestArchiver;

import java.util.List;
import java.util.Set;

/**
 * 归档回测数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/05 10:59 <br/>
 */
@Getter
public class KafkaBacktestArchiver extends AbstractBacktestArchiver {
    private final KafkaProducer<String, String> kafkaProducer;

    /**
     * @param name          回测方案名称
     * @param tag           回测标签名
     * @param account       交易账户
     * @param mainBarSeries 交易的目标 BarSeries
     * @param auxBarSeries  辅助 BarSeries
     * @param indicators    行情Bar的指标
     * @param rules         交易规则
     * @param strategies    交易策略
     * @param kafkaProducer kafka生产者
     */
    public KafkaBacktestArchiver(String name,
                                 String tag,
                                 Account account,
                                 BarSeries mainBarSeries,
                                 Set<BarSeries> auxBarSeries,
                                 List<Indicator<?>> indicators,
                                 List<Rule> rules,
                                 List<Strategy> strategies,
                                 KafkaProducer<String, String> kafkaProducer) {
        super(name, tag, account, mainBarSeries, auxBarSeries, indicators, rules, strategies);
        Assert.notNull(kafkaProducer, "参数 kafkaProducer 不能为 null");
        this.kafkaProducer = kafkaProducer;
    }
}
