package org.nutz.walnut.web.view;

import java.io.DataInputStream;
import java.io.InputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.mvc.view.RawView2;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.web.util.WnWeb;

public class WnObjDownloadView extends RawView2 {

    public WnObjDownloadView(WnIo io, WnObj o, String ua) {
        this(io, o, o.mime(), ua);
    }

    public WnObjDownloadView(WnIo io, WnObj o, String mimeType, String ua) {
        this(o.len() == 0 ? Lang.ins("") : io.getInputStream(o, 0), (int) o.len(), mimeType);
        String nm = o.name();
        if (o.hasType() && !nm.endsWith("." + o.type())) {
            nm += "." + o.type();
        }
        this.CONTENT_DISPOSITION = WnWeb.genHttpRespHeaderContentDisposition(nm, ua);
    }

    public WnObjDownloadView(InputStream ins, int maxLen, String mimeType) {
        this(new DataInputStream(ins), maxLen, mimeType);
    }

    public WnObjDownloadView(DataInputStream input, int maxLen, String mimeType) {
        this.contentType = Strings.sBlank(mimeType, "text/plain");
        this.in = input;
        this.maxLen = maxLen;
    }

}
