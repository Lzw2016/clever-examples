package tmp;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.configuration.MetricOptions;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;

/**
 * 作者：lizw <br/>
 * 创建时间：2025/12/31 16:47 <br/>
 */
@Slf4j
public class FlinkTest {
    @SneakyThrows
    @Test
    public void test01() {
        Configuration config = new Configuration();
        config.set(DeploymentOptions.TARGET, "embedded");
        config.set(CoreOptions.DEFAULT_PARALLELISM, 1);
        // 禁用系统指标（包括文件描述符等）
        config.set(MetricOptions.SCOPE_NAMING_JM, "");
        config.set(MetricOptions.SCOPE_NAMING_TM, "");
        config.set(MetricOptions.SCOPE_NAMING_TASK, "");
        config.set(MetricOptions.SCOPE_NAMING_OPERATOR, "");
        // config.set(MetricOptions., false);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(config);
        DataGeneratorSource<String> source = new DataGeneratorSource<>(
            value -> String.format("seq:%s", value),
            100,
            RateLimiterStrategy.perSecond(10),
            Types.STRING
        );
        DataStreamSource<String> stream = env.fromSource(
            source,
            WatermarkStrategy.noWatermarks(),
            "n_01"
        );
        stream.sinkTo(context -> new LogSinkWriter<>("01"));
        stream.sinkTo(context -> new LogSinkWriter<>("02"));
        env.execute("my_first_job");
        env.close();
    }
}

@Slf4j
class LogSinkWriter<T> implements SinkWriter<T> {
    private final String id;

    LogSinkWriter(String id) {
        this.id = id;
    }

    @Override
    public void write(T element, Context context) {
        log.info("[{}]item: {}", id, element);
    }

    @Override
    public void flush(boolean endOfInput) {
        log.info("[{}]flush", id);
    }

    @Override
    public void close() {
        log.info("[{}]close", id);
    }
}
