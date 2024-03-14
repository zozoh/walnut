package com.site0.walnut.ext.sys.cron.hdl;

import org.nutz.json.Json;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.cron.WnSysCron;
import com.site0.walnut.ext.sys.cron.WnSysCronApi;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

@JvmHdlParamArgs("cqn")
public class cron_add implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 创建定期任务
        WnSysCron cron = new WnSysCron();
        cron.setCron(hc.params.val_check(0));

        // 准备命令内容
        String command;
        if (hc.params.has("f")) {
            String ph = hc.params.getString("f");
            WnObj o = Wn.checkObj(sys, ph);
            command = sys.io.readText(o);
        }
        // 从标准输入读取
        else {
            command = sys.in.readAll();
        }

        // 防守
        if (Ws.isBlank(command)) {
            return;
        }
        cron.setCommand(command);

        // 准备命令的操作用户
        WnAccount me = sys.getMe();
        if (hc.params.hasString("u") && sys.auth.isMemberOfGroup(me, "root")) {
            String userName = hc.params.getString("u");
            me = sys.auth.checkAccount(userName);
        }
        cron.setUser(me.getName());

        // 准备服务类
        WnSysCronApi cronApi = sys.services.getCronApi();

        // 切换账号 & 创建任务
        cronApi.addCron(cron);

        // 输出内容
        String json = Json.toJson(cron.getMeta(), hc.jfmt);
        sys.out.println(json);
    }

}
