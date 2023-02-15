package org.nutz.walnut.ext.net.mailx.hdl;

import org.nutz.walnut.ext.net.mailx.MailxContext;
import org.nutz.walnut.ext.net.mailx.MailxFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class mailx_to extends MailxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, null);
    }

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        String str = Ws.join(params.vals, ";");
        fc.builder.to(str);
    }

}
