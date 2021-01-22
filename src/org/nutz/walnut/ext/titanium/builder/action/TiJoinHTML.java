package org.nutz.walnut.ext.titanium.builder.action;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.builder.TiJoinAction;
import org.nutz.walnut.ext.titanium.builder.bean.TiBuildEntry;
import org.nutz.walnut.ext.titanium.builder.bean.TiExportItem;

public class TiJoinHTML extends TiJoinAction {

    public TiJoinHTML(WnIo io,
                      TiBuildEntry entry,
                      List<String> outputs,
                      Map<String, TiExportItem> exportMap,
                      Set<String> depss) {
        super(io, entry, outputs, exportMap, depss);
    }

    @Override
    public void exec(String url, String[] lines, WnObj f) throws Exception {
        // 输出内容
        outputs.add("Ti.Preload(\"" + url + "\", `" + lines[0]);
        int last = lines.length - 1;
        for (int i = 1; i < last; i++) {
            outputs.add(lines[i]);
        }
        outputs.add(lines[last] + "`);");
    }

}
