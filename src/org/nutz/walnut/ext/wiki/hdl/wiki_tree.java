package org.nutz.walnut.ext.wiki.hdl;

import org.nutz.plugins.zdoc.NutDSet;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.wiki.WikiService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.web.Webs.Err;

@JvmHdlParamArgs("^(json|xml|c|n|q)$")
public class wiki_tree implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnObj wobj = Wn.getObj(sys, hc.params.val_check(0));
        NutDSet dset = new NutDSet(wobj.name());
        hc.ioc.get(WikiService.class).tree(wobj, dset, false);
        if (hc.params.is("xml")) {
            throw Err.create("e.cmd.wiki_tree.xml_output_not_support_yet");
        }
        sys.out.writeJson(dset, Cmds.gen_json_format(hc.params));
    }

}
