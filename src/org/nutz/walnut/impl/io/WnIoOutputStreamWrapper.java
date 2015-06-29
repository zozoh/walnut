package org.nutz.walnut.impl.io;

import java.io.IOException;
import java.io.OutputStream;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public class WnIoOutputStreamWrapper extends OutputStream {

    private WnIo io;

    private WnObj o;

    private OutputStream ops;

    private String old_sha1;

    public WnIoOutputStreamWrapper(WnIo io, WnObj o, OutputStream ops) {
        this.io = io;
        this.o = o;
        this.ops = ops;
        if (o.isFILE())
            old_sha1 = o.sha1();
    }

    public void write(int b) throws IOException {
        ops.write(b);
    }

    public int hashCode() {
        return ops.hashCode();
    }

    public void write(byte[] b) throws IOException {
        ops.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        ops.write(b, off, len);
    }

    public boolean equals(Object obj) {
        return ops.equals(obj);
    }

    public void flush() throws IOException {
        ops.flush();
    }

    public void close() throws IOException {
        ops.close();

        // 文件:触发同步时间修改
        if (o.isFILE()) {
            String sha1 = io.checkById(o.id()).sha1();
            if (null == old_sha1 || !old_sha1.equals(sha1)) {
                Wn.Io.update_ancestor_synctime(io, o, false);
                old_sha1 = sha1;
            }
        }
        // 其他触发同步时间修改
        else {
            Wn.Io.update_ancestor_synctime(io, o, false);
        }

        // 最后触发修改的钩子
        Wn.WC().doHook("write", o);
    }

    public String toString() {
        return ops.toString();
    }

}
