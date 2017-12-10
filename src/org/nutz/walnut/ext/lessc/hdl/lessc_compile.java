package org.nutz.walnut.ext.lessc.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.lessc.WnLesscService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class lessc_compile implements JvmHdl {

    protected WnLesscService lessc;

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // TODO 做个lessc池
        // 如果还没有lessc对象,生成一个
        if (lessc == null) {
            lessc = new WnLesscService(sys.io);
            lessc.init();
        }
        // lessc compile xxx.less , 把xxx.less转为绝对路径,取出来
        String path = Wn.normalizeFullPath(hc.params.val_check(0), sys);
        WnObj wobj = sys.io.check(null, path);
        WnObj base;
        // 支持一下include-path试试
        if (hc.params.has("include-path")) {
            base = sys.io.check(null, Wn.normalizeFullPath(hc.params.get("include-path"), sys));
        } else {
            // 默认就是wobj所在的目录咯
            base = wobj.parent();
        }
        // 来吧,渲染之
        sys.out.print(lessc.renderWnObj(wobj, base));
    }

}
