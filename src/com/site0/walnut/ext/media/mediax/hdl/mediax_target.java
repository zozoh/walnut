package com.site0.walnut.ext.media.mediax.hdl;

import java.net.URI;

import com.site0.walnut.ext.media.mediax.MediaXAPI;
import com.site0.walnut.ext.media.mediax.MediaXService;
import com.site0.walnut.ext.media.mediax.impl.WnMediaXService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class mediax_target implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 得到第一个参数，看看是否是 actionName
        String actionName = hc.params.val_check(0);
        String url;
        // 是一个 ActionName
        if (actionName.matches("^(crawl|post|post|download)$")) {
            url = hc.params.val_check(1);
        }
        // 否则就当做全部咯
        else {
            url = actionName;
            actionName = null;
        }

        // 分析参数
        URI uri = new URI(url);

        // 准备服务类
        MediaXService mxs = new WnMediaXService(sys.io, hc.oRefer);

        // 创建接口
        MediaXAPI api = mxs.create(uri, hc.getString("account"));

        // 得到返回值并打印
        String re = api.dumpTarget(actionName);
        sys.out.println(re);
    }

}
