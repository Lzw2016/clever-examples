package org.clever.quant.rules;

import org.clever.quant.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 14:06 <br/>
 */
public abstract class AbstractRule implements Rule {
    protected final transient Logger log = LoggerFactory.getLogger(getClass());
    protected final String name;

    public AbstractRule() {
        this(null);
    }

    public AbstractRule(String name) {
        this.name = name == null ? getClass().getSimpleName() : name;
    }

    @Override
    public String getName() {
        return name;
    }
}
