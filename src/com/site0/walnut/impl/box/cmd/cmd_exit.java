package com.site0.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.io.WnEvalLink;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.util.Wn;

public class cmd_exit extends JvmExecutor {

    @Override
    public void exec(final WnSystem sys, String[] args) throws Exception {
        Wn.WC().security(new WnEvalLink(sys.io), () -> {
            __exec_without_security(sys);
        });
    }

    private void __exec_without_security(final WnSystem sys) {
        // 退出登录：延迟几秒以便给后续操作机会
        String oldTicket = sys.session.getTicket();
        WnSession newSe = sys.auth.removeSession(sys.session);

        // 输出这个新会话
        if (null != newSe) {
            JsonFormat jfmt = JsonFormat.nice().setQuoteName(true).setIgnoreNull(true);
            NutMap bean = newSe.toBean();
            String json = Json.toJson(bean, jfmt);
            sys.out.println(json);
        }

        // ............................................
        // 在沙盒的上下文标记一把，这样 HTTP 模块就有机会将这个作为宏发下去
        NutMap macro = new NutMap();
        if (null != newSe) {
            macro.put("seid", newSe.getTicket());
            macro.put("exit", true);
            macro.put("old_seid", oldTicket);
        }
        sys.attrs().put(Wn.MACRO.CHANGE_SESSION, macro);
    }

}
