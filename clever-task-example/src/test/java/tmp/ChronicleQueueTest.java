package tmp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.wire.WireType;
import org.apache.commons.io.FileUtils;
import org.clever.data.jdbc.Jdbc;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 作者：lizw <br/>
 * 创建时间：2025/12/29 13:05 <br/>
 */
@SuppressWarnings({"LoggingSimilarMessage", "TryFinallyCanBeTryWithResources"})
@Slf4j
public class ChronicleQueueTest {
    @SneakyThrows
    public void del(File file) {
        // Chronicle Queue 使用内存映射文件（mmap），而 JVM 的 mmap 资源释放是延迟的（依赖 GC）
        System.gc();
        Thread.sleep(1_000);
        System.gc();
        log.info("file -> {}", file.getAbsolutePath());
        FileUtils.deleteDirectory(file);
    }

    @SneakyThrows
    @Test
    public void test01() {
        Jdbc jdbc = JdbcTest.createInnerJdbc();
        File queueDir = new File("chronicle_queue_01");
        SingleChronicleQueue queue = ChronicleQueue.singleBuilder(queueDir)
            .rollCycle(RollCycles.FAST_DAILY)
            .wireType(WireType.BINARY_LIGHT)
            // .forceDirectoryListingRefreshIntervalMs(60_000)
            // .blockSize(64 << 20)
            // .bufferCapacity(1 << 20)
            // .readOnly(false)
            // .syncMode(SyncMode.ASYNC)
            // .checkInterrupts(true)
            // .sourceId(1)
            .build();
        try {
            ExcerptAppender appender = queue.acquireAppender();
            final long startTime = System.currentTimeMillis();
            final AtomicLong count = new AtomicLong(0);
            jdbc.queryForCursor("select * from doc_task_items limit 10", rowData -> {
                MyData data = new MyData();
                data.setDbData(rowData.getRowData());
                appender.writeDocument(data);
                long cnt = count.incrementAndGet();
                if (cnt % 10000 == 0) {
                    log.info("[写]数量: {}", cnt);
                }
            });
            log.info("[写]数量: {}", count.get());
            final long endTime = System.currentTimeMillis();
            log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count.get() * 1.0 / (endTime - startTime));
            appender.close();

            count.set(0);
            ExcerptTailer tailer = queue.createTailer("my-consumer-01");
            boolean hasNext;
            do {
                hasNext = tailer.readDocument(wire -> {
                    MyData data2 = new MyData();
                    data2.readMarshallable(wire);
                    long cnt = count.incrementAndGet();
                    if (cnt % 10000 == 0) {
                        log.info("[读]数量: {}", cnt);
                    }
                    // log.info("index -> {}", tailer.index());
                });
            } while (hasNext);
            log.info("[读]数量: {}", count.get());
            log.info("index -> {}", tailer.index());
            tailer.close();
        } finally {
            queue.close();
            jdbc.close();
            //del(queue.file());
        }
    }

    @SneakyThrows
    @Test
    public void test02() {
        Jdbc jdbc = JdbcTest.createInnerJdbc();
        File queueDir = new File("chronicle_queue_02");
        SingleChronicleQueue queue = ChronicleQueue.singleBuilder(queueDir)
            .rollCycle(RollCycles.FAST_DAILY)
            .wireType(WireType.BINARY_LIGHT)
            .build();
        try {
            ExcerptAppender appender = queue.acquireAppender();
            final long startTime = System.currentTimeMillis();
            final AtomicLong count = new AtomicLong(0);
            jdbc.queryForCursor("select * from bas_package_items", rowData -> {
                MyData data = new MyData();
                data.setDbData(rowData.getRowData());
                appender.writeDocument(data);
                long cnt = count.incrementAndGet();
                if (cnt % 10000 == 0) {
                    log.info("[写]数量: {}", cnt);
                }
            });
            log.info("[写]数量: {}", count.get());
            final long endTime = System.currentTimeMillis();
            log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count.get() * 1.0 / (endTime - startTime));
            appender.close();
        } finally {
            queue.close();
            jdbc.close();
            //del(queue.file());
        }
    }

    @SneakyThrows
    @Test
    public void test03() {
        File queueDir = new File("chronicle_queue_02");
        SingleChronicleQueue queue = ChronicleQueue.singleBuilder(queueDir)
            .rollCycle(RollCycles.FAST_DAILY)
            .wireType(WireType.BINARY_LIGHT)
            .build();
        try {
            final AtomicLong count = new AtomicLong(0);
            ExcerptTailer tailer = queue.createTailer("my-consumer-01");
            tailer.toStart();
            final long startTime = System.currentTimeMillis();
            boolean hasNext;
            do {
                hasNext = tailer.readDocument(wire -> {
                    MyData data2 = new MyData();
                    data2.readMarshallable(wire);
                    long cnt = count.incrementAndGet();
                    if (cnt % 10000 == 0) {
                        log.info("[读]数量: {} | {}", cnt, data2);
                    }
                    // log.info("index -> {}", tailer.index());
                });
            } while (hasNext);
            log.info("index -> {}", tailer.index());
            log.info("[读]数量: {}", count.get());
            final long endTime = System.currentTimeMillis();
            // [读]数量: 4358779 | 耗时: 27322ms, 速度: 159.53367249835298行/ms
            // [读]数量: 4358779 | 耗时: 26246ms, 速度: 166.074030328431行/ms
            log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count.get() * 1.0 / (endTime - startTime));
            tailer.close();
        } finally {
            queue.close();
            //del(queue.file());
        }
    }

    @SneakyThrows
    @Test
    public void test04() {
        Jdbc jdbc = JdbcTest.createInnerJdbc();
        File queueDir = new File("chronicle_queue_04");
        SingleChronicleQueue queue = ChronicleQueue.singleBuilder(queueDir)
            .rollCycle(RollCycles.FAST_DAILY)
            .wireType(WireType.BINARY_LIGHT)
            .build();
        try {
            ExcerptAppender appender = queue.acquireAppender();
            List<Map<String, Object>> list = jdbc.queryMany("select * from bas_item limit 1000000");
            final long startTime = System.currentTimeMillis();
            final AtomicLong count = new AtomicLong(0);
            for (Map<String, Object> map : list) {
                MyData data = new MyData();
                data.setDbData(map);
                appender.writeDocument(data);
                long cnt = count.incrementAndGet();
                if (cnt % 10000 == 0) {
                    log.info("[写]数量: {}", cnt);
                }
            }
            log.info("[写]数量: {}", count.get());
            final long endTime = System.currentTimeMillis();
            // [写]数量: 1000000 | 耗时: 14530ms, 速度: 68.82312456985547行/ms
            // [写]数量: 1000000 | 耗时: 14530ms, 速度: 68.82312456985547行/ms
            // [写]数量: 1000000 | 耗时: 14325ms, 速度: 69.80802792321117行/ms
            log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count.get() * 1.0 / (endTime - startTime));
            appender.close();
        } finally {
            queue.close();
            jdbc.close();
            //del(queue.file());
        }
    }

    @SneakyThrows
    @Test
    public void test05() {
        Jdbc jdbc = JdbcTest.createInnerJdbc();
        File queueDir = new File("chronicle_queue_05");
        SingleChronicleQueue queue = ChronicleQueue.singleBuilder(queueDir)
            .rollCycle(RollCycles.FAST_DAILY)
            .wireType(WireType.BINARY_LIGHT)
            .build();
        try {
            ExcerptAppender appender = queue.acquireAppender();
            List<Map<String, Object>> list = jdbc.queryMany("select * from bas_item limit 1000000");
            final long startTime = System.currentTimeMillis();
            final AtomicLong count = new AtomicLong(0);
            for (Map<String, Object> map : list) {
                MyData data = new MyData();
                data.setDbData(map);
                appender.writeDocument(data);
                long cnt = count.incrementAndGet();
                if (cnt % 10000 == 0) {
                    log.info("[写]数量: {}", cnt);
                }
            }
            log.info("[写]数量: {}", count.get());
            final long endTime = System.currentTimeMillis();
            // [写]数量: 1000000 | 耗时: 14530ms, 速度: 68.82312456985547行/ms
            // [写]数量: 1000000 | 耗时: 14530ms, 速度: 68.82312456985547行/ms
            // [写]数量: 1000000 | 耗时: 14325ms, 速度: 69.80802792321117行/ms
            log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count.get() * 1.0 / (endTime - startTime));
            appender.close();
        } finally {
            queue.close();
            jdbc.close();
            //del(queue.file());
        }
    }

    @SneakyThrows
    @Test
    public void test06() {
        Jdbc jdbc = JdbcTest.createInnerJdbc();
        Kryo kryo = KryoTest.createKryo();
        File queueDir = new File("chronicle_queue_06");
        SingleChronicleQueue queue = ChronicleQueue.singleBuilder(queueDir)
            .rollCycle(RollCycles.FAST_DAILY)
            .wireType(WireType.BINARY_LIGHT)
            .build();
        try {
            ExcerptAppender appender = queue.acquireAppender();
            final long startTime = System.currentTimeMillis();
            final AtomicLong count = new AtomicLong(0);
            jdbc.queryForCursor("select * from doc_task_items limit 10", rowData -> {
                MyData data = new MyData();
                data.setDbData(rowData.getRowData());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Output output = new Output(baos);
                kryo.writeObject(output, data);
                output.close();
                appender.writeBytes(Bytes.wrapForRead(baos.toByteArray()));
                long cnt = count.incrementAndGet();
                log.info("[写]数量: {} ", cnt);
            });
            log.info("[写]数量: {}", count.get());
            final long endTime = System.currentTimeMillis();
            log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count.get() * 1.0 / (endTime - startTime));
            appender.close();

            count.set(0);
            ExcerptTailer tailer = queue.createTailer("my-consumer-01");
            boolean hasNext;
            do {
                hasNext = tailer.readDocument(wire -> {
                    byte[] data = wire.bytes().toByteArray();
                    Input input = new Input(data);
                    MyData data2 = kryo.readObject(input, MyData.class);
                    input.close();
                    long cnt = count.incrementAndGet();
                    if (cnt % 10000 == 0) {
                        log.info("[读]数量: {} | {}", cnt, data2);
                    }
                    log.info("index -> {}", tailer.index());
                });
            } while (hasNext);
            log.info("[读]数量: {}", count.get());
            log.info("index -> {}", tailer.index());
            tailer.close();
        } finally {
            queue.close();
            jdbc.close();
            // del(queue.file());
        }
    }

    @SneakyThrows
    @Test
    public void test07() {
        Jdbc jdbc = JdbcTest.createInnerJdbc();
        Kryo kryo = KryoTest.createKryo();
        File queueDir = new File("chronicle_queue_07");
        SingleChronicleQueue queue = ChronicleQueue.singleBuilder(queueDir)
            .rollCycle(RollCycles.FAST_DAILY)
            .wireType(WireType.BINARY_LIGHT)
            .build();
        try {
            ExcerptAppender appender = queue.acquireAppender();
            List<Map<String, Object>> list = jdbc.queryMany("select * from bas_item limit 1000000");
            final long startTime = System.currentTimeMillis();
            final AtomicLong count = new AtomicLong(0);
            for (Map<String, Object> map : list) {
                MyData data = new MyData();
                data.setDbData(map);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Output output = new Output(baos);
                kryo.writeObject(output, data);
                output.close();
                appender.writeBytes(Bytes.wrapForRead(baos.toByteArray()));
                long cnt = count.incrementAndGet();
                if (cnt % 10000 == 0) {
                    log.info("[写]数量: {}", cnt);
                }
            }
            log.info("[写]数量: {}", count.get());
            final long endTime = System.currentTimeMillis();
            // [写]数量: 1000000 | 1.51GB | 耗时: 12168ms, 速度: 82.1827744904668行/ms
            log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count.get() * 1.0 / (endTime - startTime));
            appender.close();
        } finally {
            queue.close();
            jdbc.close();
            //del(queue.file());
        }
    }

    @SneakyThrows
    @Test
    public void test08() {
        Kryo kryo = KryoTest.createKryo();
        File queueDir = new File("chronicle_queue_07");
        SingleChronicleQueue queue = ChronicleQueue.singleBuilder(queueDir)
            .rollCycle(RollCycles.FAST_DAILY)
            .wireType(WireType.BINARY_LIGHT)
            .build();
        try {
            final AtomicLong count = new AtomicLong(0);
            ExcerptTailer tailer = queue.createTailer("my-consumer-01");
            tailer.toStart();
            final long startTime = System.currentTimeMillis();
            boolean hasNext;
            do {
                hasNext = tailer.readDocument(wire -> {
                    byte[] data = wire.bytes().toByteArray();
                    Input input = new Input(data);
                    MyData data2 = kryo.readObject(input, MyData.class);
                    input.close();
                    long cnt = count.incrementAndGet();
                    if (cnt % 10000 == 0) {
                        log.info("[读]数量: {} | {}", cnt, data2);
                    }
                    // log.info("index -> {}", tailer.index());
                });
            } while (hasNext);
            log.info("index -> {}", tailer.index());
            log.info("[读]数量: {}", count.get());
            final long endTime = System.currentTimeMillis();
            // [读]数量: 1000000 | 耗时: 6618ms, 速度: 151.1030522816561行/ms
            // [读]数量: 1000000 | 耗时: 6333ms, 速度: 157.9030475288173行/ms
            // [读]数量: 1000000 | 耗时: 6635ms, 速度: 150.71590052750565行/ms
            log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count.get() * 1.0 / (endTime - startTime));
            tailer.close();
        } finally {
            queue.close();
            //del(queue.file());
        }
    }
}
