package org.nutz.walnut.ext.hmaker.util.com;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.nutz.castor.Castors;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.hmaker.skin.HmSkinInfoCom;
import org.nutz.walnut.ext.hmaker.util.HmComHandler;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

/**
 * 所有控件类处理类的基类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class AbstractCom implements HmComHandler {

    protected abstract String getArenaClassName();

    @Override
    public void invoke(HmPageTranslating ing) {
        // 分析布局属性
        ing.propBlock = Hms.loadPropAndRemoveNode(ing.eleCom, "hm-prop-block");

        // 分析布局属性
        this.__prepare_block_css_and_skin_attributes(ing);

        // 分析控件属性
        ing.propCom = Hms.loadPropAndRemoveNode(ing.eleCom, "hm-prop-com");

        // 记录当前控件的 ID
        ing.comId = ing.eleCom.attr("id");
        ing.comType = ing.eleCom.attr("ctype");

        // 确保标记本页为动态
        if (this.isDynamic(ing.eleCom))
            ing.markPageAsWnml();

        // 调用子类
        this._exec(ing);

        // 最后统一清除一些属性
        ing.eleCom.getElementsByAttribute("del-attrs").removeAttr("del-attrs");
        ing.eleCom.getElementsByClass("ui-arena").removeClass("ui-arena");

    }

    protected String[] skinAttributes;

    /**
     * 同步皮肤的自定义属性
     * 
     * @param ing
     *            运行时
     * @param eleArena
     *            Arena 元素
     * @param skin
     *            皮肤选择器
     */
    protected void syncComSkinAttributes(HmPageTranslating ing, Element eleArena, String skin) {
        if (null != this.skinAttributes && this.skinAttributes.length > 0) {
            String ctype = ing.comType;
            HmSkinInfoCom sic = ing.skinInfo != null ? ing.skinInfo.getSkinForCom(ctype, skin)
                                                     : null;
            // 直接都移除
            if (null == sic || null == sic.attributes || sic.attributes.isEmpty()) {
                for (String sa : skinAttributes) {
                    eleArena.removeAttr(sa);
                }
            }
            // 挨个判断一下
            else {
                for (String sa : skinAttributes) {
                    String val = sic.attributes.getString(sa);
                    if (Strings.isBlank(val))
                        eleArena.removeAttr(sa);
                    else
                        eleArena.attr(sa, val);
                }
            }
        }
    }

    private void __prepare_block_css_and_skin_attributes(HmPageTranslating ing) {
        // 生成顶级元素的 CSS: 这个逻辑会顺便移除位置相关的属性
        ing.cssEle = __gen_cssEle_for_mode(ing);

        // 准备 css 对象
        // ing.cssArena = ing.cssEle.pick("width", "height");
        ing.cssArena = new NutMap();
        ing.skinAttributes = new NutMap();

        // 如果控件设置了宽高，那么 arena 要用 100% 适应
        if (ing.cssEle.has("width"))
            ing.cssArena.put("width", "100%");
        if (ing.cssEle.has("height"))
            ing.cssArena.put("height", "100%");

        // 处理块属性
        Pattern p = Pattern.compile("^#([BCL])>(.+)$");
        for (Map.Entry<String, Object> en : ing.propBlock.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();

            // 空值忽略
            if (null == val)
                continue;

            // 皮肤属性
            if (key.startsWith("sa-")) {
                ing.skinAttributes.put(key, val);
            }
            // _font
            else if ("_font".equals(key)) {
                String[] ff = Castors.me().castTo(val, String[].class);
                if (Lang.contains(ff, "underline")) {
                    ing.cssArena.put("textDecoration", "underline");
                } else if (Lang.contains(ff, "bold")) {
                    ing.cssArena.put("fontWeight", "bold");
                } else if (Lang.contains(ff, "italic")) {
                    ing.cssArena.put("fontStyle", "italic");
                }
            }
            // 那么必然是样式
            else if (!key.startsWith("_")) {
                // 自定义样式
                Matcher m = p.matcher(key);
                if (m.find()) {
                    String propType = m.group(1);
                    String selector = m.group(2);
                    String propName;
                    if ("B".equals(propType)) {
                        propName = "background";
                    } else if ("C".equals(propType)) {
                        propName = "color";
                    } else {
                        propName = "border-color";
                    }
                    ing.addMySkinRule(selector, Lang.map(propName, val));
                }
                // 其他的就属于内容区域的 CSS
                else {
                    ing.cssArena.put(key, val);
                }
            }
        }

    }

    private NutMap __gen_cssEle_for_mode(HmPageTranslating ing) {
        NutMap re;

        // 首先取得要处理的相关的位置属性
        NutMap prop = ing.propBlock.pickAndRemove("mode",
                                                  "posBy",
                                                  "top",
                                                  "left",
                                                  "bottom",
                                                  "right",
                                                  "width",
                                                  "height",
                                                  "margin");

        // 处理顶级块的 CSS
        String mode = prop.getString("mode");
        // 对于绝对位置，绝对位置的话，应该忽略 margin
        if ("abs".equals(mode)) {
            // 绝对位置的话，就不要 margin 了
            ing.propBlock.remove("margin");

            // 看看需要挑选什么样的尺寸属性
            String regex;
            String posBy = prop.getString("posBy", "TLWH");
            switch (posBy) {
            case "TLWH":
                regex = "^(top|left|width|height)$";
                break;
            case "TRWH":
                regex = "^(top|right|width|height)$";
                break;
            case "LBWH":
                regex = "^(left|bottom|width|height)$";
                break;
            case "BRWH":
                regex = "^(bottom|right|width|height)$";
                break;
            case "TLBR":
                regex = "^(top|left|bottom|right)$";
                break;
            case "TLBW":
                regex = "^(top|left|bottom|width)$";
                break;
            case "TBRW":
                regex = "^(top|bottom|right|width)$";
                break;
            case "TLRH":
                regex = "^(top|left|right|height)$";
                break;
            case "LBRH":
                regex = "^(left|bottom|right|height)$";
                break;
            default:
                throw Er.createf("e.cmd.hmaker.posBy",
                                 "'%s' @ #%s(%s): %s",
                                 posBy,
                                 ing.eleCom.attr("id"),
                                 ing.eleCom.attr("ctype"),
                                 ing.oSrc.path());
            }
            re = prop.pickBy(Pattern.compile(regex), false);
            re.put("position", "absolute");
        }
        // 相对位置
        else {
            re = prop.pick("width", "height", "margin");
        }

        // 修正 css 的宽高
        if (re.is("width", "unset"))
            re.remove("width");
        if (re.is("height", "unset"))
            re.remove("height");

        // 返回
        return re;
    }

    // protected void applyBlockCss(HmPageTranslating ing) {
    // Pattern p =
    // Pattern.compile("^(position|top|left|right|bottom|width|height|border|margin)$");
    //
    // NutMap cssCom = ing.cssBlock.pickBy(p, false);
    // NutMap cssArena = ing.cssBlock.pickBy(p, true);
    //
    // ing.addMyCss(Lang.map("", cssCom).setv(">div", cssArena));
    // }

    protected abstract void _exec(HmPageTranslating ing);

}
