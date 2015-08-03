package org.nutz.walnut.impl.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketManager;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnStore;
import org.nutz.walnut.impl.io.bucket.LocalFileBucket;
import org.nutz.walnut.impl.io.bucket.MemoryBucket;
import org.nutz.walnut.io.WnHandle;
import org.nutz.walnut.util.Wn;

public class WnStoreImpl implements WnStore {

    private static final int DFT_BUCKET_BLOCK_SIZE = 8192;

    private WnBucketManager buckets;

    private Map<String, WnHandle> handles;

    public void on_create() {
        handles = new HashMap<String, WnHandle>();
    }

    public void on_depose() {
        handles.clear();
    }

    @Override
    public void _clean_for_unit_test() {
        buckets._clean_for_unit_test();
        on_depose();
    }

    @Override
    public String open(WnObj o, int mode) {
        // 只能针对 FILE 进行操作
        if (o.isDIR()) {
            throw Er.create("e.io.dir.open", o);
        }

        // 创建句柄
        WnHandle hdl = new WnHandle();
        hdl.id = R.UU64();
        hdl.ct = System.currentTimeMillis();
        hdl.lm = hdl.ct;
        hdl.mode = mode;
        hdl.obj = o;
        hdl.pos = Wn.S.isAppend(mode) ? o.len() : 0;

        // 分析文件数据
        String data = o.data();

        // 对象元数据句柄
        if (o.isRWMeta()) {
            hdl.bucket = new MemoryBucket(DFT_BUCKET_BLOCK_SIZE);
            if (Wn.S.isRead(mode)) {
                String json = Json.toJson(o, JsonFormat.full());
                hdl.bucket.write(json);
            }
        }
        // 如果是映射到本地文件
        else if (null != data && data.startsWith("file://")) {
            if (!Wn.S.isReadOnly(mode)) {
                throw Er.create("e.io.local.readonly", o);
            }
            String ph = data.substring("file://".length());
            File f = Files.findFile(ph);
            if (null == f)
                throw Er.create("e.io.local.noexists", ph);
            hdl.bucket = new LocalFileBucket(f);
        }
        // 否则
        else {
            // 获取对象原有的桶
            WnBucket bu = Strings.isBlank(data) ? null : buckets.checkById(data);

            // 写入模式无论如何都要弄个新桶
            if (Wn.S.isWriteOrAppend(mode)) {
                // 如果没有桶，则分配一个
                if (null == bu) {
                    hdl.bucket = buckets.alloc(DFT_BUCKET_BLOCK_SIZE);
                }
                // 如果有桶，且已封盖，在写入模式下的时候就创建一个新桶
                else if (bu.sealed) {
                    hdl.bucket = bu.duplicate(true);
                }
                // 没有封盖的桶，继续使用
                else {
                    hdl.bucket = bu;
                }

                // 准备缓冲区
                hdl.swap = new byte[bu.block_size];
                hdl.swap_size = 0;
            }
        }

        // 持有句柄并返回
        handles.put(hdl.id, hdl);

        return hdl.id;
    }

    @Override
    public WnObj close(String hid) {
        WnHandle hdl = __check_hdl(hid);

        // 刷新缓存
        __flush(hdl);

        // 移除句柄
        handles.remove(hid);

        // 如果是追加
        if (Wn.S.isAppend(hdl.mode)) {

        }
        // 如果是写
        else if (Wn.S.isWite(hdl.mode)) {

        }

        // 返回
        return hdl.obj;
    }

    @Override
    public int read(String hid, byte[] bs, int off, int len) {
        WnHandle hdl = __check_hdl(hid);
        return hdl.bucket.read(hdl.pos, bs, off, len);
    }

    @Override
    public void write(String hid, byte[] bs, int off, int len) {
        WnHandle hdl = __check_hdl(hid);
        hdl.bucket.write(hdl.pos, bs, off, len);
    }

    @Override
    public void write(String hid, byte[] bs) {
        WnHandle hdl = __check_hdl(hid);
        hdl.bucket.write(hdl.pos, bs, 0, bs.length);
    }

    @Override
    public void write(String hid, String s) {
        WnHandle hdl = __check_hdl(hid);
        hdl.bucket.write(s);
    }

    @Override
    public int read(String hid, byte[] bs) {
        WnHandle hdl = __check_hdl(hid);
        return hdl.bucket.read(hdl.pos, bs, 0, bs.length);
    }

    @Override
    public String getString(String hid) {
        WnHandle hdl = __check_hdl(hid);
        return hdl.bucket.getString();
    }

    @Override
    public void seek(String hid, long pos) {
        WnHandle hdl = __check_hdl(hid);

        if (Wn.S.isAppend(hdl.mode))
            throw Er.create("e.io.seek.append", hdl.obj);
    }

    @Override
    public void flush(String hid) {
        WnHandle hdl = __check_hdl(hid);
        __flush(hdl);
    }

    public void __flush(WnHandle hdl) {
        if (hdl.swap_size > 0) {
            long index = hdl.pos / hdl.bucket.block_nb;
            int padding = (int) (hdl.pos - index * hdl.bucket.block_nb);
            hdl.bucket.write(index, padding, hdl.swap, hdl.swap_size);
            hdl.pos += hdl.swap_size;
            hdl.swap_size = 0;
        }
    }

    @Override
    public void delete(WnObj o) {
        throw Lang.noImplement();
    }

    private WnHandle __check_hdl(String hid) {
        WnHandle hdl = handles.get(hid);
        if (null == hdl) {
            throw Er.create("e.io.nohdl", hid);
        }
        return hdl;
    }
}
