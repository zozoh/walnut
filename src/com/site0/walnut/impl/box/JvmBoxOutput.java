package com.site0.walnut.impl.box;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;
import org.nutz.lang.stream.VoidOutputStream;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.api.WnOutputable;

public class JvmBoxOutput implements WnOutputable {

    private static final Log log = Wlog.getBOX();

    private OutputStream ops;

    private Writer _w;

    public JvmBoxOutput(OutputStream ops) {
        ops = null == ops ? new VoidOutputStream() : ops;
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
                throw Wlang.wrapThrow(e);
            }
        }
        return _w;
    }

    @Override
    public void write(InputStream ins) {
        try {
            Streams.write(ops, ins);
            ops.flush();
        }
        catch (org.eclipse.jetty.io.EofException e) {
            if (log.isDebugEnabled())
                log.debug("EofException cached");
        }
        catch (IOException e) {
            throw Wlang.wrapThrow(e);
        }
    }

    @Override
    public void writeAndClose(InputStream ins) {
        try {
            Streams.write(ops, ins);
            ops.flush();
        }
        catch (org.eclipse.jetty.io.EofException e) {
            if (log.isDebugEnabled())
                log.debug("EofException cached");
        }
        catch (IOException e) {
            throw Wlang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    @Override
    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        try {
            ops.write(b, off, len);
            ops.flush();
        }
        catch (org.eclipse.jetty.io.EofException e) {
            if (log.isDebugEnabled())
                log.debug("EofException cached");
        }
        catch (IOException e) {
            throw Wlang.wrapThrow(e);
        }
    }

    @Override
    public void println() {
        print("\n");
    }

    @Override
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

    @Override
    public void printlnf(String fmt, Object... args) {
        print(String.format(fmt, args) + "\n");
    }

    @Override
    public void writeJson(Object o, JsonFormat fmt) {
        try {
            Writer w = getWriter();
            Json.toJson(w, o, fmt);
            w.flush();
        }
        catch (org.eclipse.jetty.io.EofException e) {
            if (log.isDebugEnabled())
                log.debug("EofException cached");
        }
        catch (IOException e) {
            throw Wlang.wrapThrow(e);
        }
    }

    @Override
    public void writeJson(Object o) {
        writeJson(o, JsonFormat.forLook());
    }

    @Override
    public void printf(String fmt, Object... args) {
        print(String.format(fmt, args));
    }

    @Override
    public void print(CharSequence msg) {
        try {
            Writer w = getWriter();
            w.write(null == msg ? "null" : msg.toString());
            w.flush();
        }
        catch (org.eclipse.jetty.io.EofException e) {
            if (log.isDebugEnabled())
                log.debug("EofException cached");
        }
        catch (IOException e) {
            throw Wlang.wrapThrow(e);
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
