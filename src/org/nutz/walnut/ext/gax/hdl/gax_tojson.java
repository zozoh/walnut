package org.nutz.walnut.ext.gax.hdl;

import java.io.ByteArrayInputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.Xmls;
import org.nutz.lang.Xmls.XmlParserOpts;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class gax_tojson implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String text = null;
        if (sys.pipeId > 0) {
            text = sys.in.readAll();
        }
        else {
            text = sys.io.readText(sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys)));
        }
        ByteArrayInputStream ins = new ByteArrayInputStream(text.getBytes());
        XmlParserOpts opts = new XmlParserOpts();
        opts.setDupAsList(true);
        opts.setLowerFirst(true);
        opts.setAttrAsKeyValue(true);
        opts.setAlwaysAsList(Lang.list("trkpt", "wpt"));
        NutMap map = Xmls.asMap(Xmls.xml(ins).getDocumentElement(), opts);
        sys.out.writeJson(map, Cmds.gen_json_format(hc.params));
    }

}
