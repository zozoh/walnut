package org.nutz.walnut.ext.hmaker.util;

import java.util.ArrayList;
import java.util.HashSet;
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
            if ("background".equals(key)) {
                Matcher m = Pattern.compile("url\\(\"?/o/read/id:([0-9a-z]+)\"?\\)").matcher(str);
                if (m.find()) {
                    String bgImgId = m.group(1);

                    // 确保图片对象会被加载
                    WnObj oBgImg = ing.io.checkById(bgImgId);
                    ing.resources.add(oBgImg);

                    // 得到相对路径
                    String rph = ing.getRelativePath(ing.oSrc, oBgImg);

                    // 替换
                    str = str.substring(0, m.start())
                          + " url(\""
                          + rph
                          + "\")"
                          + str.substring(m.end());
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
     * 
     * @return 解析好的属性
     */
    public static NutMap loadPropAndRemoveNode(Element ele, String className) {
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

        // 删除属性
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
     * @param str
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

    // =================================================================
    // 不许实例化
    private Hms() {}

    /**
     * 根据给定的内容给页面对象设置元数据
     * 
     * @param sys
     *            系统上下文
     * @param oPage
     *            页面对象
     * @param content
     *            内容
     */
    public static void syncPageMeta(WnSystem sys, WnObj oPage, String content) {
        Set<String> libNames = new HashSet<>();
        if (!Strings.isBlank(content)) {
            Document doc = Jsoup.parse(content);
            Elements eleLibs = doc.body().select(".hm-com[lib]");
            for (Element ele : eleLibs) {
                libNames.add(ele.attr("lib"));
            }
        }

        // 得到站点
        WnObj oSiteHome = getSiteHome(sys, oPage);
        oPage.setv("hm_site_id", oSiteHome == null ? null : oSiteHome.id());

        // .................................................
        // 保存元数据索引
        oPage.setv("hm_libs", libNames);
        sys.io.set(oPage, "^hm_(site_id|libs)$");
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
}
