package org.nutz.walnut.impl.local.data;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.local.AbstractLocalOutputStream;
import org.nutz.walnut.util.Wn;

public class LocalDataOutputStream extends AbstractLocalOutputStream {

    private LocalDataWnStore store;

    private File f;

    private WnObj obj;

    public LocalDataOutputStream(LocalDataWnStore store, OutputStream ops, File f, WnObj o) {
        super(ops);
        this.store = store;
        this.f = f;
        this.obj = o;
    }

    public void close() throws IOException {
        // 关闭输出流（因为是 BufferedOutputStream，所以不用 flush，它自己会先 flush 的）
        ops.close();

        // 计算 SHA1
        String sha1 = Lang.sha1(f);
        long len = f.length();
        long nano = System.nanoTime();

        // 更新 Obj 状态 (obj.data 已经被 LocalDataWnStore 在之前设置好了）
        obj.mender(Wn.WC().checkMe());
        obj.sha1(sha1);
        obj.len(len);
        obj.nanoStamp(nano);
        store.indexer().set(obj, "^m|data|sha1|len|lm|nano$");

    }

    public String toString() {
        return String.format("dataops: %s << %s", obj, f);
    }
}
