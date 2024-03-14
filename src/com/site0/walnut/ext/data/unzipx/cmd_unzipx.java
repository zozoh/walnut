package com.site0.walnut.ext.data.unzipx;

import java.nio.charset.Charset;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_unzipx extends JvmFilterExecutor<UnzipxContext, UnzipxFilter> {
    public cmd_unzipx() {
        super(UnzipxContext.class, UnzipxFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl", "^(noexists|macosx|hidden)$");
    }

    @Override
    protected UnzipxContext newContext() {
        return new UnzipxContext();
    }

    @Override
    protected void prepare(WnSystem sys, UnzipxContext fc) {
        String ph = fc.params.val_check(0);
        fc.oZip = Wn.checkObj(sys, ph);
        String charsetName = fc.params.getString("charset", "UTF-8");
        fc.charset = Charset.forName(charsetName);

        fc.hidden = fc.params.is("hidden");
        fc.macosx = fc.params.is("macosx");
    }

    @Override
    protected void output(WnSystem sys, UnzipxContext fc) {}
}
