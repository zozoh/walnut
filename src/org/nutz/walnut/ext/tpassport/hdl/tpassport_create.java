package org.nutz.walnut.ext.tpassport.hdl;

import java.awt.image.BufferedImage;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.tpassport.TPassport;
import org.nutz.walnut.ext.tpassport.TPassportDrawItem;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class tpassport_create implements JvmHdl {
    
    private static final Log log = Logs.get();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        TPassport tp = null;
        if (hc.params.vals.length > 0) {
            tp = sys.io.readJson(sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys)), TPassport.class);
        }
        else {
            tp = Json.fromJson(TPassport.class, sys.in.getReader());
        }
        if (tp == null) {
            sys.err.print("e.cmd.tpassport.create.source_null");
            return;
        }
        if (tp.getItems() == null || tp.getItems().isEmpty()) {
            sys.err.print("e.cmd.tpassport.create.need_items");
            return;
        }
        if (hc.params.has("data")) {
            String tmp = hc.params.get("data");
            NutMap data;
            if (tmp.startsWith("{")) {
                data = Json.fromJson(NutMap.class, tmp);
            }
            else {
                data = sys.io.readJson(sys.io.check(null, Wn.normalizeFullPath(tmp, sys)), NutMap.class);
            }
            for (TPassportDrawItem tpd : tp.getItems()) {
                String value = data.getString(tpd.name);
                if (!Strings.isBlank(value)) {
                    tpd.content = value;
                }
            }
        }
        tp.setSys(sys);
        try {
            tp.prepare();
            tp.render();
        }
        catch (Throwable e) {
            sys.err.print("e.cmd.tpassport.create.error_" + e.getMessage());
            log.info("tpassport something happen", e);
            return;
        }
        finally {
            tp.finish();
        }
        BufferedImage image = tp.getImage();
        if (hc.params.has("dst")) {
            WnObj dst = sys.io.createIfNoExists(null, Wn.normalizeFullPath(hc.params.get("dst"), sys), WnRace.FILE);
            sys.io.writeImage(dst, image);
        }
        sys.out.print("{\"ok\":true}");
    }

}