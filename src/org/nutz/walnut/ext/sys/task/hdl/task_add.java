package org.nutz.walnut.ext.sys.task.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.ext.sys.task.WnSysTaskApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

@JvmHdlParamArgs("cqn")
public class task_add implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 创建任务
        WnObj oTask = new WnIoObj();

        // 设置任务命令
        String cmdText = null;

        // 从标准输入读取
        if (hc.params.vals.length == 0) {
            String str = sys.in.readAll();
            cmdText = Ws.trim(str);
        }
        // 读取内容
        else {
            cmdText = Ws.join(hc.args, " ");
        }

        // 防守
        if (Ws.isBlank(cmdText)) {
            return;
        }

        // 记入命令
        oTask.put("command", cmdText);

        // 读取命令输入
        byte[] input = null;

        // 从文件读取
        if (hc.params.has("f")) {
            String ph = hc.params.get("f");
            WnObj oInput = Wn.checkObj(sys, ph);
            input = sys.io.readBytes(oInput);
        }
        // 指定了参数
        else if (hc.params.has("input")) {
            String is = hc.params.getString("input");
            if (Ws.isBlank(is)) {
                is = sys.in.readAll();
            }
            if (!Ws.isBlank(is)) {
                input = is.getBytes(Encoding.CHARSET_UTF8);
            }
        }

        // 准备命令的操作用户
        WnAccount me = sys.getMe();
        if (hc.params.hasString("u") && sys.auth.isMemberOfGroup(me, "root")) {
            String userName = hc.params.getString("u");
            me = sys.auth.checkAccount(userName);
        }
        // 如果是域子账号，则用域主账号的名称
        if (me.isSysAccount()) {
            oTask.put("user", me.getName());
        } else {
            oTask.put("user", me.getGroupName());
        }

        // 准备服务类
        WnSysTaskApi taskApi = sys.services.getTaskApi();

        // 切换账号 & 创建任务
        taskApi.addTask(oTask, input);

        // 输出内容
        String json = Json.toJson(oTask, hc.jfmt);
        sys.out.println(json);
    }

}
