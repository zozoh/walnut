package com.site0.walnut.util;

import java.io.IOException;
import java.io.InputStream;

import org.nutz.json.JsonFormat;
import com.site0.walnut.api.WnOutputable;

public class WnJsOutput implements WnOutputable {

    public static WnJsOutput WRAP(WnOutputable out) {
        if (out instanceof WnJsOutput) {
            return (WnJsOutput) out;
        }
        return new WnJsOutput(out);
    }

    private WnOutputable out;

    private WnJsOutput(WnOutputable out) {
        this.out = out;
    }

    /**
     * 不得不搞一个这个函数，这个 openjdk 的 nashorn 引擎很瘸。 如果我 <code>var i = 0</code>, 这个是
     * Integer, 但是 <code>i++;</code> 之后就变 Double 了 而大量的业务代码，在循环里打印，采用的是:
     *
     * <pre>
     * sys.out.printlnf("%d) xxxxx", i)
     * </pre>
     * 
     * 某个版本的 nashorn 会导致:
     * 
     * <pre>
     * java.util.IllegalFormatConversionException : java.util.IllegalFormatConversionException: d != java.lang.Double
     * </pre>
     * 
     * 只能通过一个转换函数，发现参数里的 Double 其实是个整数，那么就改成 Long
     * 
     * @param args
     *            输入参数
     * @return 整理后的参数
     */
    private Object[] formatArgs(Object[] args) {
        Object[] vals = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof Double) {
                Double d = (Double) arg;
                double dv = d.doubleValue();
                long dl = d.longValue();
                if (dv == ((double) dl)) {
                    vals[i] = Long.valueOf(dl);
                    continue;
                }
            }
            vals[i] = arg;
        }
        return vals;
    }

    @Override
    public void printlnf(String fmt, Object... args) {
        Object[] vals = formatArgs(args);
        out.printlnf(fmt, vals);
    }

    @Override
    public void printf(String fmt, Object... args) {
        Object[] vals = formatArgs(args);
        out.printf(fmt, vals);
    }

    @Override
    public void write(InputStream ins) {
        out.write(ins);
    }

    @Override
    public void writeAndClose(InputStream ins) {
        out.writeAndClose(ins);
    }

    @Override
    public void write(byte[] b) {
        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        out.write(b, off, len);
    }

    @Override
    public void println() {
        out.println();
    }

    @Override
    public void println(Object obj) {
        out.println(obj);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void writeJson(Object o, JsonFormat fmt) {
        out.writeJson(o, fmt);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void writeJson(Object o) {
        out.writeJson(o);
    }

    @Override
    public void print(CharSequence msg) {
        out.print(msg);
    }

}
