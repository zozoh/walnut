package com.site0.walnut.ext.net.http.upload;

import java.io.IOException;
import java.io.InputStream;

import com.site0.walnut.util.Wlang;

public class HttpFormFieldInputStream extends InputStream {

    private HttpFormUploadField field;

    HttpFormFieldInputStream(HttpFormUploadField field) {
        this.field = field;
    }

    @Override
    public int read() throws IOException {
        throw Wlang.noImplement();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return field.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return field.read(b, off, len);
    }

}
