package com.site0.walnut.web;

import java.io.File;
import java.io.IOException;

import com.site0.walnut.util.Ws;
import com.site0.walnut.log.WnLogs;

import com.site0.walnut.util.Wlang;

public class WnInitMount {

    public String path;

    public String mount;

    public WnInitMount(String line) {
        int pos = line.indexOf(':');
        if (pos <= 0) {
            throw Wlang.makeThrow("init mount invalid line input: %s", line);
        }
        path = Ws.trim(line.substring(0, pos));
        mount = Ws.trim(line.substring(pos + 1));
        if (mount.startsWith("file://.")) {
            try {
                mount = "file://" + new File(mount.substring("file://".length())).getCanonicalPath();
            }
            catch (IOException e) {
                WnLogs.get().info("bad mount path    " + line, e);
            }
        }
    }

    public String toString() {
        return path + " : " + mount;
    }
}
