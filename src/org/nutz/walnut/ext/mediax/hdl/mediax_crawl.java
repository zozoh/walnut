package org.nutz.walnut.ext.mediax.hdl;

import java.net.URI;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.walnut.ext.mediax.MediaXAPI;
import org.nutz.walnut.ext.mediax.MediaXService;
import org.nutz.walnut.ext.mediax.bean.MxCrawl;
import org.nutz.walnut.ext.mediax.bean.MxReCrawl;
import org.nutz.walnut.ext.mediax.impl.WnMediaXService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class mediax_crawl implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析参数
        String url = hc.params.val_check(0);
        URI uri = new URI(url);

        // 准备服务类
        MediaXService mxs = new WnMediaXService(sys.io, hc.oRefer);

        // 创建接口
        MediaXAPI api = mxs.create(uri, hc.getString("account"));

        // 准备爬取条件
        MxCrawl cr = new MxCrawl().uri(uri);
        if (hc.has("limit"))
            cr.limit = hc.getInt("limit");
        if (hc.has("last")) {
            String last = hc.getString("last");
            // 毫秒
            if (last.matches("^[0-9]{6,}$")) {
                cr.lastDate(Long.parseLong(last));
            }
            // 日期
            else {
                cr.lastDate(last);
            }
        }

        // 执行爬取
        List<MxReCrawl> list = api.crawl(cr);

        // 输出
        sys.out.println(Json.toJson(list, hc.jfmt));
    }

}
