package com.site0.walnut.impl.box;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.stream.VoidInputStream;

public class JvmBoxInput implements Closeable {

    private InputStream ins;

    private BufferedReader __r;

    public JvmBoxInput(InputStream ins) {
        ins = null == ins ? new VoidInputStream() : ins;
        this.ins = ins;
        this.__r = Streams.buffr(new InputStreamReader(ins, Encoding.CHARSET_UTF8));
    }

    public BufferedReader getReader() {
        return __r;
    }

    public InputStream getInputStream() {
        return ins;
    }

    public String readLine() {
        try {
            return __r.readLine();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    public String readAll() {
        try {
            return Streams.read(__r).toString();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    public int read(byte[] b) throws IOException {
        return ins.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return ins.read(b, off, len);
    }

    public void close() throws IOException {
        Streams.safeClose(__r);
    }

}
