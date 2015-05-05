package org.nutz.walnut.web;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

public class WnInitMount {

    public String path;

    public String mount;

    public WnInitMount(String line) {
        String[] ss = Strings.splitIgnoreBlank(line, ":");
        if (ss.length != 2) {
            throw Lang.makeThrow("init mount invalid line input: %s", line);
        }
        path = ss[0];
        mount = ss[1];
    }

    public String toString() {
        return path + " : " + mount;
    }

}
