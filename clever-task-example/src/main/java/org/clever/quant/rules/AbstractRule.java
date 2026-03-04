package org.clever.quant.rules;

import lombok.Getter;
import org.clever.core.id.SnowFlake;
import org.clever.quant.BarSeries;
import org.clever.quant.Indicator;
import org.clever.quant.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 14:06 <br/>
 */
public abstract class AbstractRule implements Rule {
    protected final transient Logger log = LoggerFactory.getLogger(getClass());
    @Getter
    private final long id = SnowFlake.SNOW_FLAKE.nextId();
    protected final String name;
    protected final Set<BarSeries> allBarSeries = new HashSet<>();

    public AbstractRule() {
        this(null);
    }

    public AbstractRule(String name) {
        this.name = name == null ? getClass().getSimpleName() : name;
    }

    /**
     * 增加与当前规则相关的 BarSeries
     */
    protected void addBarSeries(BarSeries... barSeries) {
        if (barSeries != null) {
            for (BarSeries item : barSeries) {
                if (item != null) {
                    allBarSeries.add(item);
                }
            }
        }
    }

    /**
     * 增加与当前规则相关的 BarSeries, BarSeries从Rule对象中获取
     */
    protected void addBarSeries(Rule... rules) {
        if (rules != null) {
            for (Rule rule : rules) {
                if (rule != null) {
                    addBarSeries(rule.getAllBarSeries().toArray(new BarSeries[0]));
                }
            }
        }
    }

    /**
     * 增加与当前规则相关的 BarSeries, BarSeries从Indicator对象中获取
     */
    protected void addBarSeries(Indicator<?>... indicators) {
        if (indicators != null) {
            for (Indicator<?> indicator : indicators) {
                if (indicator != null) {
                    addBarSeries(indicator.getBarSeries());
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<BarSeries> getAllBarSeries() {
        return Collections.unmodifiableSet(allBarSeries);
    }
}
