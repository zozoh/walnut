package org.nutz.walnut.core.bm.localfile;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleManager;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.util.Wn;

/**
 * 我是本地文件桶管理器（可读写）。
 * <p>
 * 我理解的桶ID(buckId)就是我的 home 目录下的相对路径（开头不带<code>/</code>）
 * <p>
 * !!! 暂时的，我不支持 Copy 操作
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class LocalFileWBM extends LocalFileBM {

    public LocalFileWBM(WnIoHandleManager handles, File home) {
        super(handles, home);
    }

    @Override
    public boolean isSame(WnIoBM bm) {
        if (super.isSame(bm)) {
            return (bm instanceof LocalFileWBM);
        }
        return false;
    }

    @Override
    public WnIoHandle createHandle(int mode) {
        // 只读
        if (Wn.S.isRead(mode)) {
            return new LocalFileReadHandle();
        }
        // 只写
        if (Wn.S.isWriteOnly(mode)) {
            return new LocalFileWriteHandle();
        }
        throw Er.create("e.io.bm.localfile.NonsupportMode", mode);
    }

    @Override
    public long remove(String buckId, String referId) {
        File f = this.checkFile(buckId);
        f.delete();
        return 0;
    }

    @Override
    public long truncate(WnObj o, long len, WnIoIndexer indexer) {
        File f = this.checkFile(o.data());
        RandomAccessFile raf = null;
        FileChannel chan = null;
        try {
            raf = new RandomAccessFile(f, "w");
            chan = raf.getChannel();
            chan.truncate(len);
            chan.force(false);
        }
        catch (Exception e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(chan);
            Streams.safeClose(raf);
        }
        return len;
    }

}
