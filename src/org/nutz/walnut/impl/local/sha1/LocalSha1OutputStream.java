package org.nutz.walnut.impl.local.sha1;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.local.Locals;

public class LocalSha1OutputStream extends OutputStream {

    private LocalSha1WnStore store;

    private File swap;

    private WnObj obj;

    private OutputStream ops;

    public LocalSha1OutputStream(LocalSha1WnStore store, File swap, WnObj o) {
        this.store = store;
        this.swap = swap;
        try {
            this.ops = new BufferedOutputStream(new FileOutputStream(swap, true));
        }
        catch (FileNotFoundException e) {
            throw Lang.wrapThrow(e);
        }
        this.obj = o;
    }

    public void close() throws IOException {
        // 关闭输出流（因为是 BufferedOutputStream，所以不用 flush，它自己会先 flush 的）
        ops.close();

        // 计算 SHA1
        String sha1 = Lang.sha1(swap);
        long len = swap.length();

        // 将临时文件移动到指纹路径
        String destPath = Locals.key2path(sha1);
        File dest = Files.getFile(store.home, destPath);
        // 指纹已经存在
        if (dest.exists()) {
            Files.deleteFile(swap);
        }
        // 移动过去
        else {
            Files.move(swap, dest);
        }

        // 生成历史记录
        WnHistory his = store.addHistory(obj.id(), null, sha1, len);

        // 更新 Obj 状态
        obj.sha1(his.sha1());
        obj.len(his.len());
        obj.nanoStamp(his.nanoStamp());
        store.indexer.set(obj.id(), obj.toMap("^sha1|len|lm|nano$"));

        // 是否清除多余的历史
        int remain = obj.remain();
        if (remain >= 0) {
            store.cleanHistoryBy(obj, remain);
        }
    }

    public void write(int b) throws IOException {
        ops.write(b);
    }

    public void write(byte[] b) throws IOException {
        ops.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        ops.write(b, off, len);
    }

    public void flush() throws IOException {
        ops.flush();
    }

    public String toString() {
        return String.format("sha1ops: %s << %s", obj, swap);
    }

}
