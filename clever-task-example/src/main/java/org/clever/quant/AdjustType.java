package org.clever.quant;

import lombok.Getter;
import lombok.ToString;

/**
 * 复权类型
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/03/13 21:21 <br/>
 */
@ToString
@Getter
public enum AdjustType {
    /**
     * 不复权
     */
    none("none"),
    /**
     * 前复权
     */
    front("front"),
    /**
     * 后复权
     */
    back("back"),
    ;
    private final String name;

    AdjustType(String name) {
        this.name = name;
    }
}
