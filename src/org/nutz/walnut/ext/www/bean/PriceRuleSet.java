package org.nutz.walnut.ext.www.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PriceRuleSet {

    private Map<String, PriceRule> map;

    public PriceRuleSet() {
        map = new HashMap<>();
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
