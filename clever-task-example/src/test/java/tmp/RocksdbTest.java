package tmp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.clever.core.BatchDataUtils;
import org.clever.core.random.RandomUtil;
import org.clever.data.jdbc.Jdbc;
import org.junit.jupiter.api.Test;
import org.rocksdb.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 作者：lizw <br/>
 * 创建时间：2025/12/29 22:20 <br/>
 */
@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
public class RocksdbTest {
    static {
        RocksDB.loadLibrary();
    }

    @SneakyThrows
    @Test
    public void test01() {
        // the Options class contains a set of configurable DB options
        // that determines the behaviour of the database.
        final Options options = new Options().setCreateIfMissing(true);
        // a factory method that returns a RocksDB instance
        final RocksDB db = RocksDB.open(options, "rocksdb_01");
        db.put("k1".getBytes(StandardCharsets.UTF_8), "v1".getBytes(StandardCharsets.UTF_8));
        byte[] v1 = db.get("k1".getBytes(StandardCharsets.UTF_8));
        log.info("v1={}", new String(v1));
        db.close();
        options.close();
    }

    @SneakyThrows
    @Test
    public void test02() {
        Jdbc jdbc = JdbcTest.createInnerJdbc();
        Kryo kryo = KryoTest.createKryo();
        final Options options = new Options().setCreateIfMissing(true);
        final RocksDB db = RocksDB.open(options, "rocksdb_02");
        List<Map<String, Object>> list = jdbc.queryMany("select * from bas_item limit 1000000");
        final long startTime = System.currentTimeMillis();
        long count = 0;
        for (Map<String, Object> map : list) {
            count++;
            MyData data = new MyData();
            data.setDbData(map);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Output output = new Output(baos);
            kryo.writeObject(output, data);
            output.close();
            db.put(String.valueOf(count).getBytes(StandardCharsets.UTF_8), baos.toByteArray());
            if (count % 10000 == 0) {
                log.info("[写]数量: {}", count);
            }
        }
        final long endTime = System.currentTimeMillis();
        log.info("[写]数量: {}", count);
        // [写]数量: 1000000 | 507MB | 耗时: 13255ms, 速度: 75.44322897019993行/ms
        // [写]数量: 1000000 |
        log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count * 1.0 / (endTime - startTime));
        db.close();
        options.close();
        jdbc.close();
    }

    @SneakyThrows
    @Test
    public void test03() {
        Jdbc jdbc = JdbcTest.createInnerJdbc();
        Kryo kryo = KryoTest.createKryo();
        final Options options = new Options().setCreateIfMissing(true);
        // 控制单个MemTable的大小,默认64MB
        options.setWriteBufferSize(128 * 1024 * 1024);
        // 控制内存中MemTable的最大数量,默认5
        options.setMaxWriteBufferNumber(6);
        // 触发L0层Compaction的文件数量阈值,默认4
        options.setLevelZeroFileNumCompactionTrigger(10);
        // options.setNumLevels(6);
        // L0文件数量达到此值时延缓写入,默认20
        options.setLevel0SlowdownWritesTrigger(30);
        // L0文件数量达到此值时停止写入,默认36
        options.setLevel0StopWritesTrigger(50);
        // 最大后台任务数(包括flush和compaction),默认2
        options.setMaxBackgroundJobs(6);
        // CompressionOptions compressionOpts = new CompressionOptions();
        // compressionOpts.setLevel(6);
        // options.setCompressionOptions(compressionOpts);
        // 建议使用的压缩算法：LZ4、LZ4HC、ZSTD、ZLIB
        options.setCompressionType(CompressionType.ZSTD_COMPRESSION);
        // options.setCompressionOptions()
        // RocksDB 会定期执行 Compaction（压缩/合并） 操作，将多个 SSTable 合并成更少的新文件
        // UNIVERSAL 是一种 非分层（non-level）的 Compaction 策略，由 RocksDB 提供，适用于写密集、对空间放大敏感的场景
        options.setCompactionStyle(CompactionStyle.UNIVERSAL);
        // 设置每个层级的压缩算法
        // options.setCompressionPerLevel();
        // 启用动态层级压缩(强烈推荐)
        options.setLevelCompactionDynamicLevelBytes(true);
        // 优化布隆过滤器内存使用
        options.setOptimizeFiltersForHits(true);
        // SSD 专属：Direct I/O(绕过 OS Page Cache)注意：仅 Linux 支持，Windows/macOS 会忽略
        // options.setUseDirectReads(true);
        // options.setUseDirectIoForFlushAndCompaction(true);
        // 减少 WAL 的同步频率(默认是 0，即每次都要 sync) 1024 * 1024 = 1MB
        // options.setWalBytesPerSync(1024 * 1024);
        // 或者完全由操作系统控制 flush(不主动 sync)
        // options.setManualWalFlush(true);
        final RocksDB db = RocksDB.open(options, "rocksdb_03");
        List<Map<String, Object>> list = jdbc.queryMany("select * from bas_item limit 1000000");
        final long startTime = System.currentTimeMillis();
        long count = 0;
        boolean batch = true;
        // noinspection ConstantValue
        if (batch) {
            List<List<Map<String, Object>>> batchList = BatchDataUtils.toBatch(list, 5000);
            for (List<Map<String, Object>> maps : batchList) {
                WriteBatch writeBatch = new WriteBatch();
                for (Map<String, Object> map : maps) {
                    count++;
                    MyData data = new MyData();
                    data.setValues(new ArrayList<>(map.values()));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Output output = new Output(baos);
                    kryo.writeObject(output, data);
                    output.close();
                    ByteBuffer buffer = ByteBuffer.allocate(8);
                    buffer.order(ByteOrder.BIG_ENDIAN);
                    buffer.putLong(count);
                    writeBatch.put(buffer.array(), baos.toByteArray());
                }
                WriteOptions writeOptions = new WriteOptions();
                writeOptions.setDisableWAL(true);
                db.write(writeOptions, writeBatch);
                writeOptions.close();
                writeBatch.close();
                if (count % 10000 == 0) {
                    log.info("[写]数量: {}", count);
                }
            }
        } else {
            for (Map<String, Object> map : list) {
                count++;
                MyData data = new MyData();
                data.setValues(new ArrayList<>(map.values()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Output output = new Output(baos);
                kryo.writeObject(output, data);
                output.close();
                WriteOptions writeOptions = new WriteOptions();
                writeOptions.setDisableWAL(true);
                db.put(writeOptions, String.valueOf(count).getBytes(StandardCharsets.UTF_8), baos.toByteArray());
                writeOptions.close();
                if (count % 10000 == 0) {
                    log.info("[写]数量: {}", count);
                }
            }
        }
        db.close();
        final long endTime = System.currentTimeMillis();
        log.info("[写]数量: {}", count);
        // [写]数量: 1000000 | 167MB | 耗时: 10216ms, 速度: 97.88566953797964行/ms
        // 优化
        // [写]数量: 1000000 | 161MB | 耗时:  9852ms, 速度: 101.50223304912708行/ms
        // [写]数量: 1000000 | 162MB | 耗时:  8961ms, 速度: 111.59468809284678行/ms
        // [写]数量: 1000000 | 157MB | 耗时:  9309ms, 速度: 107.4229240519927行/ms
        // [写]数量: 1000000 | 159MB | 耗时:  9342ms, 速度: 107.04345964461571行/ms
        // [写]数量: 1000000 | 141MB | 耗时: 10438ms, 速度: 95.80379383023568行/ms
        // [写]数量: 1000000 | 119MB | 耗时:  8578ms, 速度: 116.57729074376311行/ms
        // [写]数量: 1000000 | 115MB | 耗时:  8192ms, 速度: 122.0703125行/ms
        // [写]数量: 1000000 | 115MB | 耗时:  8218ms, 速度: 121.68410805548795行/ms
        // [写]数量: 1000000 | 115MB | 耗时:  7791ms, 速度: 128.3532280836863行/ms
        log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count * 1.0 / (endTime - startTime));
        // RocksDB.destroyDB("rocksdb_03", options);
        options.close();
        jdbc.close();
    }

    @SneakyThrows
    @Test
    public void test04() {
        Kryo kryo = KryoTest.createKryo();
        final Options options = new Options().setCreateIfMissing(true);
        final RocksDB db = RocksDB.open(options, "rocksdb_03");
        final long startTime = System.currentTimeMillis();
        long count = 0;
        ReadOptions readOpts = new ReadOptions();
        RocksIterator iter = db.newIterator(readOpts);
        for (iter.seekToFirst(); iter.isValid(); iter.next()) {
            ByteBuffer readBuffer = ByteBuffer.wrap(iter.key());
            readBuffer.order(ByteOrder.BIG_ENDIAN);
            long key = readBuffer.getLong();
            byte[] data = iter.value();
            Input input = new Input(data);
            MyData data2 = kryo.readObject(input, MyData.class);
            input.close();
            count++;
            if (count % 10000 == 0) {
                log.info("[读]数量: {} | {}={}", count, key, data2);
            }
        }
        log.info("[读]数量: {}", count);
        final long endTime = System.currentTimeMillis();
        log.info("[读]数量: {}", count);
        // [读]数量: 1000000 | 耗时: 2924ms, 速度: 341.9972640218878行/ms
        // [读]数量: 1000000 | 耗时: 2925ms, 速度: 341.88034188034186行/ms
        // [读]数量: 1000000 | 耗时: 3155ms, 速度: 316.95721077654514行/ms
        log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count * 1.0 / (endTime - startTime));
        db.close();
        options.close();
    }

    @SneakyThrows
    @Test
    public void test05() {
        Kryo kryo = KryoTest.createKryo();
        final Options options = new Options().setCreateIfMissing(true);
        final RocksDB db = RocksDB.open(options, "rocksdb_03");
        final long startTime = System.currentTimeMillis();
        long count = 0;
        do {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putLong(RandomUtil.randomLong(1, 99_9999));
            byte[] data = db.get(buffer.array());
            MyData data2 = null;
            if (data != null) {
                Input input = new Input(data);
                data2 = kryo.readObject(input, MyData.class);
                input.close();
            }
            count++;
            if (count % 10000 == 0) {
                log.info("[读]数量: {} | {}", count, data2);
            }
        } while (count < 100_0000);
        log.info("[读]数量: {}", count);
        final long endTime = System.currentTimeMillis();
        log.info("[读]数量: {}", count);
        // [读]数量: 1000000 | 耗时: 12268ms, 速度: 81.51287903488752行/ms
        // [读]数量: 1000000 | 耗时: 11212ms, 速度: 89.19015340706386行/ms
        // [读]数量: 1000000 | 耗时: 11469ms, 速度: 87.19155985700584行/ms
        // [读]数量: 1000000 | 耗时: 14203ms, 速度: 70.40766035344646行/ms
        // [读]数量: 1000000 | 耗时: 13346ms, 速度: 74.9288176232579行/ms
        // [读]数量: 1000000 | 耗时: 12601ms, 速度: 79.35878104912308行/ms
        log.info("--> 耗时: {}ms, 速度: {}行/ms", endTime - startTime, count * 1.0 / (endTime - startTime));
        db.close();
        options.close();
    }
}
