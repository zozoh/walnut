package org.nutz.walnut.web;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

public class WnInitMount {

    public String path;

    public String mount;

    public WnInitMount(String line) {
        int pos = line.indexOf(':');
        if (pos <= 0) {
            throw Lang.makeThrow("init mount invalid line input: %s", line);
        }
        path = Strings.trim(line.substring(0, pos));
        mount = Strings.trim(line.substring(pos + 1));
    }

    public String toString() {
        return path + " : " + mount;
    }

}
