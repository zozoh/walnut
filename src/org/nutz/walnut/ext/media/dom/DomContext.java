package org.nutz.walnut.ext.media.dom;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.xml.CheapXmlParsing;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

public class DomContext extends JvmFilterContext {

    public CheapDocument doc;

    public List<CheapElement> selected;

    /**
     * 某些过滤器，譬如<code>@as</cod>，可以标识这个属性，阻止主程序的自动输出
     */
    public boolean quiet;

    public void loadHtml(String[] paths, boolean fromPip, boolean body) {
        CheapXmlParsing ing = new CheapXmlParsing();

        // 读取 HTML 输入
        String html;
        // 从文件读取
        if (paths.length > 0) {
            String ph = paths[0];
            WnObj oHtml = Wn.checkObj(sys, ph);
            html = sys.io.readText(oHtml);
        }
        // 从标准输入读取
        else if (fromPip) {
            html = Ws.trim(sys.in.readAll());
            if (Ws.isBlank(html)) {
                return;
            }
        }
        // 那就无视咯
        else {
            return;
        }

        // 开始解析
        this.doc = ing.parseDoc(html);
        this.selected = new LinkedList<>();
    }

    public boolean hasSelected() {
        return null != selected && !selected.isEmpty();
    }

}
