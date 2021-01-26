package org.nutz.walnut.ext.titanium.builder.action;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.builder.TiJoinAction;
import org.nutz.walnut.ext.titanium.builder.bean.TiBuildEntry;
import org.nutz.walnut.ext.titanium.builder.bean.TiExportItem;
import org.nutz.walnut.util.Ws;

public class TiJoinTiJSON extends TiJoinAction {

    public TiJoinTiJSON(WnIo io,
                        TiBuildEntry entry,
                        List<String> outputs,
                        Map<String, TiExportItem> exportMap,
                        Set<String> depss,
                        Map<String, Integer> importCount) {
        super(io, entry, outputs, exportMap, depss, importCount);
    }

    @Override
    public void exec(String url, String[] lines, WnObj f) throws Exception {
        // Blank JSON File
        if (lines.length == 0) {
            outputs.add("Ti.Preload(\"" + url + "\", '')");
        }
        // Online JSON File
        else if (lines.length == 1) {
            outputs.add("Ti.Preload(\"" + url + "\", " + lines[0] + ")");
        }
        // 输出内容
        else {
            outputs.add("Ti.Preload(\"" + url + "\", " + lines[0]);
            int last = lines.length - 1;
            for (int i = 1; i < last; i++) {
                outputs.add(lines[i]);
            }
            outputs.add(lines[last] + ");");
        }
        // 针对 _com.json 的特殊处理
        if (f.name().equals("_com.json")) {
            String json = Ws.join(lines, "\n");
            NutMap map = Json.fromJson(NutMap.class, json);
            if (map.containsKey("deps")) {
                List<String> list = map.getAsList("deps", String.class);
                depss.addAll(list);
            }
        }
    }

}
