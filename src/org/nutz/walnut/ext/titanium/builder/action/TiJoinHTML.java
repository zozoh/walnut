package org.nutz.walnut.ext.titanium.builder.action;

import java.util.List;

import org.nutz.walnut.ext.titanium.builder.TiJoinAction;
import org.nutz.walnut.ext.titanium.builder.bean.TiBuildEntry;

public class TiJoinHTML extends TiJoinAction {

    public TiJoinHTML(TiBuildEntry entry, List<String> outputs) {
        super(entry, outputs);
    }

    @Override
    public void exec(String url, String[] lines) throws Exception {
        // 输出内容
        outputs.add("Ti.Preload(\"" + url + "\", `" + lines[0]);
        int last = lines.length - 1;
        for (int i = 1; i < last; i++) {
            outputs.add(lines[i]);
        }
        outputs.add(lines[last] + "`);");
    }

}
