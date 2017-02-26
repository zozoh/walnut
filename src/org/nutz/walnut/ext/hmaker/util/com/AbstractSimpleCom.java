package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

/**
 * 简单控件，即不能再有子控件的控件。因此绘制的时候有下面的特点:
 * 
 * <ul>
 * <li>总是要被清空重新绘制
 * <li>其 DOM 结构总是为 ELE > eleArena > 内容
 * <li>其 ELE 就是为了应用 位置宽高等 css
 * <li>其皮肤的属性，总是要记录在 eleArena 上
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class AbstractSimpleCom extends AbstractCom {

    protected abstract String getArenaClassName();

    protected abstract void doArena(HmPageTranslating ing, Element eleArena);

    protected String blockCssPropNames;

    protected AbstractSimpleCom() {
        this.blockCssPropNames = "^(position|top|left|right|bottom|width|height|margin)";
    }

    @Override
    protected void _exec(HmPageTranslating ing) {
        // ...........................................
        // 处理 DOM
        ing.eleCom.empty();
        Element eleArena = ing.eleCom.appendElement("DIV").addClass(this.getArenaClassName());

        // 子类的处理
        this.doArena(ing, eleArena);

        // ...........................................
        // 处理 CSS: 挑选整体的属性先
        

        // 挑选 ELE 的 CSS

        // 挑选 Arena 的 CSS

    }

}
