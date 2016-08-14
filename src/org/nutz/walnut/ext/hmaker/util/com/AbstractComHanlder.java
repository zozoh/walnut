package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.hmaker.util.HmComHandler;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

/**
 * 所有控件类处理类的基类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class AbstractComHanlder implements HmComHandler {

    @Override
    public void invoke(HmPageTranslating ing) {
        // 找到属性节点 ...
        Element eleProp = ing.eleCom.child(0);

        // 这玩意必须是个 script 吧
        if (!eleProp.tagName().equals("script") || !eleProp.hasClass("hmc-th-prop-ele")) {
            throw Er.create("e.cmd.hmaker.publish.invalidEleProp",
                            ing.oSrc.path() + ":\n" + eleProp.outerHtml());
        }

        // 嗯，读完了属性它没用了，删了吧
        String json = eleProp.html();
        ing.prop = Json.fromJson(NutMap.class, json);
        eleProp.remove();

        // 记录当前控件的 ID
        ing.comId = ing.prop.getString("_id");

        // 调用子类
        this._exec(ing);

    }

    protected abstract void _exec(HmPageTranslating ing);
}
