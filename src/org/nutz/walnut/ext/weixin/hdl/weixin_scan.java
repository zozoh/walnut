package org.nutz.walnut.ext.weixin.hdl;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.segment.CharSegment;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrInfo;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class weixin_scan implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // regapi的模板
        // weixin ${weixin_ToUserName} scan -openid ${weixin_FromUserName}
        // -eventkey '${weixin_EventKey}' -c

        ZParams params = ZParams.parse(hc.args, "c");
        String pnb = hc.oHome.name();
        String openid = params.check("openid");
        String eventkey = params.check("eventkey");

        // 如果指定了需要新建用户,且为root组的权限
        if (params.is("c") && sys.me.myGroups().contains("root")) {
            // 检查是否已经建好用户,没有的话就建一下
            WnUsrInfo info = new WnUsrInfo();
            info.setWeixinPNB(pnb);
            info.setWeixinOpenId(openid);

            WnUsr usr = sys.usrService.fetchBy(info);
            if (usr == null) {
                usr = sys.usrService.create(info);
                sys.out.println("用户新建完成");
            }
        }

        // 乱入?
        if (Strings.isBlank(eventkey)) {
            return;
        }

        // 第一次关注就扫描的话, 会添加前缀qrscene_ 移除之
        if (eventkey.startsWith("qrscene_"))
            eventkey = eventkey.substring("qrscene_".length());

        // 找找有没有对应的文本,有就当命令执行一下
        String path = Wn.normalizeFullPath("~/.weixin/"
                                           + hc.oHome.name()
                                           + "/scene/"
                                           + eventkey,
                                           sys);
        WnObj obj = sys.io.fetch(null, path);
        if (obj != null) {
            String cmd = sys.io.readText(obj);
            CharSegment cs = new CharSegment(cmd);
            cmd = cs.render(Lang.context().set("weixin_FromUserName", openid)).toString();
            sys.out.print(sys.exec2(cmd));
        }
        // TODO 支持default?
    }
}
