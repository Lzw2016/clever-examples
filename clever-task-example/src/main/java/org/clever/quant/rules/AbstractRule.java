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
    protected final Set<Indicator<?>> indicators = new HashSet<>();
    protected final Set<BarSeries> allBarSeries = new HashSet<>();

    public AbstractRule(String name) {
        this.name = name == null ? getClass().getSimpleName() : name;
    }

    /**
     * 增加与当前规则相关的 BarSeries
     */
    private void addIndicators(Indicator<?>... indicators) {
        if (indicators != null) {
            for (Indicator<?> item : indicators) {
                if (item != null) {
                    this.indicators.add(item);
                }
            }
        }
    }

    /**
     * 增加与当前规则相关的 BarSeries
     */
    private void addBarSeries(BarSeries... barSeries) {
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
    protected void dependencies(Rule... rules) {
        if (rules != null) {
            for (Rule rule : rules) {
                if (rule != null) {
                    addIndicators(rule.getIndicators().toArray(new Indicator[0]));
                    addBarSeries(rule.getAllBarSeries().toArray(new BarSeries[0]));
                }
            }
        }
    }

    /**
     * 增加与当前规则相关的 BarSeries, BarSeries从Indicator对象中获取
     */
    protected void dependencies(Indicator<?>... indicators) {
        if (indicators != null) {
            addIndicators(indicators);
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
    public Set<Indicator<?>> getIndicators() {
        return Collections.unmodifiableSet(indicators);
    }

    @Override
    public Set<BarSeries> getAllBarSeries() {
        return Collections.unmodifiableSet(allBarSeries);
    }
}
