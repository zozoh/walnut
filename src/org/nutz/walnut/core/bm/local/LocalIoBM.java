package org.nutz.walnut.core.bm.local;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleManager;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.core.WnReferApi;

/**
 * 本地桶管理器，将数据存放在本地
 * 
 * <pre>
 * Bucket Home/
 * #-----------------------------------------
 * # 桶文件
 * |-- buck/          # 桶目录
 * |   |-- 0evq/      # 采用首4字符散列目录
 * |   |-- 89..g1     # 后面 28字符（可能更多）作为文件
 * #-----------------------------------------
 * # 交换文件
 * |-- swap/
 *     |-- 4tu..8q1   # 交换文件，文件名就是写句柄ID
 * </pre>
 * 
 * <ul>
 * <li>句柄由传入的WnIoHandleManager管理，与 WnIoImpl2 共享。<br>
 * <li>而WnIoHandleManager需要通过WnIoMappingFactory取回句柄的索引管理器以及对象信息。
 * <li>因此我们说：通过 WnIoMappingFactory解开了直接的循环引用。
 * <li>每个桶的引用，由传入的引用计数管理器管理。
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class LocalIoBM implements WnIoBM {

    private WnIoHandleManager handles;

    private File dBucket;

    private File dSwap;

    private WnReferApi refers;

    public LocalIoBM(String home,
                     boolean autoCreate,
                     WnIoHandleManager handles,
                     WnReferApi refers) {
        File dHome = Files.findFile(home);
        if (null == dHome) {
            // 不自动创建，就自裁！！！
            if (!autoCreate) {
                throw Er.create("e.io.bm.local.HomeNotFound", home);
            }
            dHome = Files.createDirIfNoExists(home);
        }
        // 不是目录，自裁
        if (!dHome.isDirectory()) {
            throw Er.create("e.io.bm.local.HomeMustBeDirectory", dHome.getAbsolutePath());
        }
        // 获取桶目录
        dBucket = Files.getFile(dHome, "buck");
        dBucket = Files.createDirIfNoExists(dBucket);

        // 获取交换区目录
        dSwap = Files.getFile(dHome, "swap");
        dSwap = Files.createDirIfNoExists(dSwap);

        // 句柄管理器
        this.handles = handles;

        // 引用计数管理器
        this.refers = refers;
    }

    @Override
    public boolean isSame(WnIoBM bm) {
        if (this == bm) {
            return true;
        }
        if (bm instanceof LocalIoBM) {
            LocalIoBM lib = (LocalIoBM) bm;
            if (!lib.dBucket.equals(dBucket))
                return false;
            if (!lib.dSwap.equals(dSwap))
                return false;
            // 嗯，那就是自己了
            return true;
        }
        return false;
    }

    @Override
    public WnIoHandle createHandle() {
        return new LocalIoHandle();
    }

    @Override
    public WnIoHandle checkHandle(String hid) {
        WnIoHandle h = new LocalIoHandle();
        handles.load(h);
        // 确保已经填充了索引
        if (h.getIndexer() == null) {
            throw Er.create("e.io.bm.local.checkHandle.NilIndexer");
        }
        // 确保已经填充了对象
        if (h.getObj() == null) {
            throw Er.create("e.io.bm.local.checkHandle.NilObj");
        }
        return h;
    }

    @Override
    public WnIoHandle open(WnObj o, int mode, WnIoIndexer indexer) {
        // 先搞一个句柄
        WnIoHandle h = new LocalIoHandle();
        h.setIndexer(indexer);
        h.setObj(o);
        h.setMode(mode);
        h.setOffset(0);

        // 只能有一个写,保存一下，不出错就成
        handles.save(h);

        return h;
    }

    @Override
    public int copy(String id) {
        return refers.incOne(id);
    }

    @Override
    public int remove(String id) {
        int rec = refers.decOne(id);
        // 归零了，那么要删除
        if (rec <= 0) {
            throw Lang.noImplement();
        }
        return rec;
    }

    @Override
    public long trancate(String id, long len) {
        return 0;
    }

}
