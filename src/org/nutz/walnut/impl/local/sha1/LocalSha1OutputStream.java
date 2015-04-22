package org.nutz.walnut.impl.local.sha1;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.local.AbstractLocalOutputStream;
import org.nutz.walnut.impl.local.Locals;

public class LocalSha1OutputStream extends AbstractLocalOutputStream {

    private LocalSha1WnStore store;

    private File swap;

    private WnObj obj;

    public LocalSha1OutputStream(LocalSha1WnStore store, OutputStream ops, File swap, WnObj o) {
        super(ops);
        this.store = store;
        this.swap = swap;
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
        File dest = Files.getFile(store.sha1Home, destPath);
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
        long nano = his.nanoStamp();

        // 更新 Obj 状态
        obj.mender(his.owner());
        obj.sha1(sha1);
        obj.len(len);
        obj.nanoStamp(nano);
        store.indexer().set(obj.id(), obj.toMap4Update("^m|sha1|len|lm|nano$"));

        // 是否清除多余的历史
        int remain = obj.remain();
        if (remain >= 0) {
            store.cleanHistoryBy(obj, remain);
        }
    }

    public String toString() {
        return String.format("sha1ops: %s << %s", obj, swap);
    }

}
