package org.nutz.walnut.ext.weixin.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.weixin.WxConf;
import org.nutz.walnut.ext.weixin.WxMsgHandler;
import org.nutz.walnut.ext.weixin.WxUtil;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.web.WnConfig;
import org.nutz.weixin.bean.WxInMsg;

public class weixin_mplogin implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
        if (params.vals.length == 1 && "init".equals(params.vals[0])) {
            String mopenid = hc.oHome.name();
            
            // 首先, 创建regapi
            WnObj obj = sys.io.createIfNoExists(null, Wn.normalizeFullPath("~/.regapi/api/mplogin/qrcode", sys), WnRace.FILE);
            String format = "weixin %s qrcode QR_SCENE -qrsid 0 -qrexpi 120 -cmd \"echo '${weixin_FromUserName}' > ~/.weixin/%s/mplogin/${http-qs-uu32}\" | json -q";
            String str = String.format(format, mopenid, mopenid);
            sys.io.writeText(obj, str);
            
            // 检查wxconf里面的是否有scan
            WxConf conf = WxUtil.newWxApi(sys, hc.oHome.path()).getConfig();
            boolean flag = true;
            if (conf.handlers != null) {
                WxInMsg in = new WxInMsg();
                in.setMsgType("event");
                in.setEvent("scan");
                for (WxMsgHandler handler : conf.handlers) {
                    if (handler == null)
                        continue;
                    if (handler.isMatched(in)) {
                        flag = false;
                        break;
                    }
                }
            }
            if (flag) {
                
            }
        }
    }

}
