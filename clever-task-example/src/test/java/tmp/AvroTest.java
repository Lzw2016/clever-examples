package tmp;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.clever.core.id.IDCreateUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/01/21 21:05 <br/>
 */
@Slf4j
public class AvroTest {

    @Test
    public void test01() {
        // <groupId>org.apache.avro</groupId>
        // <artifactId>avro-maven-plugin</artifactId>

//        // 1. 创建 User 对象
//        MyData2 user = MyData2.newBuilder()
//            .setId(1)
//            .setName("张三")
//            .setEmail("zhangsan@example.com")
//            .build();
//
//        // 2. 序列化到文件
//        DatumWriter<User> datumWriter = new SpecificDatumWriter<>(User.class);
//        DataFileWriter<User> dataFileWriter = new DataFileWriter<>(datumWriter);
//        dataFileWriter.create(user.getSchema(), new File("user.avro"));
//        dataFileWriter.append(user);
//        dataFileWriter.close();
//
//        System.out.println("✅ 已写入 user.avro");
//
//        // 3. 从文件反序列化
//        DatumReader<User> datumReader = new SpecificDatumReader<>(User.class);
//        DataFileReader<User> dataFileReader = new DataFileReader<>(new File("user.avro"), datumReader);
//        User readUser = dataFileReader.next();
//        dataFileReader.close();
//
//        System.out.println("读取结果: id=" + readUser.getId() +
//            ", name=" + readUser.getName() +
//            ", email=" + readUser.getEmail());
    }

    @SneakyThrows
    @Test
    public void test02() {
        // 1. 从 .avsc 文件加载 schema
        Schema schema = new Schema.Parser().parse(
            this.getClass().getClassLoader().getResourceAsStream("MyData2.avsc")
        );
        // 2. 创建 GenericRecord
        GenericRecord row = new GenericData.Record(schema);
        row.put("f1", -1);
        row.put("f2", IDCreateUtils.shortUuid());
        row.put("f3", System.currentTimeMillis());
        // 3. 写入文件
        DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(schema);
        DataFileWriter<GenericRecord> fileWriter = new DataFileWriter<>(writer);
        File file = new File("./out/MyData2.avro");
        log.info("file -> {}", file.getAbsolutePath());
        fileWriter.create(schema, file);
        fileWriter.append(row);
        for (int idx = 0; idx < 100; idx++) {
            row = new GenericData.Record(schema);
            row.put("f1", idx);
            row.put("f2", IDCreateUtils.shortUuid());
            row.put("f3", System.currentTimeMillis());
            fileWriter.append(row);
        }
        fileWriter.close();
        // 4. 读取
        DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
        DataFileReader<GenericRecord> fileReader = new DataFileReader<>(file, reader);
        GenericRecord record = null;
        while (fileReader.hasNext()) {
            record = fileReader.next();
            // log.info("-> {},", record);
            log.info("-> {},", GenericData.get().toString(record));
        }
        if (record != null) {
            for (Schema.Field field : record.getSchema().getFields()) {
                log.info("{}={}", field.name(), record.get(field.name()));
            }
        }
        fileReader.close();
    }
}
