package org.nutz.walnut.ext.media.tpassport.hdl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.media.tpassport.TPassport;
import org.nutz.walnut.ext.media.tpassport.TPassportDrawItem;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class tpassport_create implements JvmHdl {
    
    private static final Log log = Wlog.getCMD();

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
        if (hc.params.has("dst")) {
            BufferedImage image = tp.getImage();
            WnObj dst = sys.io.createIfNoExists(null, Wn.normalizeFullPath(hc.params.get("dst"), sys), WnRace.FILE);
            sys.io.appendMeta(dst, new NutMap("mime", "image/jpeg"));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Images.writeJpeg(image, out, 0.9f);
            sys.io.writeAndClose(dst, new ByteArrayInputStream(out.toByteArray()));
        }
        sys.out.print("{\"ok\":true}");
    }

}
