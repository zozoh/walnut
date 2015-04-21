package org.nutz.walnut.impl.local.tree;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.local.AbstractLocalOutputStream;

public class LocalTreeOutputStream extends AbstractLocalOutputStream {

    private LocalTreeWnStore store;

    private File f;

    private WnObj obj;

    public LocalTreeOutputStream(LocalTreeWnStore store, OutputStream ops, File f, WnObj o) {
        super(ops);
        this.store = store;
        this.f = f;
        this.obj = o;
    }

    public void close() throws IOException {
        ops.close();

        // 计算 SHA1
        long nano = System.nanoTime();
        String sha1 = Lang.sha1(f);
        long len = f.length();

        // 更新 Obj 状态
        obj.sha1(sha1);
        obj.len(len);
        obj.nanoStamp(nano);
        store.indexer.set(obj.id(), obj.toMap4Update("^sha1|len|lm|nano$"));
    }

    public String toString() {
        return String.format("local >> %s", f);
    }

}
