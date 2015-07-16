package org.nutz.walnut.impl.io.local.sha1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnStoreTable;
import org.nutz.walnut.impl.io.AbstractWnStore;
import org.nutz.walnut.impl.io.local.Locals;
import org.nutz.walnut.util.RandomAccessFileInputStream;

public class LocalSha1WnStore extends AbstractWnStore {

    File sha1Home;

    File swapHome;

    public LocalSha1WnStore(WnIndexer indexer, WnStoreTable table, String homePath) {
        super(indexer, table);
        this.sha1Home = Files.createDirIfNoExists(homePath + "/raw");
        this.swapHome = Files.createDirIfNoExists(homePath + "/swap");
    }

    WnIndexer indexer() {
        return indexer;
    }

    @Override
    protected void _do_real_remove_history_data(WnHistory his) {
        String ph = Locals.key2path(his.sha1());
        File f = Files.getFile(sha1Home, ph);
        if (f.exists())
            Files.deleteFile(f);

        Locals.ocd_clean_data_dir(f);
    }

    @Override
    protected String _get_realpath(WnHistory his) {
        // 根据 sha1 得到文件路径
        String sha1 = his.sha1();
        String ph = Locals.key2path(sha1);
        File f = Files.getFile(sha1Home, ph);
        if (!f.exists()) {
            throw Er.create("e.io.store.sha1.noexists", his);
        }
        return f.getAbsolutePath();
    }

    @Override
    protected InputStream _get_inputstream(WnHistory his, long off) {
        // 根据 sha1 得到文件路径
        String sha1 = his.sha1();
        String ph = Locals.key2path(sha1);
        File f = Files.getFile(sha1Home, ph);
        if (!f.exists()) {
            throw Er.create("e.io.store.sha1.noexists", his);
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
    protected OutputStream _get_outputstream(WnObj o, long off) {
        // 初始化临时的数据文件
        File swap = Files.getFile(swapHome, R.UU16());
        Files.createFileIfNoExists(swap);

        // 那么要把原来的文件复制一下，复制多少呢？ 看 off 咯
        if (off != 0) {
            // 找到原来文件
            File org = null;
            String sha1 = o.sha1();
            if (!Strings.isBlank(sha1)) {
                org = Files.getFile(sha1Home, Locals.key2path(sha1));
            }
            // 如果原来的文件存在，复制
            if (null != org && org.exists()) {
                try {
                    if (off > 0)
                        Files.copyFile(org, swap, off);
                    else
                        Files.copy(org, swap);
                }
                catch (IOException e) {
                    throw Lang.wrapThrow(e);
                }
            }
        }

        try {
            OutputStream ops = new FileOutputStream(swap, true);

            // 生成输出流，当调用者关闭的时候，会做很多事情 ...
            return new LocalSha1OutputStream(this, ops, swap, o);
        }
        catch (FileNotFoundException e) {
            throw Lang.wrapThrow(e);
        }

    }

    @Override
    public void _clean_for_unit_test() {
        super._clean_for_unit_test();
        Files.clearDir(sha1Home);
        Files.clearDir(swapHome);
    }

}
