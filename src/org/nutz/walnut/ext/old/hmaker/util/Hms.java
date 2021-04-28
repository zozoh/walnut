package org.nutz.walnut.ext.old.hmaker.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Maths;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 帮助函数集
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public final class Hms {

    /**
     * 控件处理类工厂的单例
     */
    public static HmComFactory COMs = new HmComFactory();

    /**
     * 自动把驼峰命名的键变成中划线分隔的，比如 "borderColor" 的属性会变成 "border-color"
     */
    public static final int AUTO_LOWER = 1;

    /**
     * 生成的 CSS 文本时用大括号包裹
     */
    public static final int WRAP_BRACE = 1 << 1;

    /**
     * 生成从 CSS 文本为多行
     */
    public static final int RULE_MULTILINE = 1 << 2;

    private static final Pattern P_BACKGROUND = Pattern.compile("^.*(url\\(\"?/o/read/id:([0-9a-z]+)(/(.+))?\"?\\)).*$");

    /**
     * 根据一个 Map 描述的CSS规则，生成 CSS 文本，
     * 
     * @param ing
     *            上下文对象
     * 
     * @param rule
     *            Map 描述的规则
     * 
     * @param mode
     *            位图配置参加常量 <em>AUTO_LOWER</em>,<em>WRAP_BRACE</em>,
     *            <em>RULE_MULTILINE</em>
     * 
     * @return CSS 文本
     * 
     * @see #AUTO_LOWER
     * @see #WRAP_BRACE
     * @see #RULE_MULTILINE
     */
    public static String genCssRule(HmPageTranslating ing, Map<String, Object> rule, int mode) {
        if (rule.isEmpty())
            return null;
        boolean autoLower = Maths.isMask(mode, AUTO_LOWER);
        boolean wrapBrace = Maths.isMask(mode, WRAP_BRACE);
        boolean multiline = Maths.isMask(mode, RULE_MULTILINE);

        String re = wrapBrace ? "{" : "";

        for (Map.Entry<String, Object> en : rule.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();

            // 忽略空
            if (null == val) {
                continue;
            }

            // 忽略特殊属性
            if (key.startsWith("seo"))
                continue;

            // 数字转成字符串要加 "px"
            String str;
            if (Mirror.me(val).isNumber()) {
                str = val + "px";
            }
            // 其他的全当字符串
            else {
                str = val.toString();
                if (Strings.isBlank(str))
                    continue;
            }

            // 对于背景的特殊处理 /o/read/id:xxxx 要改成相对路径
            /**
             * <pre>
            0/62  Regin:0/62
            0:[  0, 62) #000 url(/o/read/id:xxxx/images/bg(1).gif) no-repeat top right
            1:[  5, 42) url(/o/read/id:xxxx/images/bg(1).gif)
            2:[ 20, 24) xxxx
            3:[ 24, 41) /images/bg(1).gif
            4:[ 25, 41) images/bg(1).gif
             * </pre>
             */
            if ("background".equals(key) || "backgroundImage".equals(key)) {
                Matcher m = P_BACKGROUND.matcher(str);
                if (m.find()) {
                    String bgImgId = m.group(2);
                    String bgImgPh = m.group(4);

                    // 确保图片对象会被加载
                    WnObj oBgImg = ing.io.checkById(bgImgId);

                    // 是其内的图片
                    if (!Strings.isBlank(bgImgPh)) {
                        // TODO 这个正则有问题，图片名在 url("xxx") 形式下
                        // 后面会有一个 "，懒得把它弄复杂了，这里字符串截断一下吧
                        if (bgImgPh.endsWith("\"")) {
                            bgImgPh = bgImgPh.substring(0, bgImgPh.length() - 1);
                        }
                        // 确保去掉前后空白
                        bgImgPh = Strings.trim(bgImgPh);
                        // 来吧
                        oBgImg = ing.io.check(oBgImg, bgImgPh);
                    }

                    // 计入资源
                    ing.resources.add(oBgImg);

                    // 得到相对路径
                    String rph = ing.getRelativePath(ing.oSrc, oBgImg);

                    // 替换
                    str = str.substring(0, m.start(1))
                          + "url(\""
                          + rph
                          + "\")"
                          + str.substring(m.end(1));
                }
            }

            // 拼装
            if (multiline)
                re += "\n  ";
            re += autoLower ? Strings.lowerWord(key, '-') : key;
            re += ":" + str + ";";
        }

        // 判断空内容
        if (Strings.isBlank(re) || (wrapBrace && "{".equals(re)))
            return null;

        // 结尾
        if (multiline) {
            re += '\n';
        }

        if (wrapBrace)
            re += '}';

        // 返回
        return re;
    }

    /**
     * 生成一行的 CSS 样式文本，不用大括号包裹。且不会自动将驼峰命名的样式名转成中划线分隔
     * 
     * @param rule
     *            Map 描述的规则
     * @return 样式文本
     * @see #genCssRule(NutMap, int)
     */
    public static String genCssRuleStyle(HmPageTranslating ing, Map<String, Object> rule) {
        return genCssRule(ing, rule, AUTO_LOWER);
    }

    /**
     * 生成多行的 CSS 样式文本，用大括号包裹。同时会自动将驼峰命名的样式名转成中划线分隔
     * 
     * @param rule
     *            Map 描述的规则
     * @return 样式文本
     * @see #genCssRule(NutMap, int)
     */
    public static String genCssRuleText(HmPageTranslating ing, Map<String, Object> rule) {
        return genCssRule(ing, rule, AUTO_LOWER | WRAP_BRACE | RULE_MULTILINE);
    }

    /**
     * 将一个 json 描述的 CSS 对象变成 CSS 文本
     * 
     * @param css
     *            对象，key 作为 selector，值是 Map 对象，代表 rule
     * @param prefix
     *            为 selector 增加前缀，如果有的话，后面会附加空格
     * @return 样式文本
     * 
     * @see #genCssRuleText(Map)
     */
    public static String genCssText(HmPageTranslating ing, Map<String, NutMap> css, String prefix) {
        prefix = Strings.isBlank(prefix) ? "" : prefix + " ";
        String re = "";
        for (Map.Entry<String, NutMap> en : css.entrySet()) {
            String selector = en.getKey();
            NutMap rule = en.getValue();

            String ruleText = genCssRuleText(ing, rule);

            if (null == ruleText)
                continue;

            // 没有前缀，直接来
            if (Strings.isBlank(prefix)) {
                re += Strings.sNull(selector, "");
            }
            // 循环增加前缀
            else if (!Strings.isBlank(selector)) {
                String[] ss = Strings.splitIgnoreBlank(selector);
                for (int i = 0; i < ss.length; i++) {
                    ss[i] = prefix + " " + ss[i];
                }
                re += Lang.concat(", ", ss);
            }
            // 直接就是前缀
            else {
                re += prefix;
            }
            re += ruleText;
            re += "\n";
        }
        return re;
    }

    public static String escapeJsoupHtml(String html) {
        return html.replace("\n", "\\hm:%N")
                   .replace(" ", "\\hm:%W")
                   .replace("<", "\\hm:%[")
                   .replace(">", "\\hm:%]")
                   .replace("&", "\\hm:%#");
    }

    public static String unescapeJsoupHtml(String html) {
        return html.replace("\\hm:%N", "\n")
                   .replace("\\hm:%W", " ")
                   .replace("\\hm:%[", "<")
                   .replace("\\hm:%]", ">")
                   .replace("\\hm:%#", "&");
    }

    public static String wrapjQueryDocumentOnLoad(String script) {
        return "$(function(){" + script + "});";
    }

    /**
     * @see #loadProp(Element, String, boolean)
     */
    public static NutMap loadPropAndRemoveNode(Element ele, String className) {
        return loadProp(ele, className, true);
    }

    /**
     * 从一个节点里读取 hmaker 给它设置的属性，
     * <p>
     * 属性节点是一个 SCRIPT，根据类选择器确定该节点
     * <p>
     * 节点必须是给定节点的子，本函数读取完节点后，会将其删除
     * 
     * @param ele
     *            元素
     * @param className
     *            类选择器选择器
     * @param removeNode
     *            是否同时删除这个节点
     * 
     * @return 解析好的属性
     */
    public static NutMap loadProp(Element ele, String className, boolean removeNode) {
        // Element eleProp = ele.children().last();
        Element eleProp = ele.select(">script." + className).first();
        if (null == eleProp)
            return new NutMap();
        // throw Er.createf("e.cmd.hmaker.publish.invalidEleProp",
        // "<%s#%s>",
        // ele.tagName(),
        // Strings.sBlank(ele.attr("id"), "BLOCK"));

        // 读取
        String json = eleProp.html();
        json = Wn.unescapeHtml(json, false);

        // 删除属性
        if (removeNode)
            eleProp.remove();

        // 解析并返回
        if (Strings.isBlank(json))
            return new NutMap();
        return Json.fromJson(NutMap.class, json);

    }

    /**
     * 判断站点的一个文件对象是否需要转换
     * <p>
     * 没有后缀，且类型为 "html" 标识着需要转换
     * 
     * @param o
     *            文件对象
     * @return 是否需要转换
     */
    public static boolean isNeedTranslate(WnObj o) {
        String suffixName = Strings.sNull(Files.getSuffixName(o.path()), "");
        return Strings.isBlank(suffixName) && o.isType("html");
    }

    /**
     * 给定一个站点目录的路径，或者其内任意的文件或者目录。本函数会向上查找，直到找到 tp:"hmaker_site" 的目录为止
     * 
     * @param sys
     *            运行上下文
     * @param str
     *            站点目录路径（或者是其内文件）
     * @return 站点目录对象
     * @throws "e.cmd.hmaker.noSiteHome"
     */
    public static WnObj checkSiteHome(WnSystem sys, String str) {
        WnObj o = Wn.checkObj(sys, str);
        while (!o.isType("hmaker_site") && o.hasParent()) {
            o = o.parent();
        }
        if (null == o || !o.isType("hmaker_site"))
            throw Er.create("e.cmd.hmaker.noSiteHome", str);
        return o;
    }

    /**
     * 给定一个站点内文件对象。本函数会向上查找，直到找到 tp:"hmaker_site" 的目录为止
     * 
     * @param sys
     *            运行上下文
     * @param data
     *            站点目录路径（或者是其内文件）
     * @return 站点目录对象，null 表示不在任何站点中
     */
    public static WnObj getSiteHome(WnSystem sys, WnObj oPage) {
        if (null != oPage) {
            WnObj o = oPage;
            while (!o.isType("hmaker_site") && o.hasParent()) {
                o = o.parent();
            }
            return o;
        }
        return null;
    }

    /**
     * 判断给定 css 值是否是 unset，空值也会当做 unset
     * 
     * @param val
     *            值
     * @return 是否是 unset
     */
    public static boolean isUnset(String val) {
        return Strings.isBlank(val) || "unset".equals(val);
    }

    public static WnObj copy_skin_var(WnSystem sys, WnObj oSiteHome, String skin) {
        WnObj oSkinVar;
        String ls_ph = Wn.appendPath("~/.hmaker/skin/", skin, "_skin_var.less");
        WnObj oSkinVarSrc = Wn.checkObj(sys, ls_ph);
        oSkinVar = sys.io.create(oSiteHome, ".skin/_skin_var.less", WnRace.FILE);
        Wn.Io.copyFile(sys.io, oSkinVarSrc, oSkinVar);
        return oSkinVar;
    }

    /**
     * 会看看站点的皮肤 skin.css 有没有必要重新生成
     * 
     * @param sys
     *            系统上下文
     * @param oSiteHome
     *            站点主目录
     * @param skinName
     *            皮肤名称
     * @return 生成的皮肤 css
     */
    public static WnObj genSiteSkinCssObj(WnSystem sys, WnObj oSiteHome, String skinName) {
        WnObj oSkinVar = sys.io.fetch(oSiteHome, ".skin/_skin_var.less");
        // 如果没有这个文件，就采用皮肤目录里的 skin.css
        if (null == oSkinVar) {
            WnObj oSkinCss = Wn.getObj(sys, "~/.hmaker/skin/" + skinName + "/skin.css");
            if (null != oSkinCss)
                return oSkinCss;

            // 还没有的话，就试图生成一个
            oSkinVar = copy_skin_var(sys, oSiteHome, skinName);
        }

        // 如果 ETag 与 .cache/skin.css 文件里面的记录的一样
        // 那么就用 skin.css 来下载
        String etagSkinVar = Wn.getEtag(oSkinVar);
        WnObj oSkinCss = sys.io.fetch(oSiteHome, ".cache/skin.css");
        if (null != oSkinCss
            && !Strings.isBlank(etagSkinVar)
            && oSkinCss.is("skin_var_etag", etagSkinVar)) {
            // 嗯，我看啥也不用做了
        }
        // 否则的话，就执行命令，生成一个 skin.css
        else {
            // lessc compile ~/.hmaker/skin/default/skin.less -pri-path
            // id:siteId/.skin
            String cmdStr = "lessc compile ~/.hmaker/skin/%s/skin.less -pri-path id:%s/.skin > id:%2$s/.cache/skin.css";
            sys.exec2f(cmdStr, skinName, oSiteHome.id());

            // 得到这个 skin.css
            oSkinCss = sys.io.fetch(oSiteHome, ".cache/skin.css");

            // 重新标记 ETag 后
            oSkinCss.setv("skin_var_etag", etagSkinVar);
            sys.io.set(oSkinCss, "^skin_var_etag$");
        }
        return oSkinCss;
    }

    /**
     * 根据给定的内容给页面对象设置元数据
     * 
     * @param hpc
     *            转换上下文，如果为 null 则会自动生成
     * 
     * @param sys
     *            系统上下文
     * @param oPage
     *            页面对象
     * @param content
     *            内容
     */
    public static void syncPageMeta(HmContext hpc, WnSystem sys, WnObj oPage, String content) {
        // 生成一个新的转换上下文
        if (null == hpc) {
            hpc = new HmContext(sys.io, sys.getMyGroup());
        }
        // 确保有 apiHome
        if (null == hpc.oApiHome)
            hpc.oApiHome = Wn.getObj(sys, "~/.regapi/api");

        // 准备存储分析的结果
        Set<String> libNames = new HashSet<>();
        Set<String> apiList = new HashSet<String>();
        Set<String> tsidList = new HashSet<String>();
        Set<String> tmplList = new HashSet<String>();
        List<String> palist = new LinkedList<>();
        List<String> anchors = new LinkedList<>();
        List<String> comIds = new LinkedList<>();

        // 有内容的话开始分析
        if (!Strings.isBlank(content)) {
            // 逐个分析控件
            Document doc = Jsoup.parse(content);
            Elements eleComs = doc.body().select(".hm-com");

            for (Element ele : eleComs) {
                // 存储关联的组件
                String libName = ele.attr("lib");
                String comType = ele.attr("ctype");
                String comId = ele.attr("id");

                // 组件的话记录一下
                if (!Strings.isBlank(libName)) {
                    libNames.add(libName);
                }
                // 普通控件的话，看看是否接受什么动态参数以及内部支持的锚点
                else {
                    HmComHandler hmCom = Hms.COMs.check(comType);

                    comIds.add(comId);
                    hmCom.joinParamList(ele, palist);
                    hmCom.joinAnchorList(ele, anchors);
                }

                // 分析动态控件
                if ("dynamic".equals(comType)) {
                    NutMap com = loadProp(ele, "hm-prop-com", false);

                    // 记录模板
                    String template = com.getString("template");
                    if (!Strings.isBlank(template))
                        tmplList.add(template);

                    // 记录 API
                    String api = com.getString("api");
                    if (!Strings.isBlank(api))
                        apiList.add(api);

                    // 得到 params.pid
                    NutMap apiParams = com.getAs("params", NutMap.class);
                    if (null != apiParams) {
                        String[] tsids = apiParams.getAs("pid", String[].class);
                        if (null != tsids && tsids.length > 0) {
                            for (String tsid : tsids) {
                                tsidList.add(tsid);
                            }
                        }
                    }
                }
            }
        }

        // 得到原来的记录
        String myTsId = __get_hm_pg_xx_in_list(oPage, "hm_pg_tsid", tsidList);
        String myApi = __get_hm_pg_xx_in_list(oPage, "hm_pg_api", apiList);

        // 更新一下 API 的返回值
        String apiMethod = null;
        String apiReturn = null;
        if (!Strings.isBlank(myApi)) {
            WnObj oApi = hpc.getApiObj(myApi);
            if (null != oApi) {
                apiReturn = oApi.getString("api_return");
                apiMethod = oApi.getString("api_method");
            }
        }

        // 得到站点
        WnObj oSiteHome = getSiteHome(sys, oPage);
        oPage.setv("hm_site_id", oSiteHome == null ? null : oSiteHome.id());
        oPage.setv("hm_pg_tsid", myTsId);
        oPage.setv("hm_pg_api", myApi);
        oPage.setv("hm_api_return", apiReturn);
        oPage.setv("hm_api_method", apiMethod);
        oPage.setv("hm_list_tsid", tsidList);
        oPage.setv("hm_list_api", apiList);
        oPage.setv("hm_list_tmpl", tmplList);
        oPage.setv("hm_pg_anchors", anchors);
        oPage.setv("hm_pg_params", palist);
        oPage.setv("hm_pg_coms", comIds);
        // .................................................
        // 保存元数据索引
        oPage.setv("hm_libs", libNames);
        sys.io.set(oPage, "^hm_(site_id|libs|(pg|list|api)_[a-z]+)$");
    }

    private static String __get_hm_pg_xx_in_list(WnObj oPage, String key, Set<String> tsidList) {
        String myTsId = oPage.getString(key);
        // 清空
        if (tsidList.isEmpty()) {
            myTsId = null;
        }
        // 不存在或者本身就没有，自动选第一个
        else if (Strings.isBlank(myTsId) || !tsidList.contains(myTsId)) {
            myTsId = tsidList.iterator().next();
        }
        return myTsId;
    }

    /**
     * 按照一定格式输出给定的对象列表，函数假想你的命令支持下面的参数:
     * 
     * <pre>
     * [-key id,nm..]    # 指定输出的文件元数据字段，这里 `rph` 是特殊元数据，表示相对路径
     * [-obj]            # 如果只有一个字段，强制为对象输出，否则会输出字符串
     * [-cqn]            # 输出库文件元数据据 c,q,n 为 JSON 的格式化信息
     * </pre>
     * 
     * @param sys
     *            系统上下文
     * @param hc
     *            子命令上下文
     * @param list
     *            要输出的对象列表
     */
    public static void output_resource_objs(WnSystem sys, JvmHdlContext hc, List<WnObj> list) {
        // 输出结果
        if (hc.params.has("key")) {
            String[] keys = Strings.splitIgnoreBlank(hc.params.get("key"));

            // 如果只有一个字段，默认会输出字符串，除非强制为对象
            if (keys.length == 1 && !hc.params.is("obj")) {
                List<Object> outs = new ArrayList<>(list.size());
                String key = keys[0];
                for (WnObj o : list) {
                    Object v = o.get(key);
                    outs.add(v);
                }
                sys.out.print(Json.toJson(outs, hc.jfmt));
            }
            // 过滤字段
            else {
                List<NutMap> outs = new ArrayList<>(list.size());
                for (WnObj o : list) {
                    NutMap map = NutMap.WRAP(o).pick(keys);
                    outs.add(map);
                }
                sys.out.print(Json.toJson(outs, hc.jfmt));
            }
        }
        // 默认的输出全部的 JSON
        else {
            sys.out.print(Json.toJson(list, hc.jfmt));
        }
    }

    // =================================================================
    // 不许实例化
    private Hms() {}
}
