package org.nutz.walnut.ext.data.www.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PriceRuleSet {

    /**
     * 一个标称的零售价
     */
    private float retailPrice;

    /**
     * 动态价格规则映射表
     */
    private Map<String, PriceRule> map;

    public PriceRuleSet() {
        map = new HashMap<>();
    }

    public float getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(float retailPrice) {
        this.retailPrice = retailPrice;
    }

    public PriceRule getRule(String ruleName) {
        return map.get(ruleName);
    }

    public void addRule(PriceRule rule) {
        if (null != rule) {
            map.put(rule.getName(), rule);
        }
    }

    public void addRules(Collection<PriceRule> rules) {
        if (null != rules && !rules.isEmpty()) {
            for (PriceRule rule : rules) {
                this.addRule(rule);
            }
        }
    }

}
