package org.clever.quant;

import lombok.Getter;
import lombok.ToString;

/**
 * Bar的周期枚举
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/26 18:15 <br/>
 */
@ToString
@Getter
public enum Period {
    _1m("1m"),
    _5m("5m"),
    _15m("15m"),
    _30m("30m"),
    _60m("60m"),
    _1h("1h"),
    _1d("1d"),
    _1w("1w"),
    _1mon("1mon"),
    _1q("1q"),
    _1hy("1hy"),
    _1y("1y"),
    ;
    private final String name;

    Period(String name) {
        this.name = name;
    }
}
