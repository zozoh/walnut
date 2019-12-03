package org.nutz.walnut.ext.weixin.hdl;

import java.io.IOException;

import org.nutz.resource.NutResource;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.weixin.WxUtil;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 
 * 获取临时素材的方法
 * 
 * @author pw
 *
 */
public class weixin_media implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnIoWeixinApi wxApi = WxUtil.genWxApi(sys, hc);
        // 下载
        if (hc.params.has("mid")) {
            String mediaId = hc.params.getString("mid");
            NutResource media = wxApi.media_get(mediaId);
            try {
                if (hc.params.has("out")) {
                    String out = hc.params.getString("out");
                    WnObj outObj = sys.io.createIfNoExists(null,
                                                           Wn.normalizeFullPath(out, sys),
                                                           WnRace.FILE);
                    sys.io.writeAndClose(outObj, media.getInputStream());
                } else {
                    sys.out.write(media.getInputStream());

                }
            }
            catch (IOException e) {
                sys.err.print(e.toString());
            }
        }
        // 上传
    }

}
