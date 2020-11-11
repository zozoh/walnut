package org.nutz.walnut.core;

import java.io.IOException;

import org.nutz.lang.Each;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public class WnIoMapping {

    private WnIoIndexer indexer;

    private WnIoBM bm;

    public WnIoMapping(WnIoIndexer indexer, WnIoBM bm) {
        if (null == indexer) {
            throw Er.create("e.io.mapping.nilIndexer");
        }
        if (null == bm) {
            throw Er.create("e.io.mapping.nilBM");
        }
        this.indexer = indexer;
        this.bm = bm;
    }

    public WnIoIndexer getIndexer() {
        return indexer;
    }

    public void setIndexer(WnIoIndexer indexer) {
        this.indexer = indexer;
    }

    public WnIoBM getBucketManager() {
        return bm;
    }

    public void setBucketManager(WnIoBM bm) {
        this.bm = bm;
    }

    public boolean isSameBM(WnIoMapping mapping) {
        return this.bm.isSame(mapping.bm);
    }

    public WnIoHandle open(WnObj o, int mode) throws WnIoHandleMutexException, IOException {
        return bm.open(o, mode, indexer);
    }

    public WnIoHandle getHandle(String hid) {
        return bm.checkHandle(hid);
    }

    public void truncate(WnObj o, long len) {
        bm.truncate(o, len, indexer);
    }

    public void delete(WnObj o, boolean r, WnIoActionCallback callback) {
        // 仅仅是文件
        if (o.isFILE()) {
            if (o.hasSha1()) {
                bm.remove(o);
            }
            //
            // 删除存储
            //
            if (!Wn.Io.isEmptySha1(o.sha1())) {
                bm.remove(o);
            }
            //
            // 删除索引
            //
            // 之前的回调
            if (null != callback) {
                callback.on_before(o);
            }
            indexer.delete(o);
            // 之后的回调
            if (null != callback) {
                callback.on_after(o);
            }
            return;
        }

        // 递归删除所有的子孙
        if (r) {
            indexer.eachChild(o, null, new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    delete(child, r, callback);
                }
            });
        }
        // 否则必须确保自身不为空
        else if (indexer.hasChild(o)) {
            throw Er.create("e.io.rm.NoEmptyDir");
        }

        // 之前的回调
        if (null != callback) {
            callback.on_before(o);
        }
        // 删除自身
        indexer.delete(o);
        // 之后的回调
        if (null != callback) {
            callback.on_after(o);
        }
    }

    /**
     * 给出一个快捷的方法，将对象 A 的内容快速 copy 到对象B 中
     * <p>
     * 本函数会直接修改
     * 
     * @param oSr
     *            源对象A
     * @param oTa
     *            目标对象B
     * @return 复制后目标对象的长度。 -1 表示源对象也为空
     */
    public long copyData(WnObj oSr, WnObj oTa) {
        // 交给桶管理器处理
        bm.copy(oSr, oTa);

        // 更新一下目标的索引
        oTa.len(oSr.len()).sha1(oSr.sha1());
        oTa.lastModified(Wn.now());
        indexer.set(oTa, "^(len|sha1|lm)$");
        return oTa.len();
    }

}
