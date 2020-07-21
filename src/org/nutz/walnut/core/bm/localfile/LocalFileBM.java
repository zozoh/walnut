package org.nutz.walnut.core.bm.localfile;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleManager;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.core.bm.AbstractIoBM;
import org.nutz.walnut.util.Wn;

/**
 * 我是本地文件桶管理器（只读模式）。
 * <p>
 * 我理解的桶ID(buckId)就是我的 home 目录下的相对路径（开头不带<code>/</code>）
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class LocalFileBM extends AbstractIoBM {

    protected File dHome;

    public LocalFileBM(WnIoHandleManager handles, File home) {
        super(handles);
        if (null == home) {
            throw Er.create("e.io.bm.localfile.NilHome");
        }
        if (!home.isDirectory()) {
            throw Er.create("e.io.bm.localfile.NotDir");
        }
        this.dHome = home;
    }

    @Override
    public boolean isSame(WnIoBM bm) {
        if (this == bm) {
            return true;
        }
        if (bm instanceof LocalFileBM) {
            LocalFileBM lfbm = (LocalFileBM) bm;
            if (!lfbm.dHome.equals(dHome))
                return false;
            // 嗯，那就是自己了
            return true;
        }
        return false;
    }

    @Override
    public WnIoHandle createHandle(int mode) {
        // 只读
        if (Wn.S.isRead(mode)) {
            return new LocalFileReadHandle(this);
        }
        throw Er.create("e.io.bm.localfile.NonsupportMode", mode);
    }

    @Override
    public long copy(String buckId, String referId) {
        throw Lang.noImplement();
    }

    @Override
    public long remove(String buckId, String referId) {
        throw Lang.noImplement();
    }

    @Override
    public long truncate(WnObj o, long len, WnIoIndexer indexer) {
        throw Lang.noImplement();
    }

    protected File checkFile(String buckId) {
        if (Strings.isBlank(buckId)) {
            throw Er.create("e.io.bm.localfile.NilPath");
        }
        File f = Files.getFile(dHome, buckId);
        if (!f.exists()) {
            throw Er.create("e.io.bm.localfile.NoExists", buckId);
        }
        if (!f.isFile()) {
            throw Er.create("e.io.bm.localfile.NotFile", buckId);
        }
        return f;
    }
}
