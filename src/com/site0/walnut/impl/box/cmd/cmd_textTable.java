package com.site0.walnut.impl.box.cmd;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;

public class cmd_textTable extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "bish");

        // 得到输入
        String input = sys.in.readAll();

        List<NutMap> list = Json.fromJsonAsList(NutMap.class, input);

        // 输出
        Cmds.output_beans(sys, params, null, list);
    }

}
