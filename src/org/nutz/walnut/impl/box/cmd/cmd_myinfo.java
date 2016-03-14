package org.nutz.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_myinfo extends JvmExecutor {

    public void exec(final WnSystem sys, final String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqn");
        JsonFormat fmt = this.gen_json_format(params);
        WnUsr usr = sys.usrService.fetch(sys.se.me());
        usr.password("********");
        usr.salt("???");
        sys.out.println(Json.toJson(usr, fmt));
    }

}
