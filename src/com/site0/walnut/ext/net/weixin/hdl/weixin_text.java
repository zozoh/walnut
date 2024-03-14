package com.site0.walnut.ext.net.weixin.hdl;

import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;
import org.nutz.weixin.bean.WxInMsg;
import org.nutz.weixin.bean.WxOutMsg;
import org.nutz.weixin.util.Wxs;

public class weixin_text implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
        String openvpn = params.check("openid");
        String text = Cmds.checkParamOrPipe(sys, params, "text", true);
        WxOutMsg om = Wxs.respText(null, text);
        WxInMsg im = new WxInMsg();
        im.setFromUserName(openvpn);
        im.setToUserName(hc.oRefer.name());
        Wxs.fix(im, om);
        String xml = Wxs.asXml(om);
        sys.out.println(xml);
    }

}
