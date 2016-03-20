package org.nutz.walnut.impl.box;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;

public class JvmBoxOutput implements Flushable, Closeable {

    private OutputStream ops;

    private Writer _w;

    public JvmBoxOutput(OutputStream ops) {
        this.ops = ops;
    }

    public OutputStream getOutputStream() {
        return ops;
    }

    public Writer getWriter() {
        if (null == _w) {
            _w = Streams.utf8w(ops);
        }
        return _w;
    }

    public Writer getWriter(String charset) {
        if (null == _w) {
            try {
                _w = new OutputStreamWriter(ops, charset);
            }
            catch (UnsupportedEncodingException e) {
                throw Lang.wrapThrow(e);
            }
        }
        return _w;
    }

    public void write(InputStream ins) {
        try {
            Streams.write(ops, ins);
            ops.flush();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) {
        try {
            ops.write(b, off, len);
            ops.flush();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    public void println() {
        print("\n");
    }

    public void println(Object obj) {
        if (null == obj) {
            print("null\n");
        }
        // 字符串
        else if (obj instanceof CharSequence) {
            print(obj + "\n");
        }
        // 其他对象
        else {
            print(Castors.me().castToString(obj) + "\n");
        }
    }

    public void printlnf(String fmt, Object... args) {
        print(String.format(fmt, args) + "\n");
    }

    public void writeJson(Object o, JsonFormat fmt) {
        Writer w = getWriter();
        Json.toJson(w, o, fmt);
        try {
            w.flush();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    public void writeJson(Object o) {
        writeJson(o, JsonFormat.forLook());
    }

    public void printf(String fmt, Object... args) {
        print(String.format(fmt, args));
    }

    public void print(CharSequence msg) {
        Writer w = getWriter();
        try {
            w.write(null == msg ? "null" : msg.toString());
            w.flush();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    @Override
    public void flush() {
        Streams.safeFlush(_w);
        Streams.safeFlush(ops);
    }

    @Override
    public void close() {
        flush();
        Streams.safeClose(ops);
    }

}
