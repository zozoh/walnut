package org.nutz.walnut.ext.geo.lbs.bean;

import org.nutz.lang.Nums;

public class LbsFreight {

    public static class Weight {
        /**
         * 首重（公斤）
         */
        public float first;
        /**
         * 续重（公斤）
         */
        public float additional;
    }

    /**
     * 重量分析
     */
    private Weight weight;

    /**
     * 相关运费计算规则
     */
    private LbsFreightRule rule;

    /**
     * 首重价格（元）
     */
    private float first;

    /**
     * 续重总价格（元）
     */
    private float additional;

    /**
     * 总运费
     */
    private float total;

    public Weight getWeight() {
        return weight;
    }

    public void setWeight(Weight weight) {
        this.weight = weight;
    }

    public LbsFreightRule getRule() {
        return rule;
    }

    public void setRule(LbsFreightRule rule) {
        this.rule = rule;
    }

    public float getFirst() {
        return first;
    }

    public void setFirst(float first) {
        this.first = Nums.precision(first, 2);
    }

    public float getAdditional() {
        return additional;
    }

    public void setAdditional(float additional) {
        this.additional = Nums.precision(additional, 2);
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = Nums.precision(total, 2);
    }

}
