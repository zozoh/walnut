package org.nutz.walnut.impl.box.cmd;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

@IocBean
public class cmd_session extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        // ZParams params = ZParams.parse(args, "^v|clear$");

        sys.out.writeLine(Json.toJson(sys.se));
    }

}
