package org.nutz.walnut.web.view;

import java.io.DataInputStream;
import java.io.InputStream;

import org.nutz.lang.Strings;
import org.nutz.mvc.view.RawView2;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;

public class WnObjDownloadView extends RawView2 {

    public WnObjDownloadView(WnIo io, WnObj o) {
        this(io, o, o.mime());
    }

    public WnObjDownloadView(WnIo io, WnObj o, String mimeType) {
        this(io.getInputStream(o, 0), (int) o.len(), mimeType);
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
