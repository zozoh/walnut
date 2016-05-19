package org.nutz.walnut.ext.noti.impl;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.noti.WnNotiHandler;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class WnXXPushNotiHandler implements WnNotiHandler {

    @Override
    public NutMap add(WnSystem sys, ZParams params) {
        NutMap meta = new NutMap();

        meta.put("noti_target", params.check("to"));
        if (params.has("provider"))
            meta.put("noti_push_provider", params.get("provider"));
        meta.put("noti_push_text", Cmds.checkParamOrPipe(sys, params, "text", true));
        if (params.has("platform"))
            meta.put("noti_push_platform", params.get("platform"));

        return meta;
    }

    @Override
    public String send(WnSystem sys, WnObj oN) {
        String push_alias = oN.getString("noti_target");
        String provider = oN.getString("noti_push_provider");
        String text = oN.getString("noti_push_text");
        String platform = oN.getString("noti_push_platform");

        String cmdText = String.format("push -r '%s'", push_alias);

        if (!Strings.isBlank(provider))
            cmdText += " -provider '" + provider + "'";
        if (!Strings.isBlank(platform))
            cmdText += " -platform '" + platform + "'";

        String re = sys.exec2(cmdText, text);

        return (null != re && re.startsWith("e.cmd")) ? re : null;
    }

}

