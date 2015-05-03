package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;

public abstract class BaseBoxTest extends BaseUsrTest {

    protected WnBoxService boxes;

    protected WnBox box;

    protected StringBuilder out;

    protected StringBuilder err;

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

        out = new StringBuilder();
        err = new StringBuilder();

        boxes = _create_box_service(pp);

        box = boxes.alloc(0);
        box.setStdin(null);
        box.setStdout(Lang.ops(out));
        box.setStderr(Lang.ops(err));

        WnBoxContext bc = new WnBoxContext();
        bc.io = io;
        bc.me = usrs.create("xiaobai", "123456");
        bc.session = ses.login("xiaobai", "123456");
        bc.usrService = usrs;
        bc.sessionService = ses;

        box.setup(bc);
    }

    @Override
    protected void on_after(PropertiesProxy pp) {
        boxes.free(box);
        super.on_after(pp);
    }

    protected abstract WnBoxService _create_box_service(PropertiesProxy pp);

}
