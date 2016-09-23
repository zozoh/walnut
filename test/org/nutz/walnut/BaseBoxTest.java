package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrInfo;
import org.nutz.walnut.impl.box.JvmBoxService;
import org.nutz.walnut.impl.box.JvmExecutorFactory;
import org.nutz.walnut.util.Wn;

public abstract class BaseBoxTest extends BaseUsrTest {

    protected WnBoxService boxes;

    protected WnBox box;

    protected StringBuilder out;

    protected StringBuilder err;

    protected WnUsr me;

    protected WnSession se;

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

    private String __old_me;
    private String __old_grp;

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

        me = usrs.create(new WnUsrInfo("xiaobai"));
        usrs.setPassword(me, "123456");
        se = ses.create(me);

        // 将测试线程切换到当前测试账号
        __old_me = Wn.WC().checkMe();
        __old_grp = Wn.WC().checkGroup();
        Wn.WC().SE(se);

        out = new StringBuilder();
        err = new StringBuilder();

        bc = new WnBoxContext(new NutMap());
        bc.io = io;
        bc.me = me;
        bc.session = se;
        bc.usrService = usrs;
        bc.sessionService = ses;

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
        Wn.WC().SE(null);
        Wn.WC().me(__old_me, __old_grp);
        super.on_after(pp);
    }

    private WnBoxService _create_box_service() {
        JvmExecutorFactory jef = new JvmExecutorFactory();
        Mirror.me(jef).setValue(jef, "scanPkgs", Lang.array("org.nutz.walnut.impl.box.cmd"));
        return new JvmBoxService(jef);
    }

}
