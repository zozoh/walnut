package org.nutz.walnut.ext.media.mediax.hdl;

import java.net.URI;

import org.nutz.walnut.ext.media.mediax.MediaXAPI;
import org.nutz.walnut.ext.media.mediax.MediaXService;
import org.nutz.walnut.ext.media.mediax.impl.WnMediaXService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class mediax_explain implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析参数
        String actionName = hc.params.val_check(0);
        String url = hc.params.val_check(1);
        String[] path = hc.params.val_check(2).split("/");
        URI uri = new URI(url);

        // 准备服务类
        MediaXService mxs = new WnMediaXService(sys.io, hc.oRefer);

        // 创建接口
        MediaXAPI api = mxs.create(uri, hc.getString("account"));

        // 得到返回值并打印
        String re = api.explain(actionName, path);
        sys.out.println(re);
    }

}
