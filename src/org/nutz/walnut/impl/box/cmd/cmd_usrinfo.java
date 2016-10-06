package org.nutz.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_usrinfo extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);

        WnUsr u;
        String clientName = params.val(0);
        if (Strings.isBlank(clientName)) {
            u = sys.me;
        } else {
            u = sys.nosecurity(new Proton<WnUsr>() {
                protected WnUsr exec() {
                    return sys.usrService.check(clientName);
                }
            });
        }

        NutMap map = new NutMap();
        map.put("id", u.id());
        map.put("name", u.name());

        sys.out.println(Json.toJson(map, JsonFormat.compact()));

    }

}
