package org.nutz.walnut.ext.xapi;

import java.io.File;
import java.util.Map;

import org.nutz.walnut.ext.xapi.bean.ThirdXExpert;

public interface ThirdXExpertManager {

    void addExpert(File f);

    void addExpert(String apiName, ThirdXExpert expert);

    ThirdXExpert getExpert(String apiName);

    ThirdXExpert checkExpert(String apiName);

    Map<String, ThirdXExpert> getExperts();

    void setExperts(Map<String, ThirdXExpert> experts);

}