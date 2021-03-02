package org.nutz.walnut.ext.xapi.impl;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.walnut.ext.xapi.bean.ThirdXExpert;

public class DefaultThirdXExpertManager extends AbstractThirdXExpertManager {

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

    public DefaultThirdXExpertManager() {
        super();
    }

    public DefaultThirdXExpertManager(List<File> files) {
        super();

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

}
