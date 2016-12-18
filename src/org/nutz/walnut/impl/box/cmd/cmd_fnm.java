package org.nutz.walnut.impl.box.cmd;

import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_fnm extends JvmExecutor {

    private static final Pattern _P = Pattern.compile("((?<![@])[@][{]([^}]+)[}])|([@]([@][{][^}]+[}]))");

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);

        String str = params.val(0);
        String tmplStr = params.check("tmpl");

        Tmpl tmpl = Tmpl.parse(tmplStr, _P, 2, 4);
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
