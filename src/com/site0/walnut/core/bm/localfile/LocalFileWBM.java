package com.site0.walnut.core.bm.localfile;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.indexer.localfile.WnLocalFileObj;
import com.site0.walnut.util.Wn;

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
        if (Wn.S.canRead(mode)) {
            return new LocalFileReadHandle();
        }
        // 只写
        if (Wn.S.isWrite(mode)) {
            return new LocalFileWriteHandle();
        }
        // 追加
        if (Wn.S.isAppend(mode)) {
            return new LocalFileReadWriteHandle();
        }
        // 修改
        if (Wn.S.canModify(mode) || Wn.S.isReadWrite(mode)) {
            return new LocalFileReadWriteHandle();
        }
        throw Er.create("e.io.bm.localfile.NonsupportMode", mode);
    }

    @Override
    public long copy(WnObj oSr, WnObj oTa) {
        if ((oSr instanceof WnLocalFileObj) && (oTa instanceof WnLocalFileObj)) {
            WnLocalFileObj ofSr = (WnLocalFileObj) oSr;
            WnLocalFileObj ofTa = (WnLocalFileObj) oTa;
            Files.copy(ofSr.getFile(), ofTa.getFile());
            return oTa.len();
        }
        throw Wlang.noImplement();
    }

    @Override
    public long remove(WnObj o) {
        if ((o instanceof WnLocalFileObj)) {
            WnLocalFileObj of = (WnLocalFileObj) o;
            Files.deleteFile(of.getFile());
            return 0;
        }
        throw Wlang.noImplement();
    }

    @Override
    public long truncate(WnObj o, long len, WnIoIndexer indexer) {
        if ((o instanceof WnLocalFileObj)) {
            WnLocalFileObj of = (WnLocalFileObj) o;
            File f = of.getFile();
            RandomAccessFile raf = null;
            FileChannel chan = null;
            try {
                raf = new RandomAccessFile(f, "rw");
                chan = raf.getChannel();
                chan.truncate(len);
                chan.force(false);
            }
            catch (Exception e) {
                throw Wlang.wrapThrow(e);
            }
            finally {
                Streams.safeClose(chan);
                Streams.safeClose(raf);
            }
            return len;
        }
        throw Wlang.noImplement();
    }

}
