package tmp;

import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.pool.ClassAliasPool;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.clever.data.jdbc.Jdbc;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.sql.Timestamp;

/**
 * 作者：lizw <br/>
 * 创建时间：2025/12/29 10:14 <br/>
 */
@Slf4j
public class ChronicleWireTest {
    @Test
    public void test01() {
        Bytes<?> bytes = Bytes.allocateElasticOnHeap();
        Wire wire = WireType.TEXT.apply(bytes);
        wire.write("message").text("Hello World");
        log.info("--> {}", bytes);
    }

    @Test
    public void test02() {
        Jdbc jdbc = JdbcTest.createInnerJdbc();
        Bytes<ByteBuffer> bytes = Bytes.elasticByteBuffer();
        ClassAliasPool.CLASS_ALIASES.addAlias(Timestamp.class);
        // Wire wire = WireType.BINARY.apply(bytes);
        // Wire wire = new YamlWire(bytes).useBinaryDocuments();
        Wire wire = new TextWire(bytes).useTextDocuments();
        MyData data = new MyData();
        data.setDbData(jdbc.queryFirst("select * from doc_task_items "));
        log.info("--> {}", data);
        data.writeMarshallable(wire);
        // log.info("--> \n{}\n<--", bytes.toHexString());
        log.info("--> \n{}\n<--", bytes);
        MyData data2 = new MyData();
        data2.readMarshallable(wire);
        log.info("--> {}", data2);
        jdbc.close();
    }
}
