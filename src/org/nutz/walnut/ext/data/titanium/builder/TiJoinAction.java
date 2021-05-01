package org.nutz.walnut.ext.data.titanium.builder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.titanium.builder.bean.TiBuildEntry;
import org.nutz.walnut.ext.data.titanium.builder.bean.TiExportItem;

public abstract class TiJoinAction {

    protected WnIo io;

    protected TiBuildEntry entry;

    protected List<String> outputs;

    protected Map<String, TiExportItem> exportMap;

    protected Set<String> depss;

    protected Map<String, Integer> importCount;

    public TiJoinAction(WnIo io,
                        TiBuildEntry entry,
                        List<String> outputs,
                        Map<String, TiExportItem> exportMap,
                        Set<String> depss,
                        Map<String, Integer> importCount) {
        this.io = io;
        this.entry = entry;
        this.outputs = outputs;
        this.exportMap = exportMap;
        this.depss = depss;
        this.importCount = importCount;
    }

    public abstract void exec(String url, String[] lines, WnObj f) throws Exception;

}
