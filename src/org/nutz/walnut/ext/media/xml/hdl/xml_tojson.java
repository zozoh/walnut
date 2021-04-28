package org.nutz.walnut.ext.media.xml.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class xml_tojson implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 首先读取源数据
        String xml = Cmds.checkParamOrPipe(sys, hc.params, 0);

        // 如果不是 XML 的话，就应该是文件的路径
        if (!Strings.isQuoteBy(xml, '<', '>')) {
            WnObj o = Wn.checkObj(sys, xml);
            xml = sys.io.readText(o);
        }

        // 检查转换
        NutMap map = Xmls.xmlToMap(xml);

        // 输出 JSON 字符串
        sys.out.println(Json.toJson(map, hc.jfmt));
    }

}
