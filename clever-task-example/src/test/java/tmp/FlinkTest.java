//package tmp;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.doris.flink.cfg.DorisExecutionOptions;
//import org.apache.doris.flink.cfg.DorisOptions;
//import org.apache.doris.flink.sink.DorisSink;
//import org.apache.doris.flink.sink.writer.LoadConstants;
//import org.apache.doris.flink.sink.writer.WriteMode;
//import org.apache.doris.flink.sink.writer.serializer.RowSerializer;
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.table.api.DataTypes;
//import org.apache.flink.table.types.DataType;
//import org.apache.flink.types.Row;
//import org.junit.jupiter.api.Test;
//
///**
// * 作者：lizw <br/>
// * 创建时间：2025/12/30 17:25 <br/>
// */
//@Slf4j
//public class FlinkTest {
//    @Test
//    public void test01() {
//        LocalStreamEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
//        // 设置并行度为1以确保数据顺序
//        env.setParallelism(1);
//        // 构造测试数据：每行代表 (id, name, age)
//        DataStream<Row> source = env.fromData(
//            Row.of(1, "Alice", 25),
//            Row.of(2, "Bob", 30),
//            Row.of(3, "Charlie", 35)
//        );
//        // Doris 连接配置
//        DorisOptions dorisOptions = DorisOptions.builder()
//            .setFenodes("192.168.1.201:8030")
//            .setTableIdentifier("tushare.bas_item")
//            .setUsername("admin")
//            .setPassword("admin123456")
//            .build();
//        // 执行选项
//        DorisExecutionOptions executionOptions = DorisExecutionOptions.builder()
//            .setLabelPrefix("flink_doris_label")
//            // .setStreamLoadProp("format", "json")
//            // .setStreamLoadProp("strip_outer_array", "true")
//            // .setBufferFlushMaxRows(10000)
//            // .setBufferFlushIntervalMs(10000)
//            .setWriteMode(WriteMode.STREAM_LOAD)
//            .build();
//
//        // 根据实际表结构配置序列化器
//        RowSerializer serializer = RowSerializer.builder()
//            .setFieldNames(new String[]{"id", "name", "age"})
//            .setFieldType(new DataType[]{
//                DataTypes.INT(),
//                DataTypes.STRING(),
//                DataTypes.INT(),
//            })
//            .setType(LoadConstants.CSV)
//            .setFieldDelimiter(",")
//            .build();
//
//        // 创建 Doris Sink
//        DorisSink<Row> dorisSink = DorisSink.<Row>builder()
//            .setDorisOptions(dorisOptions)
//            .setDorisExecutionOptions(executionOptions)
//            .setSerializer(serializer)
//            .build();
//        // 写入 Doris
//        source.sinkTo(dorisSink);
//        try {
//            env.execute("Flink Doris Batch Write Example");
//            log.info("完成");
//            env.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("执行失败", e);
//        }
//    }
//}
