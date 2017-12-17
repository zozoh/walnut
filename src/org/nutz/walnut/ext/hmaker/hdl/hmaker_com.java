package org.nutz.walnut.ext.hmaker.hdl;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(regex = "^(obj|nolib)$", value = "cqn")
public class hmaker_com implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 首先分析指定页面
        WnObj oPage = hc.oRefer;
        String html = sys.io.readText(oPage);
        Document doc = Jsoup.parse(html);

        // 准备过滤条件
        boolean nolib = hc.params.is("nolib");

        // 循环查询一下组件, 并记入列表
        Elements eleComs = doc.body().getElementsByClass("hm-com");
        List<NutMap> list = new ArrayList<>(eleComs.size());
        for (Element ele : eleComs) {
            // 判断一下组件库
            NutMap lib = __get_my_lib_info(ele);
            if (nolib && null != lib)
                continue;

            // 准备对象
            NutMap obj = new NutMap();
            obj.put("id", ele.attr("id"));
            obj.put("ctype", ele.attr("ctype"));
            obj.put("skin", ele.attr("skin"));
            obj.put("lib", lib);

            // 过滤
            if (__is_ignore(hc, obj, "id"))
                continue;

            // 记入结果
            list.add(obj);
        }

        // 仅输出一个
        if (list.size() == 1 && hc.params.is("obj")) {
            sys.out.println(Json.toJson(list.get(0), hc.jfmt));
        }
        // 输出列表
        else {
            sys.out.println(Json.toJson(list, hc.jfmt));
        }
    }

    private NutMap __get_my_lib_info(Element ele) {
        // 找到 lib 所在的元素
        Element eleLib = ele;
        while (Strings.isBlank(eleLib.attr("lib"))) {
            eleLib = eleLib.parent();
            if (null == eleLib)
                break;
        }
        // 得到组件信息
        String cLib = null == eleLib ? null : eleLib.attr("lib");
        String mLib = ele.attr("lib");
        boolean no_cLib = Strings.isBlank(cLib);
        boolean no_mLib = Strings.isBlank(mLib);

        // 没有组件信息
        if (no_cLib && no_mLib)
            return null;

        // 返回
        NutMap lib = new NutMap();
        lib.put("name", cLib);
        lib.put("myLibName", mLib);
        lib.put("isInLib", !no_cLib && no_mLib);
        return lib;
    }

    private boolean __is_ignore(JvmHdlContext hc, NutMap obj, String key) {
        String flt = hc.params.get(key);
        String val = obj.getString(key);
        // 空值就无视吧
        if (null == val) {
            return true;
        }
        // 过滤
        else if (null != flt) {
            // 正则
            if (flt.startsWith("^")) {
                return !val.matches(flt);
            }
            // 反向正则
            else if (flt.startsWith("!")) {
                return val.matches(flt.substring(1));
            }
            // 精确匹配
            return !val.equals(flt);
        }
        return false;
    }

}
