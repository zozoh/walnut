package com.site0.walnut.ext.data.o;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_o extends JvmFilterExecutor<OContext, OFilter> {

    public cmd_o() {
        super(OContext.class, OFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl", "^(noexists)$");
    }

    @Override
    protected OContext newContext() {
        return new OContext();
    }

    @Override
    protected void prepare(WnSystem sys, OContext fc) {
        fc.keepAsList = fc.params.is("l");
        fc.subKey = fc.params.getString("subkey", "children");

        int mode = 0;
        String noexists = fc.params.get("noexists");
        if (!Strings.isBlank(noexists)) {
            if ("null".equals(noexists)) {
                mode |= Wn.Cmd.NOEXISTS_NULL;
            } else {
                mode |= Wn.Cmd.NOEXISTS_IGNORE;
            }
        }

        if (fc.params.vals.length > 0) {
            Cmds.evalCandidateObjs(sys, fc.params.vals, fc.list, mode);
        }
    }

    @Override
    protected void output(WnSystem sys, OContext fc) {
        if (!fc.quiet) {
            Object reo = fc.toOutput(true);

            // 输出
            String json = Json.toJson(reo, fc.jfmt);
            sys.out.print(json);
        }
    }

}
