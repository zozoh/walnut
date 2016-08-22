package org.nutz.walnut.ext.hmaker.util.com;

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
        // 读取属性
        ing.prop = new NutMap();
        Hms.fillProp(ing.prop, ing.eleCom, "hmc-prop-ele").remove();

        // 设置 arena
        ing.eleArena = ing.eleCom.children().first();

        // 记录当前控件的 ID
        ing.comId = ing.prop.getString("_id");

        // 调用子类
        this._exec(ing);

    }

    protected abstract void _exec(HmPageTranslating ing);
}
