package org.nutz.walnut.ext.titanium.builder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.builder.bean.TiBuildEntry;
import org.nutz.walnut.ext.titanium.builder.bean.TiExportItem;

public abstract class TiJoinAction {

    protected WnIo io;

    protected TiBuildEntry entry;

    protected List<String> outputs;

    protected Map<String, TiExportItem> exportMap;

    protected Set<String> depss;

    public TiJoinAction(WnIo io,
                        TiBuildEntry entry,
                        List<String> outputs,
                        Map<String, TiExportItem> exportMap,
                        Set<String> depss) {
        this.io = io;
        this.entry = entry;
        this.outputs = outputs;
        this.exportMap = exportMap;
        this.depss = depss;
    }

    public abstract void exec(String url, String[] lines, WnObj f) throws Exception;

}