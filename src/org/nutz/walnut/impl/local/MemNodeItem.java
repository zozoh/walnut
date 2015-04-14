package org.nutz.walnut.impl.local;

import org.nutz.lang.Strings;

public class MemNodeItem {

    public MemNodeItem() {}

    public MemNodeItem(MemNodeItem prev, String str) {
        String[] ss = Strings.splitIgnoreBlank(str, ":");
        id = ss[0];
        path = ss[1];

        this.prev = prev;
        prev.next = this;
    }

    public String id;

    public String path;

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

}
