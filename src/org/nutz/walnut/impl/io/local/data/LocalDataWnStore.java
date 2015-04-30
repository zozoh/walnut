package org.nutz.walnut.impl.io.local.data;

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
import org.nutz.lang.stream.NullInputStream;
import org.nutz.lang.stream.RandomAccessFileOutputStream;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.io.AbstractWnStore;
import org.nutz.walnut.impl.io.local.Locals;
import org.nutz.walnut.util.RandomAccessFileInputStream;

public class LocalDataWnStore extends AbstractWnStore {

    File home;

    public LocalDataWnStore(WnIndexer indexer, String homePath) {
        super(indexer, new LocalDataWnStoreTable());
        this.home = Files.createDirIfNoExists(homePath);
    }

    WnIndexer indexer() {
        return indexer;
    }

    @Override
    protected void _do_real_remove_history_data(WnHistory his) {
        String ph = Locals.key2path(his.data());
        File f = Files.getFile(home, ph);
        if (f.exists())
            Files.deleteFile(f);

        Locals.ocd_clean_data_dir(f);
    }

    @Override
    protected InputStream _get_inputstream(WnHistory his, long off) {
        // 根据 data 得到文件路径
        String data = his.data();

        // 返回空输入流
        if (Strings.isBlank(data))
            return new NullInputStream();

        String ph = Locals.key2path(data);
        File f = Files.getFile(home, ph);
        if (!f.exists()) {
            throw Er.create("e.io.store.data.noexists", his);
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
        // 确保对象分配了一个 UUID 作为自己的 data
        if (!o.hasData())
            o.data(R.UU32());

        // 初始化临时的数据文件
        String ph = Locals.key2path(o.data());
        File f = Files.getFile(home, ph);

        try {
            // 确保文件存在
            if (!f.exists())
                Files.createNewFile(f);

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

            // 生成输出流，当调用者关闭的时候，会做很多事情 ...
            return new LocalDataOutputStream(this, ops, f, o);
        }
        catch (FileNotFoundException e) {
            throw Lang.wrapThrow(e);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }

    }

    @Override
    public void _clean_for_unit_test() {
        super._clean_for_unit_test();
        Files.clearDir(home);
    }

}