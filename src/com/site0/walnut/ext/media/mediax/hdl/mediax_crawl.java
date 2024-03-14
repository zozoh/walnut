package com.site0.walnut.ext.media.mediax.hdl;

import java.net.URI;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import com.site0.walnut.ext.media.mediax.MediaXAPI;
import com.site0.walnut.ext.media.mediax.MediaXService;
import com.site0.walnut.ext.media.mediax.bean.MxCrawl;
import com.site0.walnut.ext.media.mediax.bean.MxReCrawl;
import com.site0.walnut.ext.media.mediax.impl.WnMediaXService;
import com.site0.walnut.ext.media.mediax.util.Mxs;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class mediax_crawl implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析参数
        String url = hc.params.val_check(0);
        URI uri = new URI(url);
        String apiKey = hc.params.get("api");
        String account = hc.getString("account");

        // 准备服务类
        MediaXService mxs = new WnMediaXService(sys.io, hc.oRefer);

        // 创建接口
        MediaXAPI api;
        if (Strings.isBlank(apiKey)) {
            api = mxs.create(uri, account);
        }
        // 直接指定 Key
        else {
            api = mxs.create(apiKey, account);
        }

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
