package org.nutz.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_sys extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        NutMap conf = Wn.getSysConf(sys.io);
        sys.out.println(Json.toJson(conf, JsonFormat.nice()));
    }

}
