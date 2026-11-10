package com.site0.walnut.web2.view;

import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.util.Wlang;

import java.io.IOException;
import java.io.InputStream;

import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;

public class WnDownloadView implements WnMvcView {

    private WnHttpResponseWriter re;

    @Override
    public void render(WnMvcContext ctx) {
        try {
            re.writeTo(ctx.response());
        }
        catch (IOException e) {
            throw Wlang.wrapThrow(e);
        }
    }

    public WnDownloadView(WnIo io,
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

    public WnDownloadView(InputStream ins,
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

    public WnDownloadView(byte[] buf,
                             String ua,
                             String mimeType,
                             String downloadName,
                             String etag,
                             String range) {
        this.re = new WnHttpResponseWriter();
        this.re.setEtag(etag);
        this.re.setContentType(mimeType);
        this.re.setDownloadName(downloadName);
        this.re.setUserAgent(ua);
        this.re.prepare(buf);
    }

    public WnDownloadView(byte[] buf,
                             String bufEtag,
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
        this.re.prepare(buf, bufEtag);
    }

}
