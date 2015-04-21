package org.nutz.walnut.impl.local.tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.stream.NullInputStream;
import org.nutz.lang.stream.RandomAccessFileOutputStream;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.AbstractWnStore;
import org.nutz.walnut.impl.local.Locals;
import org.nutz.walnut.util.RandomAccessFileInputStream;

public class LocalTreeWnStore extends AbstractWnStore {

    WnIndexer indexer;

    public LocalTreeWnStore(WnIndexer indexer, File home, String rootPath) {
        super(new LocalTreeWnStoreTable(home, rootPath));
        this.indexer = indexer;
    }

    @Override
    public InputStream getInputStream(WnHistory his, long off) {
        LocalFileWnHistory lhis = (LocalFileWnHistory) his;
        File f = lhis.file;

        // 不存在
        if (!f.exists()) {
            return new NullInputStream();
        }

        try {
            // 从头读取
            if (off <= 0) {
                return new FileInputStream(f);
            }

            // 超过范围
            if (off >= f.length())
                return new NullInputStream();

            // 从中间某位置读取
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            raf.seek(off);
            return new RandomAccessFileInputStream(raf);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    @Override
    public OutputStream getOutputStream(WnObj o, long off) {
        LocalTreeWnStoreTable table = (LocalTreeWnStoreTable) this.table;

        // 试着根据对象的全路径获取一下本地文件
        File f = Locals.eval_local_file(table.home, table.rootPath, o);

        try {
            // 不存在
            if (!f.exists()) {
                Files.createNewFile(f);
            }

            OutputStream ops;
            // 重头覆盖
            if (0 == off) {
                ops = new FileOutputStream(f);
            }
            // 末尾追加
            if (off < 0 || off >= f.length()) {
                ops = new FileOutputStream(f, true);
            }
            // 中间写入
            else {
                RandomAccessFile raf = new RandomAccessFile(f, "w");
                raf.seek(off);
                ops = new RandomAccessFileOutputStream(raf);
            }

            // 包裹一下
            return new LocalTreeOutputStream(this, ops, f, o);

        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }
}
