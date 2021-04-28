package org.nutz.walnut.ext.util.strx;

import org.nutz.lang.Strings;
import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_strx extends JvmFilterExecutor<StrXContext, StrXFilter> {

    public cmd_strx() {
        super(StrXContext.class, StrXFilter.class);
    }

    @Override
    protected StrXContext newContext() {
        return new StrXContext();
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "n", "^(trim)$");
    }

    @Override
    protected void prepare(WnSystem sys, StrXContext fc) {
        fc.data = sys.in.readAll();
        if (fc.params.is("trim")) {
            fc.data = Strings.trim(fc.data);
        }
    }

    @Override
    protected void output(WnSystem sys, StrXContext fc) {
        if (fc.error) {
            if (fc.params.is("n")) {
                sys.err.println(fc.data);
            } else {
                sys.err.print(fc.data);
            }
        } else {
            if (fc.params.is("n")) {
                sys.out.println(fc.data);
            } else {
                sys.out.print(fc.data);
            }
        }
    }

}
