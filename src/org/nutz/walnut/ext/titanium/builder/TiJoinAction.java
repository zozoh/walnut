package org.nutz.walnut.ext.titanium.builder;

import java.util.List;

import org.nutz.walnut.ext.titanium.builder.bean.TiBuildEntry;

public abstract class TiJoinAction {

    protected TiBuildEntry entry;

    protected List<String> outputs;

    public TiJoinAction(TiBuildEntry entry, List<String> outputs) {
        this.entry = entry;
        this.outputs = outputs;
    }

    public abstract void exec(String url, String[] lines) throws Exception;

}
