package org.nutz.walnut.ext.net.imap.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.net.imap.ImapContext;
import org.nutz.walnut.ext.net.imap.ImapFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class imap_json extends ImapFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, ImapContext fc, ZParams params) {
        // 防守
        if (!fc.hasList()) {
            sys.out.println("[]");
            return;
        }

        // 准备输出结果
        NutMap re = new NutMap();
        if (fc.hasSummary()) {
            re.put("summary", fc.summary);
        }
        re.put("messages", fc.list);

        // 输出
        JsonFormat jfmt = Cmds.gen_json_format(params);
        String json = Json.toJson(re, jfmt);
        sys.out.println(json);
    }

}
