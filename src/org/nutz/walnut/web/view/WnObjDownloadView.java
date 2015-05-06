package org.nutz.walnut.web.view;

import java.io.DataInputStream;

import org.nutz.mvc.view.RawView2;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;

public class WnObjDownloadView extends RawView2 {

    public WnObjDownloadView(WnIo io, WnObj o) {
        contentType = o.mime();
        in = new DataInputStream(io.getInputStream(o, 0));
        maxLen = (int) o.len();

    }

}
