package com.site0.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;

public class cmd_session extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqn");
        JsonFormat jfmt = Cmds.gen_json_format(params);
        NutBean se = sys.session.toBean();
        String json = Json.toJson(se, jfmt);
        sys.out.println(json);
    }

}
