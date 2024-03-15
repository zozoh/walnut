package com.site0.walnut.core.bm.localfile;

import java.io.File;

import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.bm.AbstractIoBM;
import com.site0.walnut.util.Wn;

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
        if (Wn.S.canRead(mode)) {
            return new LocalFileReadHandle();
        }
        throw Er.create("e.io.bm.localfile.NonsupportMode", mode);
    }

    @Override
    public long copy(WnObj oSr, WnObj oTa) {
        throw Wlang.noImplement();
    }

    @Override
    public long remove(WnObj o) {
        throw Wlang.noImplement();
    }

    @Override
    public long truncate(WnObj o, long len, WnIoIndexer indexer) {
        throw Wlang.noImplement();
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
