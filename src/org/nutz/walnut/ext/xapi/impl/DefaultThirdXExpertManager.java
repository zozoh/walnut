package org.nutz.walnut.ext.xapi.impl;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.xapi.ThirdXExpertManager;
import org.nutz.walnut.ext.xapi.bean.ThirdXExpert;

public class DefaultThirdXExpertManager implements ThirdXExpertManager {

    private static DefaultThirdXExpertManager ONE;

    public static DefaultThirdXExpertManager getInstance() {
        if (ONE == null) {
            synchronized (DefaultThirdXExpertManager.class) {
                if (ONE == null) {
                    String base = "org/nutz/walnut/ext/xapi/data/";
                    List<File> files = new LinkedList<>();
                    files.add(Files.findFile(base + "tianyancha.json"));
                    files.add(Files.findFile(base + "weixin.json"));
                    ONE = new DefaultThirdXExpertManager(files);
                }
            }
        }
        return ONE;
    }

    private Map<String, ThirdXExpert> experts;

    public DefaultThirdXExpertManager() {
        experts = new HashMap<>();
    }

    public DefaultThirdXExpertManager(List<File> files) {
        this();

        for (File f : files) {
            if (f.isHidden()) {
                continue;
            }
            if (!f.getName().endsWith(".json")) {
                continue;
            }
            this.addExpert(f);
        }
    }

    public DefaultThirdXExpertManager(File... files) {
        this();

        for (File f : files) {
            if (f.isHidden()) {
                continue;
            }
            if (!f.getName().endsWith(".json")) {
                continue;
            }
            this.addExpert(f);
        }
    }

    @Override
    public void addExpert(File f) {
        String apiName = Files.getMajorName(f);
        addExpert(f, apiName);
    }

    private void addExpert(File f, String apiName) {
        String json = Files.read(f);
        ThirdXExpert expert = Json.fromJson(ThirdXExpert.class, json);
        expert.setName(apiName);
        this.addExpert(apiName, expert);
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
