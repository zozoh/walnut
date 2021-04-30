package org.nutz.walnut.ext.sys.task.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.ext.sys.task.WnSysTaskService;
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
        List<String> commands = new LinkedList<>();

        // 从标准输入读取
        if (hc.params.vals.length == 0) {
            String str = sys.in.readAll();
            commands.add(Ws.trim(str));
        }
        // 读取内容
        else {
            for (String v : hc.params.vals) {
                commands.add(v);
            }
        }

        // 防守
        if (commands.isEmpty()) {
            return;
        }

        // 仅有一个命令
        if (commands.size() == 1) {
            oTask.put("command", commands.get(0));
        }
        // 多个命令
        else {
            oTask.put("command", commands);
        }

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

        // 准备服务类
        WnSysTaskService taskApi = Wn.Service.tasks();

        // 切换账号 & 创建任务
        byte[] bs = input;
        Wn.WC().su(me, new Atom() {
            public void run() {
                taskApi.addTask(oTask, bs);
            }
        });

        // 输出内容
        String json = Json.toJson(oTask, hc.jfmt);
        sys.out.println(json);
    }

}
