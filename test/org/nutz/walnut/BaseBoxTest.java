package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;

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

    @Override
    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        boxes = _create_box_service(pp);

        me = usrs.create("xiaobai", "123456");
        se = ses.create(me);

        out = new StringBuilder();
        err = new StringBuilder();

        bc = new WnBoxContext();
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
        super.on_after(pp);
    }

    protected abstract WnBoxService _create_box_service(PropertiesProxy pp);

}
