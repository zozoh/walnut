package org.nutz.walnut.web.view;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.mvc.View;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.WnHttpResponseWriter;

public class WnObjDownloadView implements View {

    private WnHttpResponseWriter re;

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp, Object obj)
            throws Throwable {
        re.writeTo(resp);
    }

    public WnObjDownloadView(WnIo io,
                             WnObj o,
                             String downloadName,
                             String ua,
                             String etag,
                             String range) {
        this.re = new WnHttpResponseWriter();
        this.re.setDownloadName(downloadName);
        this.re.setEtag(etag);
        this.re.setUserAgent(ua);
        this.re.prepare(io, o, range);
    }

    public WnObjDownloadView(InputStream ins,
                             int len,
                             String ua,
                             String mimeType,
                             String downloadName,
                             String etag,
                             String range)
            throws IOException {
        this.re = new WnHttpResponseWriter();
        this.re.setEtag(etag);
        this.re.setContentType(mimeType);
        this.re.setDownloadName(downloadName);
        this.re.setUserAgent(ua);
        this.re.prepare(ins, len);
    }

    public WnObjDownloadView(byte[] buf,
                             String ua,
                             String mimeType,
                             String downloadName,
                             String etag,
                             String range)
            throws IOException {
        this.re = new WnHttpResponseWriter();
        this.re.setEtag(etag);
        this.re.setContentType(mimeType);
        this.re.setDownloadName(downloadName);
        this.re.setUserAgent(ua);
        this.re.prepare(buf);
    }

}
