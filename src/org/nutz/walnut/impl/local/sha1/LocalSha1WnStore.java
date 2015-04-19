package org.nutz.walnut.impl.local.sha1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.random.R;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnStoreTable;
import org.nutz.walnut.impl.AbstractWnStore;
import org.nutz.walnut.impl.local.Locals;
import org.nutz.walnut.util.RandomAccessFileInputStream;

public class LocalSha1WnStore extends AbstractWnStore {

    File home;

    File swapHome;

    WnIndexer indexer;

    public LocalSha1WnStore(WnIndexer indexer, WnStoreTable table, String homePath) {
        super(table);
        this.indexer = indexer;
        this.home = Files.createDirIfNoExists(homePath);
        this.swapHome = Files.createDirIfNoExists(homePath + "/_swap");
    }

    @Override
    public InputStream getInputStream(WnHistory his, long off) {
        // 根据 sha1 得到文件路径
        String sha1 = his.sha1();
        String ph = Locals.key2path(sha1);
        File f = Files.getFile(home, ph);
        if (!f.exists()) {
            throw Er.create("e.io.store.noexists", his);
        }

        // 生成读取需要的输出流
        try {
            // 从头读取
            if (off <= 0) {
                return new FileInputStream(f);
            }
            // 从某一位置开始读取
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            raf.seek(off);
            return new RandomAccessFileInputStream(raf);

        }
        catch (FileNotFoundException e) {
            throw Lang.wrapThrow(e);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    @Override
    public OutputStream getOutputStream(WnObj o, long off) {
        // 初始化临时的数据文件
        File swap = Files.getFile(swapHome, R.UU16());

        // 从头写，那么就用这个空文件
        if (0 == off) {
            Files.createFileIfNoExists(swap);
        }
        // 那么要把原来的文件复制一下，复制多少呢？ 看 off 咯
        else {
            // 找到原来文件
            File org = null;
            String sha1 = o.sha1();
            if (o.hasSha1()) {
                org = Files.getFile(home, Locals.key2path(sha1));
            }
            // 如果原来的文件存在，复制
            if (null != org && org.exists()) {
                try {
                    Files.copyFile(org, swap, off);
                }
                catch (IOException e) {
                    throw Lang.wrapThrow(e);
                }
            }
            // 否则不能忍受，抛错吧，肯定有啥错了
            else {
                throw Er.create("o.io.store.nosha1", sha1);
            }
        }

        // 生成输出流，当调用者关闭的时候，会做很多事情 ...
        return new LocalSha1OutputStream(this, swap, o);
    }

    @Override
    public void _clean_for_unit_test() {
        super._clean_for_unit_test();
        Files.clearDir(home);
    }

}
