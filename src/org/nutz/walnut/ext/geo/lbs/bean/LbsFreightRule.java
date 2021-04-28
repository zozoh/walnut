package org.nutz.walnut.ext.geo.lbs.bean;

import org.nutz.json.JsonField;

/**
 * 运费规则
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class LbsFreightRule {

    /**
     * 规则标题，仅仅是助记用的
     */
    private String title;

    /**
     * 发货地（六位地址编码）<br>
     * !注: 000000 表示全国
     */
    @JsonField("ship_code")
    private String shipCode;

    /**
     * 目的地址（六位地址编码）<br>
     * !注: 000000 表示全国
     */
    @JsonField("target_code")
    private String targetCode;

    /**
     * 首重价格（元）
     */
    private float first;

    /**
     * 续重价格（元/续重单位）
     */
    private float additional;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShipCode() {
        return shipCode;
    }

    public void setShipCode(String shipCode) {
        this.shipCode = shipCode;
    }

    public String getTargetCode() {
        return targetCode;
    }

    public void setTargetCode(String targetCode) {
        this.targetCode = targetCode;
    }

    public float getFirst() {
        return first;
    }

    public void setFirst(float first) {
        this.first = first;
    }

    public float getAdditional() {
        return additional;
    }

    public void setAdditional(float additional) {
        this.additional = additional;
    }

}
