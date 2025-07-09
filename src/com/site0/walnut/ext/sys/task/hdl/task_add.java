package com.site0.walnut.ext.sys.task.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Encoding;

import com.adobe.internal.xmp.impl.Base64;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.ext.sys.task.WnSysTaskApi;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.WnRoleList;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

@JvmHdlParamArgs(value = "cqn", regex = "^(quiet|json)$")
public class task_add implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 创建任务
        WnObj oTask = new WnIoObj();

        // 设置任务命令
        String cmdText = null;

        // 采用 base64 输入子命令
        if (hc.params.has("base64")) {
            String str = hc.params.get("base64");
            cmdText = Base64.decode(str);
        }
        // 从标准输入读取
        else if (hc.params.vals.length == 0) {
            String str = sys.in.readAll();
            cmdText = Ws.trim(str);
        }
        // 将参数拼合
        else {
            cmdText = Ws.join(hc.params.vals, " ");
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
        WnUser me = sys.getMe();
        WnRoleList roles = sys.auth.getRoles(me);
        if (hc.params.hasString("u") && roles.isMemberOfRole("root")) {
            String userName = hc.params.getString("u");
            me = sys.auth.checkUser(userName);
        }
        // 如果是域子账号，则用域主账号的名称
        if (me.isSysUser()) {
            oTask.put("user", me.getName());
        } else {
            oTask.put("user", me.getMainGroup());
        }

        // 准备服务类
        WnSysTaskApi taskApi = sys.services.getTaskApi();

        // 切换账号 & 创建任务
        taskApi.addTask(oTask, input);

        // 静默
        if (hc.params.is("quiet")) {}
        // 输出JSON
        else if (hc.params.is("json")) {
            String json = Json.toJson(oTask, hc.jfmt);
            sys.out.println(json);
        }
        // 输出普通文本
        else {
            sys.out.printlnf("task created: id=%s, user=%s, command=%s",
                             oTask.id(),
                             oTask.get("user"),
                             oTask.get("command"));
        }
    }

}
