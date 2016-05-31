package org.nutz.walnut.ext.weixin.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class weixin_mplogin implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
        if (!"root".equals(sys.me.name()))
            return;
        if (params.vals.length == 1 && "init".equals(params.vals[0])) {
            String mopenid = hc.oHome.name();
            
            // 首先, 创建regapi
            WnObj obj = sys.io.createIfNoExists(null, Wn.normalizeFullPath("~/.regapi/api/mplogin/qrcode", sys), WnRace.FILE);
            String cmd  = "adduser ${weixin_FromUserName}; setup -quiet -u ${weixin_FromUserName} usr/login";
            String format = "weixin %s qrcode QR_SCENE -qrsid 0 -qrexpi 120 "
                    + "-cmd \"%s;echo '${weixin_FromUserName}' > ~/.weixin/%s/mplogin/${http-qs-uu32}\" | json -q";
            String str = String.format(format, mopenid, mopenid, cmd);
            sys.io.writeText(obj, str);
        }
    }

}
