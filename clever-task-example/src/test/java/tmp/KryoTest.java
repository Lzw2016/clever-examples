package tmp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.clever.data.jdbc.Jdbc;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;

/**
 * 作者：lizw <br/>
 * 创建时间：2025/12/29 11:59 <br/>
 */
@Slf4j
public class KryoTest {
    public static Kryo createKryo() {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.register(MyData.class);
        kryo.register(LinkedCaseInsensitiveMap.class);
        kryo.register(BigDecimal.class);
        return kryo;
    }

    @SneakyThrows
    @Test
    public void test01() {
        Jdbc jdbc = JdbcTest.createInnerJdbc();
        Kryo kryo = createKryo();
        MyData data = new MyData();
        data.setDbData(jdbc.queryFirst("select * from doc_task_items "));
        log.info("--> {}", data);
        Output output = new Output(new FileOutputStream("file.txt"));
        kryo.writeObject(output, data);
        output.close();
        Input input = new Input(new FileInputStream("file.txt"));
        MyData data2 = kryo.readObject(input, MyData.class);
        log.info("--> {}", data2);
        input.close();
        jdbc.close();
    }
}
