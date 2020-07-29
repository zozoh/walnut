package org.nutz.walnut.core;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;

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

    public WnIoBM getBucketManager() {
        return bm;
    }

    public boolean isSameBM(WnIoMapping mapping) {
        return this.bm.isSame(mapping.bm);
    }

    public WnIoHandle open(WnObj o, int mode) {
        return bm.open(o, mode, indexer);
    }

    public WnIoHandle getHandle(String hid) {
        return bm.checkHandle(hid);
    }

    public void truncate(WnObj o, long len) {
        bm.truncate(o, len, indexer);
    }

    public void delete(WnObj o, boolean r) {
        // 仅仅是文件
        if (o.isFILE()) {
            if (o.hasSha1()) {
                bm.remove(o.sha1(), o.id());
            }
            indexer.delete(o);
            return;
        }

        // 递归删除所有的子孙
        if (r) {

        }
        // 否则必须确保自身不为空
        else if (indexer.hasChild(o)) {
            throw Er.create("e.io.rm.NoEmptyDir");
        }

        // 删除自身
        indexer.delete(o);
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
        // 防守一下
        if (!oSr.hasSha1()) {
            return -1;
        }
        // 如果目标不是空的，那么检查一下是否有必要 Copy
        if (oTa.hasSha1()) {
            // 嗯，木有必要 Copy
            if (oSr.isSameSha1(oTa.sha1())) {
                return oTa.len();
            }
            // 已经引用了其他的数据，取消一下引用
            bm.remove(oTa.sha1(), oTa.id());
        }
        
        // 增加引用
        bm.copy(oSr.sha1(), oTa.id());

        // 直接将数据段Copy过去
        oTa.len(oSr.len()).sha1(oSr.sha1());
        oTa.lastModified(System.currentTimeMillis());
        indexer.set(oTa, "^(len|sha1|lm)$");
        return oTa.len();
    }

}
