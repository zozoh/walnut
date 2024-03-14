package com.site0.walnut.ext.net.whoisx;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;

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
