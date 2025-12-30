package tmp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.openhft.chronicle.wire.SelfDescribingMarshallable;

import java.util.List;
import java.util.Map;

/**
 * 作者：lizw <br/>
 * 创建时间：2025/12/29 13:02 <br/>
 */
@Getter
@Setter
@ToString
public class MyData extends SelfDescribingMarshallable {
    private Map<String, Object> dbData;
    private List<Object> values;
    private List<Map<String, Object>> dbDatas;
}
