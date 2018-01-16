package org.nutz.walnut.ext.hmaker.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.hmaker.util.Hms;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("ocqn")
public class hmaker_save implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到页面对象
        WnObj oPage = Wn.checkObj(sys, hc.params.val_check(0));
        // .................................................
        // 得到要写入的内容
        String content = hc.params.get("content");
        // 从标准输入得到内容
        if ("true".equals(content) || Strings.isBlank(content)) {
            content = null;
        }
        // 从文件得到内容
        if (null == content && hc.params.has("file")) {
            WnObj o = Wn.checkObj(sys, hc.params.get("file"));
            content = sys.io.readText(o);
        }
        // 默认从标准输入读取
        if (null == content) {
            content = sys.in.readAll();
        }
        // .................................................
        // 执行写入
        sys.io.writeText(oPage, content);

        // .................................................
        // 分析页面内容看看都使用了哪些组件
        Hms.syncPageMeta(null, sys, oPage, content);

        // .................................................
        // 最后输出内容
        if (hc.params.is("o")) {
            sys.out.println(Json.toJson(oPage, hc.jfmt));
        }
    }

}
