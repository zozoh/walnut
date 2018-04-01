package org.nutz.walnut.ext.whoisx;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class cmd_whoisx extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        ZParams params = ZParams.parse(args, "cqn");

        String host = params.val_check(0);
        WhoInfo info = WhoisX.query(host);

        JsonFormat jfmt = Cmds.gen_json_format(params);

        sys.out.println(Json.toJson(info, jfmt));

    }

}
