package org.nutz.walnut.web;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.web.WebConfig;

public class WnConfig extends WebConfig {

    public WnConfig(String path) {
        super(Streams.fileInr(path));
        putAll(System.getProperties());
        putAll(System.getenv());
    }

    public String[] getWebIocPkgs() {
        String str = this.get("web-ioc-pkgs");
        if (Strings.isBlank(str))
            return new String[0];
        return Strings.splitIgnoreBlank(str, "\n");
    }

    public String[] getWebModulePkgs() {
        String str = this.get("web-module-pkgs");
        if (Strings.isBlank(str))
            return new String[0];
        return Strings.splitIgnoreBlank(str, "\n");
    }

    public String[] getInitSetup() {
        String str = this.get("init-setup");
        if (Strings.isBlank(str))
            return new String[0];
        return Strings.splitIgnoreBlank(str, "\n");
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

    public WnObj getRootTreeNode() {
        String id = this.get("root-id");

        WnObj o = new WnBean();
        o.id(id);
        o.path("/");
        o.race(WnRace.DIR);
        o.name("");
        o.lastModified(System.currentTimeMillis());
        o.createTime(System.currentTimeMillis());
        o.creator("root").mender("root").group("root");
        o.mode(0755);

        return o;
    }

    public NutMap getEntryPages() {
        String json = this.get("entry-pages", "{}");
        return Json.fromJson(NutMap.class, json);
    }

    public File getBucketHome() {
        String path = get("bucket-home");
        return Files.createDirIfNoExists(path);
    }

}
