package org.nutz.walnut.ext.sys.mq.impl;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.mq.WnMqHandler;
import org.nutz.walnut.ext.sys.mq.WnMqMessage;
import org.nutz.walnut.util.WnRun;

@IocBean
public class WnMqDefaultHandler extends WnRun implements WnMqHandler {

    private static final Log log = Wlog.getEXT();

    @Override
    public void inovke(WnMqMessage msg) {

        if (log.isInfoEnabled()) {
            log.infof("MQ:received message:\n%s", msg.toString());
        }

        // 得到运行命令，如果为空，则无视
        // TODO 这里根据 msg.type 可以给出更多玩法
        String cmdText = msg.getBody();
        if (Strings.isBlank(cmdText))
            return;

        // 首先获取用户
        WnAccount u = this.auth().checkAccount(msg.getUser());

        // 得到权鉴密钥
        WnIo io = this.io();
        WnObj oHome = io.check(null, u.getHomePath());
        WnObj oSecr = io.check(oHome, ".mq/secret");
        String uSecret = io.readText(oSecr);

        // 没设密钥，不能访问
        if (Strings.isBlank(uSecret)) {
            log.warnf("Mq:NilSecret: %s :\n%s", u.getName(), msg.toString());
            return;
        }

        // 判断密钥
        uSecret = uSecret.trim();
        if (!uSecret.equals(msg.getSecret())) {
            log.warnf("Mq:InvalidSecret: %s :\n%s", u.getName(), msg.toString());
            return;
        }

        // 那么就执行咯
        this.runWithHook(u, u.getGroupName(), null, new Callback<WnAuthSession>() {
            public void invoke(WnAuthSession se) {
                exec("MQ:Run", se, cmdText);
            }
        });
    }

}
