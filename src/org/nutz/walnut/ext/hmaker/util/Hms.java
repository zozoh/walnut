package org.nutz.walnut.ext.hmaker.util;

import java.util.Map;

import org.nutz.lang.Maths;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

/**
 * 帮助函数集
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public final class Hms {

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
    public static String genCssRule(Map<String, Object> rule, int mode) {
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
            }

            // 拼装
            if (multiline)
                re += "\n  ";
            re += autoLower ? Strings.lowerWord(key, '-') : key;
            re += ":" + str + ";";
        }

        // 结尾
        if (multiline)
            re += '\n';
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
    public static String genCssRuleStyle(Map<String, Object> rule) {
        return genCssRule(rule, 0);
    }

    /**
     * 生成多行的 CSS 样式文本，用大括号包裹。同时会自动将驼峰命名的样式名转成中划线分隔
     * 
     * @param rule
     *            Map 描述的规则
     * @return 样式文本
     * @see #genCssRule(NutMap, int)
     */
    public static String genCssRuleText(Map<String, Object> rule) {
        return genCssRule(rule, AUTO_LOWER | WRAP_BRACE | RULE_MULTILINE);
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
    @SuppressWarnings("unchecked")
    public static String genCssText(NutMap css, String prefix) {
        prefix = Strings.isBlank(prefix) ? "" : prefix + " ";
        String re = "";
        for (Map.Entry<String, Object> en : css.entrySet()) {
            String selector = en.getKey();
            Object val = en.getValue();
            if (val instanceof Map<?, ?>) {
                NutMap rule = NutMap.WRAP((Map<String, Object>) val);
                re += prefix + selector;
                re += genCssRuleText(rule);
                re += "\n";
            }
        }
        return re;
    }

    public static String escapeHtmlNewline(String html) {
        return html.replace("\n", "\\hm:%N").replace(" ", "\\hm:%W");
    }

    public static String unescapeHtmlNewline(String html) {
        return html.replace("\\hm:%N", "\n").replace("\\hm:%W", " ");
    }

    public static String wrapjQueryDocumentOnLoad(String script) {
        return "$(function(){" + script + "});";
    }

    // =================================================================
    // 不许实例化
    private Hms() {}
}
