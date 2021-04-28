package org.nutz.walnut.ext.net.xapi;

import java.util.Map;

import org.nutz.walnut.ext.net.xapi.bean.ThirdXExpert;

public interface ThirdXExpertManager {

    void addExpert(String apiName, ThirdXExpert expert);

    ThirdXExpert getExpert(String apiName);

    ThirdXExpert checkExpert(String apiName);

    Map<String, ThirdXExpert> getExperts();

    void setExperts(Map<String, ThirdXExpert> experts);

}