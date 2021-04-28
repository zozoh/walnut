package org.nutz.walnut.ext.geo.lbs.bean;

public class LbsFreightRuleMatch implements Comparable<LbsFreightRuleMatch> {

    public static LbsFreightRuleMatch test(LbsFreightRule rule,
                                           LbsChinaAddr fromAddr,
                                           LbsChinaAddr toAddr) {
        // 首先确保是个合法地址
        if (null == fromAddr) {
            return null;
        }

        // 首先确保是个合法地址
        if (null == toAddr) {
            return null;
        }

        // 匹配发货地
        int ml_ship = fromAddr.match(rule.getShipCode());
        if (ml_ship < 0)
            return null;

        int ml_target = toAddr.match(rule.getTargetCode());
        if (ml_target < 0)
            return null;

        return new LbsFreightRuleMatch(rule, ml_ship, ml_target);
    }

    private LbsFreightRule rule;

    /**
     * 发货地匹配级别：
     * <p>
     * 3: 全匹配, 2: 匹配到市, 1:匹配到省, 0 不匹配
     */
    private int shipMatch;

    private int targetMatch;

    /**
     * 匹配的权重
     */
    private int matchValue;

    private LbsFreightRuleMatch(LbsFreightRule rule, int shipMatch, int targetMatch) {
        this.rule = rule;
        this.shipMatch = shipMatch;
        this.targetMatch = targetMatch;
        this.matchValue = shipMatch + targetMatch;
    }

    public LbsFreightRule getRule() {
        return rule;
    }

    public void setRule(LbsFreightRule rule) {
        this.rule = rule;
    }

    public int getShipMatch() {
        return shipMatch;
    }

    public void setShipMatch(int level) {
        this.shipMatch = level;
    }

    public int getTargetMatch() {
        return targetMatch;
    }

    public void setTargetMatch(int targetMatch) {
        this.targetMatch = targetMatch;
    }

    public int getMatchValue() {
        return matchValue;
    }

    public void setMatchValue(int matchValue) {
        this.matchValue = matchValue;
    }

    @Override
    public int compareTo(LbsFreightRuleMatch o) {
        return o.matchValue - this.matchValue;
    }

    public String toString() {
        return String.format("'%s'%d->%d", rule.getTitle(), this.shipMatch, this.targetMatch);
    }

}
