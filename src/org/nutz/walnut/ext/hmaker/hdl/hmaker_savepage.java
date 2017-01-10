package org.nutz.walnut.ext.hmaker.hdl;

import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.hmaker.util.Hms;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("ocqn")
public class hmaker_savepage implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到页面对象
        WnObj oPage = Wn.checkObj(sys, hc.params.val_check(0));
        // .................................................
        // 得到要写入的内容
        String content = hc.params.get("content");
        // 从标准输入得到内容
        if ("true".equals(content) || Strings.isBlank(content)) {
            content = null;
        }
        // 从文件得到内容
        if (null == content && hc.params.has("file")) {
            WnObj o = Wn.checkObj(sys, hc.params.get("file"));
            content = sys.io.readText(o);
        }
        // 默认从标准输入读取
        if (null == content) {
            content = sys.in.readAll();
        }
        // .................................................
        // 执行写入
        sys.io.writeText(oPage, content);

        // .................................................
        // 分析页面内容看看都使用了哪些组件
        Set<String> libNames = new HashSet<>();
        if (!Strings.isBlank(content)) {
            Document doc = Jsoup.parse(content);
            Elements eleLibs = doc.body().select(".hm-com[lib]");
            for (Element ele : eleLibs) {
                libNames.add(ele.attr("lib"));
            }
        }

        // 得到站点
        WnObj oSiteHome = Hms.getSiteHome(sys, oPage);
        oPage.setv("hm_site_id", oSiteHome == null ? null : oSiteHome.id());

        // .................................................
        // 保存元数据索引
        oPage.setv("hm_libs", libNames);
        sys.io.set(oPage, "^(hm_.+)$");

        // .................................................
        // 最后输出内容
        if (hc.params.is("o")) {
            sys.out.println(Json.toJson(oPage, hc.jfmt));
        }
    }

}
