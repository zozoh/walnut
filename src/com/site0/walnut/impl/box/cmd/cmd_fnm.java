package com.site0.walnut.impl.box.cmd;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_fnm extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);

        String str = params.val(0);
        String tmplStr = params.check("tmpl");

        WnTmpl tmpl = WnTmpl.parse(tmplStr, "@");
        WnObj current = Strings.isBlank(str) ? sys.getCurrentObj() : Wn.checkObj(sys, str);

        int i = 1;
        NutMap context = Lang.map("n", i);
        String fnm = tmpl.render(context);

        while (true) {
            if (sys.io.exists(current, fnm)) {
                i++;
                context.put("n", i);
                fnm = tmpl.render(context);
                continue;
            }
            break;
        }

        sys.out.print(fnm);
        if (params.is("N"))
            sys.out.println();
    }

}
