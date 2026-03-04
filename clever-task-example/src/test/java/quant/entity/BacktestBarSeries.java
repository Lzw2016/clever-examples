package quant.entity;

import lombok.Data;

import java.util.Date;

/**
 * 回测bar序列配置
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/04 21:06 <br/>
 */
@Data
public class BacktestBarSeries {
    /**
     * 数据id
     */
    private Long id;
    /**
     * 回撤id
     */
    private Long backtestRecordId;
    /**
     * 是否是交易目标bar(0否 / 1是)
     */
    private Integer main;
    /**
     * 数据源
     */
    private String source;
    /**
     * 数据表
     */
    private String tableName;
    /**
     * 数据开始时间
     */
    private Date startTime;
    /**
     * 数据结束时间
     */
    private Date endTime;
    /**
     * 金融产品编码
     */
    private String code;
    /**
     * 品种名称
     */
    private String name;
    /**
     * 周期
     */
    private String period;
    /**
     * 总数据量
     */
    private Long count;
    /**
     * 滑动窗口大小
     */
    private Integer slidingWindow;
    /**
     * 扩展数据
     */
    private String extData;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 删除标识(0正常 / 1删除)
     */
    private Integer delFlag;
}
