package org.nutz.walnut.ext.net.xapi.impl;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.walnut.ext.net.xapi.bean.XApiExpert;

public class DefaultXApiExpertManager extends AbstractXApiExpertManager {

    private static DefaultXApiExpertManager ONE;

    public static DefaultXApiExpertManager getInstance() {
        if (ONE == null) {
            synchronized (DefaultXApiExpertManager.class) {
                if (ONE == null) {
                    String base = "org/nutz/walnut/ext/net/xapi/data/";
                    List<File> files = new LinkedList<>();
                    files.add(Files.findFile(base + "tianyancha.json"));
                    files.add(Files.findFile(base + "wxgh.json"));
                    files.add(Files.findFile(base + "youtube.json"));
                    files.add(Files.findFile(base + "fb-graph.json"));
                    files.add(Files.findFile(base + "chatgpt.json"));
                    ONE = new DefaultXApiExpertManager(files);
                }
            }
        }
        return ONE;
    }

    public DefaultXApiExpertManager() {
        super();
    }

    public DefaultXApiExpertManager(List<File> files) {
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

    public DefaultXApiExpertManager(File... files) {
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
        XApiExpert expert = Json.fromJson(XApiExpert.class, json);
        expert.setName(apiName);
        this.addExpert(apiName, expert);
    }

}
