package org.nutz.walnut.web;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.impl.io.local.LocalWnNode;
import org.nutz.walnut.impl.io.mongo.MongoWnNode;
import org.nutz.web.WebConfig;

public class WnConfig extends WebConfig {

    public WnConfig(String path) {
        super(path);
    }

    public String[] getJvmboxPkgs() {
        String str = this.get("jvmbox-pkgs");
        if (Strings.isBlank(str))
            return new String[0];
        return Strings.splitIgnoreBlank(str, "\n");
    }

    public List<WnInitMount> getInitMount() {
        List<WnInitMount> list = new LinkedList<WnInitMount>();
        String str = this.get("init-mnt");
        if (!Strings.isBlank(str)) {
            String[] lines = Strings.splitIgnoreBlank(str, "\n");
            for (String line : lines) {
                list.add(new WnInitMount(line));
            }
        }
        return list;
    }

    public NutMap getInitUsrEnvs() {
        String str = this.get("init-usr-envs");
        if (Strings.isBlank(str))
            return new NutMap();
        return Json.fromJson(NutMap.class, str);
    }

    public WnNode getRootTreeNode() {
        String id = this.get("root-id");
        String mnt = this.get("root-mnt");

        // 本地
        if (mnt.startsWith("file://")) {
            String localPath = mnt.substring("file://".length());
            File d = Files.createDirIfNoExists(localPath);

            LocalWnNode nd = new LocalWnNode(d);
            nd.id(id);
            nd.path("/");
            nd.mount(mnt);
            return nd;
        }
        // Mongo
        if (mnt.startsWith("mongo:")) {
            MongoWnNode nd = new MongoWnNode();
            nd.id(id);
            nd.path("/");
            nd.mount(mnt);
            nd.name(mnt);
            return nd;
        }
        throw Lang.impossible();
    }

}
