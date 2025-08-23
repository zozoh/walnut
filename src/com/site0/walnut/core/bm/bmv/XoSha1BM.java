package com.site0.walnut.core.bm.bmv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.core.bm.AbstractIoBM;
import com.site0.walnut.core.bm.BMSwapFiles;
import com.site0.walnut.core.bm.Sha1Parts;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

public class XoSha1BM extends AbstractIoBM {

    XoService api;

    BMSwapFiles swaps;

    private WnReferApi refers;

    Sha1Parts parts;

    public XoSha1BM(WnIoHandleManager handles,
                    String phSwap,
                    boolean autoCreate,
                    String signAlg,
                    String parts,
                    XoService api,
                    WnReferApi refers) {
        super(handles);
        this.api = api;
        // 仅仅支持 SHA1
        if ("sha1".equals(signAlg)) {
            throw Er.create("e.io.bm.XoSha1BM.invalidSignAlg", signAlg);
        }

        // 如何分段, parts=22 表示 [2,2]
        // 譬如签名: b10d47941e27dad21b63fb76443e1669195328f2
        // 对应路径: b1/0d/47941e27dad21b63fb76443e1669195328f2
        this.parts = new Sha1Parts(parts);

        // 获取交换区目录
        this.swaps = BMSwapFiles.create(phSwap, autoCreate);

        // 引用计数管理器
        this.refers = refers;
    }

    @Override
    public boolean isSame(WnIoBM bm) {
        if (this == bm)
            return true;
        if (null == bm)
            return false;
        if (bm instanceof XoSha1BM) {
            if (!Wlang.isEqual(this.api, ((XoSha1BM) bm).api)) {
                return false;
            }
            if (!Wlang.isEqual(this.parts, ((XoSha1BM) bm).parts)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public WnIoHandle createHandle(int mode) {
        // 只读
        if (Wn.S.isRead(mode)) {
            return new XoSha1ReadHandle(this);
        }
        // 只写
        if (Wn.S.isWrite(mode)) {
            return new XoSha1WriteHandle(this);
        }
        // 追加
        if (Wn.S.isAppend(mode)) {
            // TODO : 是不是可以对追加模式做优化呢？
            return new XoSha1ReadWriteHandle(this);
        }
        // 修改
        if (Wn.S.canModify(mode) || Wn.S.isReadWrite(mode)) {
            return new XoSha1ReadWriteHandle(this);
        }
        throw Er.create("e.io.bm.XoSha1BM.NonsupportMode", mode);
    }

    @Override
    public long copy(WnObj oSr, WnObj oTa) {
        // 防守一下
        if (!oSr.hasSha1()) {
            return -1;
        }
        // 如果目标不是空的，那么检查一下是否有必要 Copy
        if (!Wn.Io.isEmptySha1(oTa.sha1())) {
            // 嗯，木有必要 Copy
            if (oSr.isSameSha1(oTa.sha1())) {
                return oTa.len();
            }
            // 已经引用了其他的数据，取消一下引用
            this.remove(oTa);
        }

        // 增加引用
        return refers.add(oSr.sha1(), oTa.id());
    }

    @Override
    public long remove(WnObj o) {
        String sha1 = o.sha1();
        long rec = refers.remove(sha1, o.id());
        // 归零了，那么要删除
        if (rec <= 0) {
            String objKey = parts.toPath(sha1);
            api.deleteObj(objKey);
        }
        return rec;
    }

    @Override
    public long truncate(WnObj o, long len, WnIoIndexer indexer) {
        // 没有桶，剪裁个屁
        if (!o.hasSha1()) {
            if (len != 0) {
                throw Er.create("e.io.bm.trancate",
                                "VirtualBucket to size(" + len + ")");
            }
            return 0;
        }
        // 呃，好像完全不需要剪裁的样子
        if (o.len() == len) {
            return len;
        }

        // 首先生成一个交换文件
        File swap = this.swaps.createSwapFile();

        try {
            load_to_swap(o, swap);

            // 剪裁交换文件
            RandomAccessFile raf = null;
            FileChannel chan = null;
            try {
                raf = new RandomAccessFile(swap, "rw");
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

            // 根据交换文件更新对象的索引
            this.updateObjSha1AndSaveSwap(o, swap, indexer);
            swap = null;

            // 搞定
            return o.len();
        }
        // 无论怎样，确保删除交换文件
        finally {
            if (null != swap && swap.exists()) {
                Files.deleteFile(swap);
            }
        }
    }

    void load_to_swap(WnObj obj, File swap) {
        String objKey = parts.toPath(obj.sha1());
        InputStream ins = api.read(objKey);
        OutputStream ops = Streams.fileOut(swap);
        Streams.writeAndClose(ops, ins);
    }

    @Override
    public void updateObjSha1(WnObj o, File swap, WnIoIndexer indexer) {
        throw Er
            .create("XoSha1BM deprecated updateObjSha1, use updateObjSha1AndSaveSwap instead");
    }

    @Override
    public void updateObjSha1(WnObj o,
                              WnIoIndexer indexer,
                              String sha1,
                              long len,
                              long lm) {
        throw Er
            .create("XoSha1BM deprecated updateObjSha1, use updateObjSha1AndSaveSwap instead");
    }

    /**
     * 根据给定的交换文件，更新文件对象的 <code>sha1|len|lm</code> 三个字段。
     * <p>
     * 它会考虑到下面的情况：
     * <ul>
     * <li>如果文件对象为空(empty sha1)
     * <li>文件对象的内容与交换文件一致
     * <li>文件对象的内容与交换文件不一致
     * </ul>
     * 
     * @param o
     *            对象
     * @param swap
     *            交换文件
     * @param indexer
     *            索引管理器
     * @throws IOException
     *             IO 读写发生异常
     */
    public void updateObjSha1AndSaveSwap(WnObj o,
                                         File swap,
                                         WnIoIndexer indexer) {
        String sha1 = null;
        long olen = 0;
        long lm = -1;
        // 某些时候，没有调用写接口的句柄实例，或者仅仅写了空字节的实例
        // 并不会生成 swap 文件
        if (null != swap && null != o) {
            sha1 = Wlang.sha1(swap);
            olen = swap.length();
            lm = swap.lastModified();
        }

        if (null != o) {
            if (lm <= 0) {
                lm = Wn.now();
            }
            // 如果和原来的一样,那就无语了，啥也不用做了
            if (o.isSameSha1(sha1)) {
                return;
            }

            String oldSha1 = o.sha1();
            o.sha1(sha1);
            o.lastModified(lm);
            o.len(olen);

            // 看看目的地是否存在，如果不存在就移动过去（标记null，防止删除）
            boolean isEmptySha1 = Wn.Io.isEmptySha1(sha1);
            if (!isEmptySha1) {
                String objKey = parts.toPath(sha1);

                // 不存在就创建
                if (null == api.getObj(objKey)) {
                    InputStream ins = Streams.fileIn(swap);
                    NutBean meta = o.pick("sha1", "mime", "title");
                    api.write(objKey, ins, meta);
                }

                // 增加引用计数
                this.refers.add(sha1, o.id());
            }

            // 删除旧引用
            if (!Wn.Io.isEmptySha1(oldSha1)) {
                long count = this.refers.remove(oldSha1, o.id());
                // 木有用了，删掉这个文件
                if (count <= 0) {
                    String oldObjKey = parts.toPath(oldSha1);
                    api.deleteObj(oldObjKey);
                }
            }

            // 更新索引
            indexer.set(o, "^(sha1|len|lm)$");
        }
    }
}
