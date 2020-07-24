package org.nutz.walnut.core;

import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;

public class WnIoMapping {

    private WnIoIndexer indexer;

    private WnIoBM bm;

    public WnIoMapping(WnIoIndexer indexer, WnIoBM bm) {
        this.indexer = indexer;
        this.bm = bm;
    }

    public WnIoIndexer getIndexer() {
        return indexer;
    }

    public WnIoBM getBucketManager() {
        return bm;
    }

    public WnObj checkById(String id) {
        return indexer.checkById(id);
    }

    public WnObj check(WnObj p, String path) {
        return indexer.check(p, path);
    }

    public boolean existsId(String id) {
        return indexer.existsId(id);
    }

    public WnObj fetch(WnObj p, String path) {
        return indexer.fetch(p, path);
    }

    public WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex) {
        return indexer.fetch(p, paths, fromIndex, toIndex);
    }

    public WnObj move(WnObj src, String destPath) {
        return indexer.move(src, destPath);
    }

    public WnObj move(WnObj src, String destPath, int mode) {
        return indexer.move(src, destPath, mode);
    }

    public WnObj rename(WnObj o, String nm) {
        return indexer.rename(o, nm);
    }

    public WnObj rename(WnObj o, String nm, boolean keepType) {
        return indexer.rename(o, nm, keepType);
    }

    public WnObj rename(WnObj o, String nm, int mode) {
        return indexer.rename(o, nm, mode);
    }

    public void set(WnObj o, String regex) {
        indexer.set(o, regex);
    }

    public WnObj setBy(String id, NutMap map, boolean returnNew) {
        return indexer.setBy(id, map, returnNew);
    }

    public WnObj setBy(WnQuery q, NutMap map, boolean returnNew) {
        return indexer.setBy(q, map, returnNew);
    }

    public int inc(String id, String key, int val, boolean returnNew) {
        return indexer.inc(id, key, val, returnNew);
    }

    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        return indexer.inc(q, key, val, returnNew);
    }

    public int getInt(String id, String key, int dft) {
        return indexer.getInt(id, key, dft);
    }

    public long getLong(String id, String key, long dft) {
        return indexer.getLong(id, key, dft);
    }

    public String getString(String id, String key, String dft) {
        return indexer.getString(id, key, dft);
    }

    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        return indexer.getAs(id, key, classOfT, dft);
    }

    public WnObj create(WnObj p, String path, WnRace race) {
        return indexer.create(p, path, race);
    }

    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        return indexer.create(p, paths, fromIndex, toIndex, race);
    }

    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        return indexer.createById(p, id, name, race);
    }

    public void delete(WnObj o, boolean r) {
        // 仅仅是文件
        if(o.isFILE()) {
            if (o.hasData()) {
                bm.remove(o.data(), o.id());
            }
            indexer.delete(o);
            return;
        }
        
        // 递归删除所有的子孙
        if(r) {
            
        }
        // 否则必须确保自身不为空
        else if(this.hasChild(o)) {
            throw Er.create("e.io.rm.NoEmptyDir");
        }
        
        // 删除自身
        indexer.delete(o);
    }

    public WnObj get(String id) {
        return indexer.get(id);
    }

    public WnObj getOne(WnQuery q) {
        return indexer.getOne(q);
    }

    public WnObj getRoot() {
        return indexer.getRoot();
    }

    public String getRootId() {
        return indexer.getRootId();
    }

    public boolean isRoot(String id) {
        return indexer.isRoot(id);
    }

    public boolean isRoot(WnObj o) {
        return indexer.isRoot(o);
    }

    public int each(WnQuery q, Each<WnObj> callback) {
        return indexer.each(q, callback);
    }

    public List<WnObj> query(WnQuery q) {
        return indexer.query(q);
    }

    public List<WnObj> getChildren(WnObj o, String name) {
        return indexer.getChildren(o, name);
    }

    public long count(WnQuery q) {
        return indexer.count(q);
    }

    public boolean hasChild(WnObj p) {
        return indexer.hasChild(p);
    }

    public WnObj push(String id, String key, Object val, boolean returnNew) {
        return indexer.push(id, key, val, returnNew);
    }

    public void push(WnQuery query, String key, Object val) {
        indexer.push(query, key, val);
    }

    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        return indexer.pull(id, key, val, returnNew);
    }

    public void pull(WnQuery query, String key, Object val) {
        indexer.pull(query, key, val);
    }

    public boolean isSameBM(WnIoMapping mapping) {
        return this.bm.isSame(mapping.bm);
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
        if (!oSr.hasData()) {
            return -1;
        }
        // 如果目标不是空的，那么检查一下是否有必要 Copy
        if (oTa.hasData()) {
            // 数据区指向相同
            if (oSr.isSameData(oTa.data())) {
                // 嗯，木有必要 Copy
                if (oSr.isSameSha1(oTa.sha1()) && oSr.len() == oTa.len()) {
                    return oTa.len();
                }
                // 那么久更新一下指纹和长度咯
                oTa.len(oSr.len()).sha1(oSr.sha1());
                indexer.set(oTa, "^(len|sha1)$");
                return oTa.len();
            }
            // 已经引用了其他的数据，取消一下引用
            bm.remove(oTa.data(), oTa.id());
        }
        // 增加引用
        bm.copy(oSr.data(), oTa.id());

        // 直接将数据段Copy过去
        oTa.data(oSr.data()).len(oSr.len()).sha1(oSr.sha1());
        oTa.lastModified(System.currentTimeMillis());
        indexer.set(oTa, "^(len|sha1|data|lm)$");
        return oTa.len();
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

}
