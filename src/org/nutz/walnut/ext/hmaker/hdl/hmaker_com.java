package org.nutz.walnut.ext.hmaker.hdl;

import java.util.LinkedList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.hmaker.util.HmComHandler;
import org.nutz.walnut.ext.hmaker.util.Hms;
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
        List<NutMap> list = new LinkedList<>();
        List<NutMap> g_anchor = new LinkedList<>();
        List<String> g_anlist = new LinkedList<>();
        List<String> g_palist = new LinkedList<>();
        NutMap g_param = new NutMap();
        for (Element ele : eleComs) {
            // 判断一下组件库
            NutMap lib = __get_my_lib_info(ele);
            if (nolib && null != lib)
                continue;
            // ....................................................
            // 过滤
            String comId = ele.attr("id");
            String comType = ele.attr("ctype");
            String skin = ele.attr("skin");
            if (__is_ignore(hc, "id", comId)
                || __is_ignore(hc, "ctype", comType)
                || __is_ignore(hc, "skin", skin))
                continue;

            // ....................................................
            // 获取组件
            HmComHandler hmCom = Hms.COMs.check(comType);

            // 准备内部锚点列表
            List<String> anchors = new LinkedList<>();
            hmCom.joinAnchorList(ele, anchors);

            // 准备动态参数列表
            List<String> params = new LinkedList<>();
            hmCom.joinParamList(ele, params);

            // ....................................................
            // 准备对象
            NutMap obj = new NutMap();
            obj.setv("id", comId);
            obj.setv("ctype", comType);
            obj.setv("skin", skin);
            obj.setv("lib", lib);
            obj.setv("anchors", anchors);
            obj.setv("params", params);

            // 记入结果
            list.add(obj);

            // -dis anchor 全部的锚点(可被界面直接使用的形式)
            g_anchor.add(NutMap.NEW()
                               .setv("id", comId)
                               .setv("ctype", comType)
                               .setv("skin", skin)
                               .setv("lib", lib));
            for (String an : anchors) {
                NutMap anobj = NutMap.NEW().setv("id", comId);
                g_anchor.add(anobj.setv("ctype", comType).setv("anchor", an));
            }

            // -dis anlist 全部的锚点
            g_anlist.add(comId);
            g_anlist.addAll(anchors);

            // -dis palist 控件可以接受的动态参数
            g_palist.addAll(params);

            // -dis param 控件可以接受的动态参数(Map形态)
            for (String pa : params) {
                g_param.put(pa, "");
            }
        }

        // 特殊显示
        String dis = hc.params.get("dis");
        // # 显示某个页面全部控件可以接受的动态参数(Map形态)
        if ("param".equals(dis)) {
            sys.out.println(Json.toJson(g_param, hc.jfmt));
        }
        // # 显示某个页面全部控件可以接受的动态参数
        else if ("palist".equals(dis)) {
            sys.out.println(Json.toJson(g_palist, hc.jfmt));
        }
        // # 显示某个页面全部的锚点
        else if ("anlist".equals(dis)) {
            sys.out.println(Json.toJson(g_anlist, hc.jfmt));
        }
        // # 显示某个页面全部的锚点(可被界面直接使用的形式)
        else if ("anchor".equals(dis)) {
            sys.out.println(Json.toJson(g_anchor, hc.jfmt));
        }
        // 仅输出一个
        else if (list.size() == 1 && hc.params.is("obj")) {
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

    private boolean __is_ignore(JvmHdlContext hc, String key, String val) {
        String flt = hc.params.get(key);
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
