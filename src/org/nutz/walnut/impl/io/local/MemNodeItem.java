package org.nutz.walnut.impl.io.local;

import org.nutz.lang.Strings;

public class MemNodeItem {

    public MemNodeItem() {}

    public MemNodeItem(MemNodeItem prev, String str) {
        int pos = str.indexOf(':');
        id = str.substring(0, pos);

        // 拆分 mount
        int pos2 = str.indexOf(">>", pos + 1);
        if (pos2 > 0) {
            path = str.substring(pos + 1, pos2);
            mount = str.substring(pos2 + 2);
        }
        // 没有 mount
        else {
            path = str.substring(pos + 1);
            mount = null;
        }

        this.prev = prev;
        prev.next = this;
    }

    public String id;

    public String path;

    public String mount;

    public MemNodeItem prev;

    public MemNodeItem next;

    public void remove() {
        if (null != prev) {
            prev.next = this.next;
        }

        if (null != next) {
            next.prev = this.prev;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(id);
        sb.append(':').append(path);
        if (!Strings.isBlank(mount)) {
            sb.append(">>").append(mount);
        }
        return sb.toString();
    }

}
