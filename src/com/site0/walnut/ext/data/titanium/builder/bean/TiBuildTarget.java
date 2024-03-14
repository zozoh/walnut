package com.site0.walnut.ext.data.titanium.builder.bean;

import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class TiBuildTarget {

    private String path;

    private boolean wrap;

    private String deps;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isWrap() {
        return wrap;
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    public String getDepsPath() {
        if (Ws.isBlank(deps))
            return Wn.appendPath(path, ".deps.json");
        return deps;
    }

    public String getDeps() {
        return deps;
    }

    public void setDeps(String deps) {
        this.deps = deps;
    }

}
