package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmBoxService;
import org.nutz.walnut.impl.box.JvmExecutorFactory;
import org.nutz.walnut.util.Wn;

public abstract class BaseBoxTest extends BaseUsrTest {

    protected WnBoxService boxes;

    protected WnBox box;

    protected StringBuilder out;

    protected StringBuilder err;

    protected WnAccount me;

    protected WnAuthSession se;

    protected WnBoxContext bc;

    protected String outs() {
        return out.toString();
    }

    protected String touts() {
        return Strings.trim(out.toString());
    }

    protected String errs() {
        return err.toString();
    }

    protected String terrs() {
        return Strings.trim(err.toString());
    }

    private WnAccount __old_me;

    protected WnObj check(String ph) {
        String path = Wn.normalizeFullPath(ph, se);
        return io.check(null, path);
    }

    protected void cleanOutputAndErr() {
        out.delete(0, out.length());
        err.delete(0, err.length());
    }

    @Override
    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        boxes = _create_box_service();

        WnAccount info = new WnAccount();
        info.setName("xiaobai");
        info.setRawPasswd("123456");
        me = auth.createAccount(info);
        se = auth.createSession(me);

        // 将测试线程切换到当前测试账号
        __old_me = Wn.WC().getMe();
        Wn.WC().setSession(se);

        out = new StringBuilder();
        err = new StringBuilder();

        bc = new WnBoxContext(new NutMap());
        bc.io = io;
        bc.session = se;
        bc.auth = auth;

        box = _alloc_box();
    }

    protected WnBox _alloc_box() {
        WnBox box = boxes.alloc(0);
        box.setStdin(null);
        box.setStdout(Lang.ops(out));
        box.setStderr(Lang.ops(err));
        box.setup(bc);
        return box;
    }

    @Override
    protected void on_after(PropertiesProxy pp) {
        boxes.free(box);
        Wn.WC().setSession(null);
        Wn.WC().setMe(__old_me);
        super.on_after(pp);
    }

    private WnBoxService _create_box_service() {
        JvmExecutorFactory jef = new JvmExecutorFactory();
        Mirror.me(jef).setValue(jef, "scanPkgs", Lang.array("org.nutz.walnut.impl.box.cmd"));
        return new JvmBoxService(jef);
    }

}
