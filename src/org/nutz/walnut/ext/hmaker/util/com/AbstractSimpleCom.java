package org.nutz.walnut.ext.hmaker.util.com;

import java.util.Map;

import org.jsoup.nodes.Element;
import org.nutz.lang.Strings;
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

    /**
     * 子类进行处理
     * 
     * @param ing
     *            上下文
     * @param eleArena
     *            要处理的 arena 元素
     * @return true 处理完毕。 false 这个控件有问题，需要删除
     */
    protected abstract boolean doArena(HmPageTranslating ing, Element eleArena);

    protected Element genArenaElement(HmPageTranslating ing, String arenaClassName) {
        return ing.eleCom.empty().appendElement("DIV").addClass(arenaClassName);
    }

    @Override
    protected void _exec(HmPageTranslating ing) {
        // ...........................................
        // 处理 DOM
        String arenaClassName = this.getArenaClassName();
        Element eleArena = this.genArenaElement(ing, arenaClassName);

        // 设置皮肤
        String skin = ing.eleCom.attr("skin");
        if (!Strings.isBlank(skin)) {
            eleArena.addClass(skin);
        }

        // 设置自定义皮肤选择器
        String selectors = ing.eleCom.attr("selectors");
        if (!Strings.isBlank(selectors)) {
            // eleArena.addClass(selectors);
            ing.eleCom.addClass(selectors);
        }

        // 同步皮肤属性开关
        this.syncComSkinAttributes(ing, eleArena, skin);

        // 添加皮肤属性
        // for (Map.Entry<String, Object> en : ing.skinAttributes.entrySet()) {
        // Object val = en.getValue();
        // if (null != val) {
        // if (val instanceof Boolean) {
        // if (!(Boolean) val)
        // continue;
        // }
        // eleArena.attr(en.getKey(), en.getValue().toString());
        // }
        // }

        // 子类的处理成功: 设置 css
        if (this.doArena(ing, eleArena)) {
            ing.addMyRule(null, ing.cssEle);
            ing.addMyRule("." + arenaClassName, ing.cssArena);
        }
        // 否则删除控件
        else {
            ing.eleCom.remove();
        }

    }

}
