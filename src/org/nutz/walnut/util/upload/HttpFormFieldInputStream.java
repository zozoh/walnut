package org.nutz.walnut.util.upload;

import java.io.IOException;
import java.io.InputStream;

import org.nutz.lang.Lang;

public class HttpFormFieldInputStream extends InputStream {

    private HttpFormField field;

    HttpFormFieldInputStream(HttpFormField field) {
        this.field = field;
    }

    @Override
    public int read() throws IOException {
        throw Lang.noImplement();
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
