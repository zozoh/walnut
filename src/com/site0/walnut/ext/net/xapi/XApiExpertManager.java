package com.site0.walnut.ext.net.xapi;

import java.util.Map;

import com.site0.walnut.ext.net.xapi.bean.XApiExpert;

public interface XApiExpertManager {

    void addExpert(String apiName, XApiExpert expert);

    XApiExpert getExpert(String apiName);

    XApiExpert checkExpert(String apiName);

    Map<String, XApiExpert> getExperts();

    void setExperts(Map<String, XApiExpert> experts);

}