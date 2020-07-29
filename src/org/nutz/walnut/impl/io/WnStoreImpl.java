package org.nutz.walnut.impl.io;

import java.io.File;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketFactory;
import org.nutz.walnut.api.io.WnBucketManager;
import org.nutz.walnut.api.io.WnHandle;
import org.nutz.walnut.api.io.WnHandleManager;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnStore;
import org.nutz.walnut.ext.quota.QuotaService;
import org.nutz.walnut.impl.io.bucket.LocalFileBucket;
import org.nutz.walnut.impl.io.bucket.MemoryBucket;
import org.nutz.walnut.impl.io.mnt.WnMemoryTree;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class WnStoreImpl implements WnStore {

    private static final int DFT_BUCKET_BLOCK_SIZE = 8192 * 1000;

    private WnBucketManager buckets;

    private WnHandleManager handles;

    private Map<String, WnBucketFactory> mapping;

    private QuotaService quota;

    public void on_depose() {
        handles.dropAll();
    }

    @Override
    public void _clean_for_unit_test() {
        buckets._clean_for_unit_test();
        on_depose();
    }

    @Override
    public long copyData(WnObj a, WnObj b) {
        // 两个对象不能相等
        if (a.equals(b))
            throw Er.create("e.io.duplicate.toSelf", a);

        // 俩文件都是空的
        if (!a.hasData() && !b.hasData()) {
            return 0;
        }

        // 俩文件本来内容就一样
        if (a.isSameData(b.data())) {
            return 0;
        }

        // 两个对象必须都是文件
        if (!a.isFILE())
            throw Er.create("e.io.duplicate.DIR", a);
        if (!b.isFILE())
            throw Er.create("e.io.duplicate.DIR", b);

        // 试图释放一下目标对象的内容
        if (b.hasData()) {
            String bData = b.data();
            buckets.incReferById(bData, -1);
        }

        // 返回 1 表示成功
        long re = 1;

        // 源对象为空
        if (!a.hasData()) {
            b.data(null).len(0).sha1(null);
        }
        // 将源对象直接复制过来
        else {
            b.data(a.data()).len(a.len()).sha1(a.sha1());
            // 直接增加引用计数咯
            re = buckets.incReferById(a.data(), 1);
        }

        // 标记最后修改时间
        b.lastModified(a.lastModified());

        return re;
    }

    @Override
    public String open(WnObj o, int mode) {
        // 只能针对 FILE 进行操作
        if (o.isDIR()) {
            throw Er.create("e.io.dir.open", o);
        }

        // secu下
        if (Wn.S.canRead(mode)) {
            o = Wn.WC().whenRead(o, false);
        }
        if (Wn.S.canWriteOrAppend(mode)) {
            o = Wn.WC().whenWrite(o, false);
        }

        // 一个对象只能打开一个写句柄
        if (Wn.S.canWite(mode) && o.hasWriteHandle()) {
            throw Er.create("e.io.obj.w.opened", o);
        }

        // 检查一些读写权限
        WnContext wc = Wn.WC();
        if (Wn.S.canWriteOrAppend(mode)) {
            o = wc.whenWrite(o, false);
        }
        if (Wn.S.canRead(mode)) {
            o = wc.whenRead(o, false);
        }
        
        // 检查一下空间配额
        if (quota != null && Wn.S.canWriteOrAppend(mode)) {
            quota.checkQuota("disk", wc.getMyName(), true);
        }

        // 创建句柄
        WnHandle hdl = handles.create();
        handles.save(hdl);
        hdl.mode = mode;
        hdl.obj = o;
        hdl.pos = Wn.S.canAppend(mode) ? o.len() : 0;
        hdl.updated = false;

        // 分析文件数据
        String data = o.data();

        // 首先看看有没有可以构建的桶实例
        if (null != mapping && null != data) {
            // 得到前缀
            int pos = data.indexOf("://");
            if (pos > 0) {
                String prefix = data.substring(0, pos + 3);
                WnBucketFactory bf = mapping.get(prefix);
                if (null != bf) {
                    hdl.bucket = bf.getBucket(o);
                    // 返回吧
                    return hdl.id;
                }
            }
        }

        // 对象元数据句柄
        if (o.isRWMeta()) {
            hdl.bucket = new MemoryBucket(64*1024);
            if (Wn.S.canRead(mode)) {
                String json = Json.toJson(o, JsonFormat.full());
                hdl.bucket.write(json);
            }
        }
        // 如果是映射到本地文件
        else if (null != data && data.startsWith("file://")) {
            if (!Wn.S.isRead(mode)) {
                throw Er.create("e.io.local.readonly", o);
            }
            String ph = data.substring("file://".length());
            File f = Files.findFile(ph);
            if (null == f)
                throw Er.create("e.io.local.noexists", ph);
            hdl.bucket = new LocalFileBucket(f, DFT_BUCKET_BLOCK_SIZE);
        }
        else if (null != data && data.startsWith("memory://")) {
            hdl.bucket = WnMemoryTree.tree(o).datas.get(o.data());
        }
        // 否则就是默认的桶实现
        else {
            // 获取对象原有的桶
            WnBucket bu = Strings.isBlank(data) ? null : buckets.checkById(data);

            // 写入模式无论如何都要弄个新桶
            if (Wn.S.canWriteOrAppend(mode)) {
                // 如果没有桶，则分配一个
                if (null == bu) {
                    hdl.bucket = buckets.alloc(DFT_BUCKET_BLOCK_SIZE);
                }
                // 如果有桶，且已封盖，且已经被其他对象使用，在写入模式下的时候就创建一个新桶
                else if (bu.isSealed()) {
                    if (bu.getCountRefer() > 1) {
                        hdl.bucket = bu.duplicateVirtual();
                    } else {
                        bu.unseal();
                        hdl.bucket = bu;
                    }
                }
                // 没有封盖的桶，继续使用
                else {
                    hdl.bucket = bu;
                }

                // 准备缓冲区
                hdl.swap = new byte[hdl.bucket.getBlockSize()];
                hdl.swap_size = 0;

                // 记录一下，对象已经被占用
                o.setWriteHandle(hdl.id);
                o.setRWMetaKeys("^_write_handle$");

            }
            // 读模式，就用原来的桶即可
            else {
                hdl.bucket = bu;
            }
        }

        // 返回
        return hdl.id;
    }

    @Override
    public WnObj close(String hid) {
        WnHandle hdl = __check_hdl(hid);

        // 刷新缓存
        __flush(hdl);

        // 写操作的句柄需要额外处理，读操作就神马也表用做了
        if (Wn.S.canWite(hdl.mode)) {
            // 如果是修改元数据
            if (hdl.obj.isRWMeta()) {
                String json = hdl.bucket.getString();
                NutMap map = Json.fromJson(NutMap.class, json);
                // 删除不该修改的 KEY
                map.remove("id");

                // 修改元数据
                hdl.obj.putAll(map);

                // 生成需要修改的元数据正则表达式
                hdl.obj.setRWMetaKeys("^(" + Lang.concat("|", map.keySet()) + ")$");
            }
            // 仅仅是修改内容
            else if (hdl.updated) {
                // 如果是修改模式，采用桶的长度作为对象的长度
                if (Wn.S.canModify(hdl.mode)) {
                    hdl.obj.len(hdl.bucket.getSize());
                }
                // 否则将会剪裁桶的长度
                else if (hdl.pos >= 0) {
                    hdl.obj.len(hdl.pos);
                    hdl.bucket.trancateSize(hdl.pos);
                }

                // 保存最后修改时间
                hdl.obj.lastModified(hdl.bucket.getLastModified());

                // 确保引用桶
                __refer_to_bucket(hdl);

                // 最后封盖桶
                String sha1 = hdl.bucket.seal();
                hdl.obj.sha1(sha1);

                // 标记对象不在被写占用
                hdl.obj.setWriteHandle(null);

                // 标记元数据
                hdl.obj.setRWMetaKeys("^(_write_handle|lm|len|sha1|data)$");
            }
            // 没有修改内容，将写占位置空
            else {
                hdl.obj.setWriteHandle(null);
                hdl.obj.setRWMetaKeys("^_write_handle$");
            }
        }

        // 如果句柄引用了本地桶，还是要释放一下的
        if (null != hdl.bucket && hdl.bucket instanceof LocalFileBucket) {
            ((LocalFileBucket) hdl.bucket).closeFile();
        }

        // 移除句柄
        handles.remove(hid);

        // 返回
        return hdl.obj;
    }

    private void __flush(WnHandle hdl) {
        if (hdl.swap_size > 0) {
            // 存入桶
            hdl.bucket.write(hdl.pos, hdl.swap, 0, hdl.swap_size);

            // 移动指针
            hdl.pos += hdl.swap_size;

            // 缓冲归零
            hdl.swap_size = 0;
        }
    }

    @Override
    public WnObj flush(String hid) {
        WnHandle hdl = __check_hdl(hid);

        // 读操作不需要刷新缓冲
        if (Wn.S.isRead(hdl.mode))
            return hdl.obj;

        // 刷新缓冲
        __flush(hdl);

        // 不是读写元数据的话，如果写过数据则需要更新对象的索引
        if (hdl.updated && !hdl.obj.isRWMeta()) {
            // 修改对象的索引
            hdl.obj.len(hdl.bucket.getSize());
            hdl.obj.sha1(null);
            hdl.obj.lastModified(hdl.bucket.getLastModified());

            // 引用桶
            if (!hdl.obj.isMount())
                __refer_to_bucket(hdl);

            // 标记要修改的元数据
            hdl.obj.setRWMetaKeys("^(data|sha1|len|lm)$");
        }

        return hdl.obj;
    }

    private void __refer_to_bucket(WnHandle hdl) {
        String data = hdl.obj.data();
        // 首次给对象附加桶
        if (null == data) {
            hdl.obj.data(hdl.bucket.getId());
            hdl.bucket.refer();
        }
        // 这是mount的桶,不用动!!
        else if (data.contains("://")) {
            return;
        }
        // 切换桶
        else if (!data.equals(hdl.bucket.getId())) {
            // 需要释放原桶
            WnBucket bu = buckets.getById(data);
            if (null != bu) {
                bu.free();
            }
            // 再引用新桶
            hdl.obj.data(hdl.bucket.getId());
            hdl.bucket.refer();
        }
        // 否则没啥需要做的
    }

    @Override
    public void write(String hid, byte[] bs, int off, int len) {
        WnHandle hdl = __check_hdl(hid);

        // 标志第一次写
        if (!hdl.updated)
            hdl.updated = true;

        // 真的写
        hdl.pos += hdl.bucket.write(hdl.pos, bs, off, len);
    }

    @Override
    public void write(String hid, byte[] bs) {
        write(hid, bs, 0, bs.length);
    }

    @Override
    public int read(String hid, byte[] bs, int off, int len) {
        WnHandle hdl = __check_hdl(hid);
        if (null == hdl.bucket)
            return -1;

        if (hdl.pos >= hdl.bucket.getSize())
            return -1;

        int re = hdl.bucket.read(hdl.pos, bs, off, len);
        hdl.pos += re;
        return re;
    }

    @Override
    public int read(String hid, byte[] bs) {
        return read(hid, bs, 0, bs.length);
    }

    @Override
    public void seek(String hid, long pos) {
        WnHandle hdl = __check_hdl(hid);
        if (Wn.S.canAppend(hdl.mode))
            throw Er.create("e.io.seek.append", hdl.obj);

        hdl.pos = pos;
        handles.save(hdl);
    }

    @Override
    public void delete(WnObj o) {
        WnBucket bu = __check_bucket(o);
        if (null != bu)
            bu.free();
    }

    @Override
    public void delete(WnObj o, boolean r) {
        this.delete(o);
    }

    private WnBucket __check_bucket(WnObj o) {
        // 对象元数据句柄，忽略
        if (o.isRWMeta()) {
            return null;
        }

        // 空对象
        if (!o.hasData())
            return null;

        // 分析文件数据
        String data = o.data();

        // 如果是映射到本地文件，只读 
        if (null != data && data.startsWith("file://")) {
            throw Er.create("e.io.local.readonly", o);
        }

        // 否则就是默认的桶实现
        return buckets.getById(data);
    }

    @Override
    public void trancate(WnObj o, long len) {
        if (o.len() != len) {
            WnBucket bu = __check_bucket(o);
            // 剪裁桶
            if (null != bu) {
                bu.trancateSize(len);
                o.len(len);
                o.sha1(bu.getSha1());
                o.lastModified(bu.getLastModified());
                bu.update();
            }
            // 没对象的话，长度必须是 0
            else if (len != 0) {
                throw Er.createf("e.io.store.trancate.nodata", "%s => %dbytes", o, len);
            }
            // 将对象置空
            else {
                o.len(len);
                o.sha1(Lang.sha1(""));
                o.lastModified(System.currentTimeMillis());
            }

            // 标记要修改的元数据
            o.setRWMetaKeys("^(sha1|len|lm)$");
        }
    }

    private WnHandle __check_hdl(String hid) {
        WnHandle hdl = handles.get(hid);
        if (null == hdl) {
            throw Er.create("e.io.nohdl", hid);
        }
        return hdl;
    }
    
    @Override
    public long getPos(String hid) {
        return __check_hdl(hid).pos;
    }
}
