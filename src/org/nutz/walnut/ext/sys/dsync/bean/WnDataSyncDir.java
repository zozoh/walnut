package org.nutz.walnut.ext.sys.dsync.bean;

import org.nutz.walnut.util.Ws;

public class WnDataSyncDir {

    private String key;

    private String path;

    private boolean leafOnly;

    private boolean ignoreThingSet;

    private boolean ignoreTop;

    private boolean ignoreHidden;

    public boolean hasKey() {
        return !Ws.isBlank(key);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isLeafOnly() {
        return leafOnly;
    }

    public void setLeafOnly(boolean leafOnly) {
        this.leafOnly = leafOnly;
    }

    public boolean isIgnoreThingSet() {
        return ignoreThingSet;
    }

    public void setIgnoreThingSet(boolean ignoreThingSet) {
        this.ignoreThingSet = ignoreThingSet;
    }

    public boolean isIgnoreTop() {
        return ignoreTop;
    }

    public void setIgnoreTop(boolean ignoreTop) {
        this.ignoreTop = ignoreTop;
    }

    public boolean isIgnoreHidden() {
        return ignoreHidden;
    }

    public void setIgnoreHidden(boolean ignoreHidden) {
        this.ignoreHidden = ignoreHidden;
    }

}
