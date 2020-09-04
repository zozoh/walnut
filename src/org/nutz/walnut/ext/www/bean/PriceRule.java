package org.nutz.walnut.ext.www.bean;

public class PriceRule {

    private String name;
    private String title;
    private PriceRuleItem[] rules;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PriceRuleItem[] getRules() {
        return rules;
    }

    public void setRules(PriceRuleItem[] rules) {
        this.rules = rules;
    }

    public PriceRuleItem matchItem(WnProduct pro) {
        if (null != rules) {
            for (PriceRuleItem ri : rules) {
                if (ri.isMatch(pro))
                    return ri;
            }
        }
        return null;
    }

}
