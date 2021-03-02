package org.nutz.walnut.ext.xapi.impl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.xapi.ThirdXExpertManager;
import org.nutz.walnut.ext.xapi.bean.ThirdXExpert;

public class AbstractThirdXExpertManager implements ThirdXExpertManager {

    protected Map<String, ThirdXExpert> experts;

    public AbstractThirdXExpertManager() {
        experts = new HashMap<>();
    }

    @Override
    public void addExpert(String apiName, ThirdXExpert expert) {
        experts.put(apiName, expert);
    }

    @Override
    public ThirdXExpert getExpert(String apiName) {
        return this.experts.get(apiName);
    }

    @Override
    public ThirdXExpert checkExpert(String apiName) {
        ThirdXExpert expert = this.getExpert(apiName);
        if (null == expert) {
            throw Er.createf("e.ThirdXApi.InvalidApiName", apiName);
        }
        return expert;
    }

    @Override
    public Map<String, ThirdXExpert> getExperts() {
        return experts;
    }

    @Override
    public void setExperts(Map<String, ThirdXExpert> experts) {
        this.experts = experts;
    }

}