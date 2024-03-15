package org.nutz.mock.servlet.multipart.inputing;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.stream.StringInputStream;

public class StringInputing implements Inputing {

    private InputStream ins;

    StringInputing(String str) {
        ins = Wlang.ins(str);
    }
    
    StringInputing(String str, Charset charset) {
        ins = new StringInputStream(str, charset);
    }

    public int read() {
        try {
            return ins.read();
        }
        catch (IOException e) {
            throw Wlang.wrapThrow(e);
        }
    }

    public long size() {
        try {
            return ins.available();
        }
        catch (IOException e) {
            throw Wlang.wrapThrow(e);
        }
    }

    public void close() throws IOException {}

    public void init() throws IOException {}

}
