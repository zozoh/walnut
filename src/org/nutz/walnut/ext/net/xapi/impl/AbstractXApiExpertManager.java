package org.nutz.walnut.ext.net.xapi.impl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.net.xapi.XApiExpertManager;
import org.nutz.walnut.ext.net.xapi.bean.XApiExpert;

public class AbstractXApiExpertManager implements XApiExpertManager {

    protected Map<String, XApiExpert> experts;

    public AbstractXApiExpertManager() {
        experts = new HashMap<>();
    }

    @Override
    public void addExpert(String apiName, XApiExpert expert) {
        experts.put(apiName, expert);
    }

    @Override
    public XApiExpert getExpert(String apiName) {
        return this.experts.get(apiName);
    }

    @Override
    public XApiExpert checkExpert(String apiName) {
        XApiExpert expert = this.getExpert(apiName);
        if (null == expert) {
            throw Er.createf("e.ThirdXApi.InvalidApiName", apiName);
        }
        return expert;
    }

    @Override
    public Map<String, XApiExpert> getExperts() {
        return experts;
    }

    @Override
    public void setExperts(Map<String, XApiExpert> experts) {
        this.experts = experts;
    }

}