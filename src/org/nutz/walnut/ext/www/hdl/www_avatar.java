package org.nutz.walnut.ext.www.hdl;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import org.nutz.img.Images;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.cmd_www;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cqn", regex = "^(quiet|ajax)$")
public class www_avatar implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/账户/密码/票据
        WnObj oWWW = cmd_www.checkSite(sys, hc);
        String uname = hc.params.val(1);
        String ticket = hc.params.get("ticket");

        // -------------------------------
        // 准备服务类
        WnWebService webs = new WnWebService(sys, oWWW);
        WnAuthService auth = webs.getAuthApi();

        // -------------------------------
        // 准备获取用户
        WnAccount u;

        // 用票据获取用户
        if (!Strings.isBlank(ticket)) {
            WnAuthSession se = auth.checkSession(ticket);
            u = se.getMe();
        }
        // 直接获取用户
        else {
            u = auth.checkAccount(uname);
        }

        // -------------------------------
        // 读取头像
        String headimgurl = hc.params.check("url");
        if (headimgurl.startsWith("=")) {
            String key = Strings.trim(headimgurl.substring(1));
            headimgurl = u.getMetaString(key);
        }
        WnObj oThumb = auth.getAvatarObj(u, true);
        // 读取 Image
        try {
            URL thumb_url = new URL(headimgurl);
            BufferedImage im = Images.read(thumb_url);
            sys.io.writeImage(oThumb, im);

            // 保存头像
            u.setThumb("id:" + oThumb.id());
            NutMap map = u.toBeanOf("thumb");
            auth.saveAccount(u, map);
        }
        catch (MalformedURLException e) {
            throw Er.wrap(e);
        }

        // -------------------------------
        // 输出
        if (!hc.params.is("quiet")) {
            NutMap uMap = u.toBeanForClient();
            cmd_www.outputJsonOrAjax(sys, uMap, hc);
        }
    }

}
