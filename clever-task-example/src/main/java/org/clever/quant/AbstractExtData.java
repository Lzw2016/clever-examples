package org.clever.quant;

import lombok.Getter;
import org.clever.core.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * 扩展数据抽象类
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/04 13:22 <br/>
 */
@Getter
public abstract class AbstractExtData {
    /**
     * 其它扩展数据
     */
    private final Map<String, Object> extData = new HashMap<>();

    /**
     * 增加扩展数据
     *
     * @param name 数据名称
     * @param val  数据值
     */
    public void addExtData(String name, Object val) {
        Assert.isNotBlank(name, "参数 name 不能为空");
        if (val == null) {
            extData.remove(name);
        } else {
            extData.put(name, val);
        }
    }

    /**
     * 移除扩展数据
     *
     * @param name 数据名称
     */
    public void removeExtData(String name) {
        extData.remove(name);
    }

    /**
     * 获取扩展数据
     *
     * @param name 数据名称
     */
    public Object getExtData(String name) {
        return extData.get(name);
    }
}
