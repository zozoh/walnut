package com.site0.walnut.web;

import java.io.File;
import java.io.IOException;

import org.nutz.lang.Strings;
import org.nutz.log.Logs;

import com.site0.walnut.util.Wlang;

public class WnInitMount {

    public String path;

    public String mount;

    public WnInitMount(String line) {
        int pos = line.indexOf(':');
        if (pos <= 0) {
            throw Wlang.makeThrow("init mount invalid line input: %s", line);
        }
        path = Strings.trim(line.substring(0, pos));
        mount = Strings.trim(line.substring(pos + 1));
        if (mount.startsWith("file://.")) {
            try {
                mount = "file://" + new File(mount.substring("file://".length())).getCanonicalPath();
            }
            catch (IOException e) {
                Logs.get().info("bad mount path    " + line, e);
            }
        }
    }

    public String toString() {
        return path + " : " + mount;
    }
}
