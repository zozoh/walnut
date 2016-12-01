package org.nutz.walnut.ext.hmaker.util.com;

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
public abstract class AbstractComHanlder implements HmComHandler {

    @Override
    public void invoke(HmPageTranslating ing) {
        // 分析布局属性
        ing.propBlock = Hms.loadPropAndRemoveNode(ing.eleCom, "hm-prop-block");

        // 应用这个布局 CSS
        this.__prepare_block_css(ing);

        // 分析控件属性
        ing.propCom = Hms.loadPropAndRemoveNode(ing.eleCom, "hm-prop-com");

        // 记录当前控件的 ID
        ing.comId = ing.eleCom.attr("id");

        // 调用子类
        this._exec(ing);

    }

    private void __prepare_block_css(HmPageTranslating ing) {
        // 准备 css 对象
        ing.cssBlock = new NutMap();

        // 对于绝对位置，绝对位置的话，应该忽略 margin
        if ("abs".equals(ing.propBlock.getString("mode"))) {
            ing.cssBlock.putAll(ing.propBlock.pickBy("!^(mode|posBy)$"));
            ing.cssBlock.put("position", "absolute");
        }
        // 相对位置
        else {
            ing.cssBlock.putAll(ing.propBlock.pickBy("!^(mode|posBy|top|left|right|bottom)$"));
            ing.cssBlock.remove("position");
        }

        // 修正 css 的宽高
        if (ing.cssBlock.is("width", "unset"))
            ing.cssBlock.remove("width");
        if (ing.cssBlock.is("height", "unset"))
            ing.cssBlock.remove("height");

        // 应用这个修改
        // this.applyBlockCss(ing);
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
