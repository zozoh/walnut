package org.nutz.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_session extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        // ZParams params = ZParams.parse(args, "^v|clear$");

        sys.out.println(Json.toJson(sys.se));
    }

}
