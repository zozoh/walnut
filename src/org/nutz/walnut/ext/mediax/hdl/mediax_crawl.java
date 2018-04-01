package org.nutz.walnut.ext.mediax.hdl;

import java.net.URI;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.mediax.MediaXAPI;
import org.nutz.walnut.ext.mediax.MediaXService;
import org.nutz.walnut.ext.mediax.bean.MxCrawl;
import org.nutz.walnut.ext.mediax.bean.MxReCrawl;
import org.nutz.walnut.ext.mediax.impl.WnMediaXService;
import org.nutz.walnut.ext.mediax.util.Mxs;
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

        // 是否指定了快捷路径
        String path = hc.params.val(1);
        if (!Strings.isBlank(path)) {
            String ph = api.explain("crawl", path.split("/"));
            url = Mxs.normalizePath(uri, ph);
            uri = new URI(url);
        }

        // 准备爬取条件
        MxCrawl cr = new MxCrawl().uri(uri);
        if (hc.params.has("limit"))
            cr.limit = hc.params.getInt("limit");
        if (hc.params.has("last")) {
            String last = hc.params.getString("last");
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
