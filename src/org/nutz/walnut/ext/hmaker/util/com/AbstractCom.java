package org.nutz.walnut.ext.hmaker.util.com;

import java.util.Map;

import org.jsoup.nodes.Element;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.util.HmComHandler;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

/**
 * 所有控件类处理类的基类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class AbstractCom implements HmComHandler {

    @Override
    public void invoke(HmPageTranslating ing) {
        // 分析布局属性
        ing.propBlock = Hms.loadPropAndRemoveNode(ing.eleCom, "hm-prop-block");

        // 分析布局属性
        this.__prepare_block_css(ing);

        // 分析控件属性
        ing.propCom = Hms.loadPropAndRemoveNode(ing.eleCom, "hm-prop-com");

        // 记录当前控件的 ID
        ing.comId = ing.eleCom.attr("id");

        // 调用子类
        this._exec(ing);

        // 最后统一清除一些属性
        ing.eleCom.getElementsByAttribute("del-attrs").removeAttr("del-attrs");
        ing.eleCom.getElementsByClass("ui-arena").removeClass("ui-arena");

    }

    private void __prepare_block_css(HmPageTranslating ing) {
        // 准备 css 对象
        ing.cssEle = new NutMap();
        ing.cssArena = new NutMap();
        ing.skinAttributes = new NutMap();

        for (Map.Entry<String, Object> en : ing.propBlock.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();

            // 皮肤属性
            if (key.startsWith("sa-")) {
                ing.skinAttributes.put(key, val);
            }
            // 位置也是属于顶级块的
            else if ("mode".equals(key)) {
                if ("abs".equals(val)) {
                    ing.cssEle.put("position", "absolute");
                }
            }
            // 顶级块的 css
            else if (key.matches("^(mode|posBy|top|left|right|bottom|width|height|margin)")) {
                ing.cssEle.put(key, val);
            }
            // 其他的就属于内容区域的 CSS
            else if (!key.equals("posBy")) {
                ing.cssArena.put(key, val);
            }
        }

        // 对于绝对位置，绝对位置的话，应该忽略 margin
        if ("absolute".equals(ing.cssEle.getString("position"))) {
            // TODO 这里弄一下位置 posBy : "TLWH"
        }

        // 修正 css 的宽高
        if (ing.cssEle.is("width", "unset"))
            ing.cssEle.remove("width");
        if (ing.cssEle.is("height", "unset"))
            ing.cssEle.remove("height");

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

    @Override
    public boolean isDynamic(Element eleCom) {
        return false;
    }

}
