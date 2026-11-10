package com.site0.walnut.web2.controller.app;

import java.io.IOException;
import java.io.Writer;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.trans.Atom;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

public class WnAppCommandCallback implements Callback<WnBoxContext> {

    private String metaOutputSeparator;
    private Writer w;
    private WnSession session;
    private Atom atom;

    WnAppCommandCallback(WnSession session, Writer w, String metaOutputSeparator, Atom atom) {
        this.session = session;
        this.w = w;
        this.metaOutputSeparator = metaOutputSeparator;
        this.atom = atom;
    }

    @Override
    public void invoke(WnBoxContext bc) {
        if (!Strings.isBlank(metaOutputSeparator)) {
            try {
                w.write("\n" + metaOutputSeparator + ":MACRO:" + Wn.MACRO.UPDATE_ENVS + "\n");
                w.write(Json.toJson(session.getEnv()));
                w.flush();
                if (bc.attrs.has(Wn.MACRO.CHANGE_SESSION)) {
                    String json = Json.toJson(bc.attrs.get(Wn.MACRO.CHANGE_SESSION),
                                              JsonFormat.compact());
                    w.write("\n" + metaOutputSeparator + ":MACRO:" + Wn.MACRO.CHANGE_SESSION + "\n");
                    w.write(json);
                    w.flush();
                }
            }
            catch (IOException e) {
                throw Wlang.wrapThrow(e);
            }
        }
        if (atom != null) {
            atom.run();
        }
    }
}
