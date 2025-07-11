package com.site0.walnut.ext.sys.task;

import java.util.List;

import org.nutz.lang.Encoding;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Ws;

public class cmd_task extends JvmHdlExecutor {

    /**
     * 根据当前会话设置，准备查询对象
     * 
     * @param sys
     *            系统运行时
     * @param hc
     *            上下文
     * @return 任务对象查询对象
     */
    public static WnSysTaskQuery prepareTaskQuery(WnSystem sys, JvmHdlContext hc) {
        WnUser me = sys.getMe();
        WnRoleList roles = sys.auth.getRoles(me);
        boolean isAdmin = roles.isMemberOfRole("root");
        return prepareTaskQuery(sys, hc, isAdmin);
    }

    public static WnSysTaskQuery prepareTaskQuery(WnSystem sys, JvmHdlContext hc, boolean isAdmin) {
        WnSysTaskQuery tq = new WnSysTaskQuery();
        tq.loadFromParams(hc.params);

        // 如果不是 root 组管理员，仅能操作自己
        if (!isAdmin) {
            WnUser me = sys.getMe();
            tq.setUserName(me.getName());
        }
        return tq;
    }

    /**
     * 输出任务列表
     * 
     * @param sys
     *            系统运行时
     * @param hc
     *            上下文
     * @param list
     *            任务对象列表
     */
    public static void outputTasks(WnSystem sys, JvmHdlContext hc, List<WnObj> list) {
        if (!hc.params.is("json")) {
            hc.params.setDftString("t", "nm,tp,user,len,command");
            hc.params.setv("b", true);
            hc.params.setv("i", true);
            hc.params.setv("s", true);
            hc.params.setv("h", true);
            formatObjCommandField(list, "command");
            Cmds.output_objs_as_table(sys, hc.params, null, list);
        }
        // 输出 Bean
        else {
            hc.params.setv("l", true);
            Cmds.output_objs(sys, hc.params, null, list, true);
        }
    }

    /**
     * 为了防止命令输出太长，本函数，把过长的部分加省略号
     * 
     * @param list
     *            任务对象
     * @param key
     *            命令存放在对象的哪个键里
     */
    public static void formatObjCommandField(List<WnObj> list, String key) {
        for (WnObj o : list) {
            Object cmd = o.get(key);
            if (null != cmd) {
                String cmdText;
                if (cmd.getClass().isArray()) {
                    byte[] bs = (byte[]) cmd;
                    cmdText = new String(bs, Encoding.CHARSET_UTF8);
                } else {
                    cmdText = cmd.toString();
                }
                cmdText = Ws.trim(cmdText);
                if (cmdText.length() > 20) {
                    cmdText = cmdText.substring(0, 20) + "..";
                }
                o.put(key, cmdText);
            }
        }
    }
}
