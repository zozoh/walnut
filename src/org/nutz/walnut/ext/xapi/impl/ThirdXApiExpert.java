package org.nutz.walnut.ext.xapi.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Files;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.xapi.bean.ThirdXRequest;

public class ThirdXApiExpert {

    private String name;

    private String base;

    private Map<String, ThirdXRequest> requests;

    public ThirdXApiExpert() {
        this(Files.findFile("org/nutz/walnut/ext/xapi/data"));
    }

    public ThirdXApiExpert(File dir) {
        requests = new HashMap<>();

        // 列出目录
        if (null != dir) {
            File[] files = dir.listFiles();
            for (File f : files) {

            }
        }
    }

    public ThirdXRequest check(String path) {
        ThirdXRequest req = requests.get(path);
        if (null != req) {
            req = req.clone();
            return req;
        }
        throw Er.createf("e.thirdx.InvalidPath", "[%s] %s : %s", name, base, path);
    }

}
