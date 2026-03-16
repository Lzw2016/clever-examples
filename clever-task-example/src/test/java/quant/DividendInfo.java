package quant;

import lombok.Data;

import java.util.Date;

/**
 * 除权数据
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/16 22:13 <br/>
 */
@Data
public class DividendInfo {
    /**
     * 日期时间
     */
    private Date time;
    /**
     * 除权系数
     */
    private Double dr;
}
