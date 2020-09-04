package org.nutz.walnut.ext.lbs.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.json.JsonField;

/**
 * 运费表
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class LbsFreightSheet {

    /**
     * 首重重量（公斤）默认 1
     */
    @JsonField("first_weight")
    private float firstWeight;

    /**
     * 续重的单位（公斤）默认 1
     */
    @JsonField("additional_unit")
    private float additional_unit;

    /**
     * 规则列表
     * <p>
     * 键：国家编码（例如 CN），值：运费规则数组
     */
    private Map<String, LbsFreightRule[]> rules;

    public List<LbsFreightRule> getCandidateRules(String country,
                                                  LbsChinaAddr fromAddr,
                                                  LbsChinaAddr toAddr) {
        LbsFreightRule[] coRules = rules.get(country);
        if (null == coRules || coRules.length == 0) {
            return new LinkedList<>();
        }

        List<LbsFreightRuleMatch> matchs = new LinkedList<>();

        if (null == fromAddr || null == toAddr) {
            return new LinkedList<>();
        }

        for (LbsFreightRule rule : coRules) {
            LbsFreightRuleMatch rm = LbsFreightRuleMatch.test(rule, fromAddr, toAddr);
            if (null != rm) {
                matchs.add(rm);
            }
        }
        // 排序
        Collections.sort(matchs);

        // 最后得到列表
        List<LbsFreightRule> list = new ArrayList<>(matchs.size());
        for (LbsFreightRuleMatch rm : matchs) {
            list.add(rm.getRule());
        }

        return list;
    }

    public List<LbsFreightRule> getCandidateRules(String country,
                                                  String fromAddrCode,
                                                  String toAddrCode) {

        LbsChina china = LbsChina.getInstance();
        LbsChinaAddr fromAddr = china.getAddress(fromAddrCode);
        LbsChinaAddr toAddr = china.getAddress(toAddrCode);

        return getCandidateRules(country, fromAddr, toAddr);
    }

    public LbsFreightRule findRule(String country, String fromAddr, String toAddr) {
        List<LbsFreightRule> list = this.getCandidateRules(country, fromAddr, toAddr);
        if (!list.isEmpty())
            return list.get(0);
        return null;
    }

    /**
     * 根据规则，计算某重量下的价格
     * 
     * @param rule
     *            相关规则
     * @param proWeight
     *            货运总重
     * 
     * @return 某规则下对应货运总重的价格
     */
    public LbsFreight calculatePrice(LbsFreightRule rule, float proWeight) {
        // 准备返回值
        LbsFreight fp = new LbsFreight();

        // 分析重量: 剩余部分作为续重
        LbsFreight.Weight wei = new LbsFreight.Weight();
        wei.first = Math.min(proWeight, this.firstWeight);
        wei.additional = proWeight - this.firstWeight;

        // 多少个续重单位
        float n = 0;
        if (wei.additional > 0 && this.additional_unit > 0) {
            n = wei.additional / this.additional_unit;
        }

        // 记录重量和规则
        fp.setWeight(wei);
        fp.setRule(rule);

        // 计算价格
        float f = rule.getFirst();
        float a = n * rule.getAdditional();
        fp.setFirst(f);
        fp.setAdditional(a);
        fp.setTotal(f + a);

        return fp;
    }

    public float getFirstWeight() {
        return firstWeight;
    }

    public void setFirstWeight(float firstWeight) {
        this.firstWeight = firstWeight;
    }

    public float getAdditional_unit() {
        return additional_unit;
    }

    public void setAdditional_unit(float additional_unit) {
        this.additional_unit = additional_unit;
    }

    public Map<String, LbsFreightRule[]> getRules() {
        return rules;
    }

    public void setRules(Map<String, LbsFreightRule[]> rules) {
        this.rules = rules;
    }

}
