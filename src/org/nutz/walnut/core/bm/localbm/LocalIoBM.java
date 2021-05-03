package org.nutz.walnut.core.bm.localbm;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleManager;
import org.nutz.walnut.core.WnReferApi;
import org.nutz.walnut.core.bm.AbstractIoBM;
import org.nutz.walnut.core.bm.localfile.LocalFileReadWriteHandle;
import org.nutz.walnut.util.Wn;

/**
 * 本地桶管理器，将数据存放在本地
 * 
 * <pre>
 * Bucket Home/
 * #-----------------------------------------
 * # 桶文件
 * |-- buck/          # 桶目录
 * |   |-- 0evq/      # 采用首4字符散列目录
 * |   |-- 89..g1     # 后面 36字符（可能更多）作为文件
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
public class LocalIoBM extends AbstractIoBM {

    private static final Log log = Wlog.getIO();

    private File dBucket;

    private File dSwap;

    WnReferApi refers;

    private int minBucketIdLen;

    int bufferSize;

    boolean canMoveSwap;

    public LocalIoBM(WnIoHandleManager handles,
                     String phBucket,
                     String phSWap,
                     boolean autoCreate,
                     WnReferApi refers) {
        super(handles);

        // 获取桶目录
        dBucket = new File(phBucket);
        if (!dBucket.exists()) {
            // 不自动创建，就自裁！！！
            if (!autoCreate) {
                throw Er.create("e.io.bm.local.BucketHomeNotFound", phBucket);
            }
            dBucket = Files.createDirIfNoExists(phBucket);
        }
        // 不是目录，自裁
        if (!dBucket.isDirectory()) {
            throw Er.create("e.io.bm.local.BucketHomeMustBeDirectory", dBucket.getAbsolutePath());
        }

        // 获取交换区目录
        dSwap = new File(phSWap);
        if (!dSwap.exists()) {
            // 不自动创建，就自裁！！！
            if (!autoCreate) {
                throw Er.create("e.io.bm.local.SwapHomeNotFound", phSWap);
            }
            dSwap = Files.createDirIfNoExists(phBucket);
        }
        // 不是目录，自裁
        if (!dSwap.isDirectory()) {
            throw Er.create("e.io.bm.local.SwapHomeMustBeDirectory", dSwap.getAbsolutePath());
        }

        // 句柄管理器
        this.handles = handles;

        // 引用计数管理器
        this.refers = refers;

        // 一些常量
        minBucketIdLen = 8;
        bufferSize = 8192;
    }

    @Override
    public boolean isSame(WnIoBM bm) {
        if (this == bm) {
            return true;
        }
        if (bm instanceof LocalIoBM) {
            LocalIoBM libm = (LocalIoBM) bm;
            if (!libm.dBucket.equals(dBucket))
                return false;
            if (!libm.dSwap.equals(dSwap))
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
            return new LocalIoReadHandle(this);
        }
        // 只写
        if (Wn.S.isWrite(mode)) {
            return new LocalIoWriteHandle(this);
        }
        // 追加
        if (Wn.S.isAppend(mode)) {
            return new LocalFileReadWriteHandle();
        }
        // 修改
        if (Wn.S.canModify(mode) || Wn.S.isReadWrite(mode)) {
            return new LocalIoReadWriteHandle(this);
        }
        throw Er.create("e.io.bm.localbm.NonsupportMode", mode);
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
        String buckId = o.sha1();
        long rec = refers.remove(buckId, o.id());
        // 归零了，那么要删除
        if (rec <= 0) {
            File fBuck = this.getBucketFile(buckId);
            if (fBuck.exists())
                fBuck.delete();
        }
        return rec;
    }

    @Override
    public long truncate(WnObj o, long len, WnIoIndexer indexer) {
        // 没有桶，剪裁个屁
        if (!o.hasSha1()) {
            if (len != 0) {
                throw Er.create("e.io.bm.trancate", "VirtualBucket to size(" + len + ")");
            }
            return 0;
        }
        // 呃，好像完全不需要剪裁的样子
        if (o.len() == len) {
            return len;
        }

        // 得到桶文件
        File buck = this.checkBucketFile(o.sha1());

        // 首先生成一个交换文件
        File swap = this.createSwapFile();

        try {
            Files.copy(buck, swap);

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
                throw Lang.wrapThrow(e);
            }
            finally {
                Streams.safeClose(chan);
                Streams.safeClose(raf);
            }

            // 根据交换文件更新对象的索引
            this.updateObjSha1(o, swap, indexer);
            swap = null;

            // 搞定
            return o.len();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        // 无论怎样，确保删除交换文件
        finally {
            if (null != swap && swap.exists()) {
                Files.deleteFile(swap);
            }
        }
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
    void updateObjSha1(WnObj o, File swap, WnIoIndexer indexer) throws IOException {
        // 无需更新，因为没有对象，对应的句柄肯定已经关闭了
        if (null == o) {
            return;
        }
        String sha1 = null;
        long olen = 0;
        long lm = Wn.now();
        // 某些时候，没有调用写接口的句柄实例，或者仅仅写了空字节的实例
        // 并不会生成 swap 文件
        if (null != swap) {
            sha1 = Lang.sha1(swap);
            olen = swap.length();
            lm = swap.lastModified();
        }
        boolean isEmptySha1 = Wn.Io.isEmptySha1(sha1);
        try {
            // 如果和原来的一样,那就无语了，啥也不用做了
            if (o.isSameSha1(sha1)) {
                return;
            }

            String oldSha1 = o.sha1();
            o.sha1(sha1);
            o.lastModified(lm);
            o.len(olen);

            // 看看目的地是否存在，如果不存在就移动过去（标记null，防止删除）
            if (!isEmptySha1) {
                File buck = this.getBucketFile(sha1);

                if (!buck.exists()) {
                    // OSS 映射的文件不支持 move，需要把这个开关关山
                    if (canMoveSwap) {
                        Files.move(swap, buck);
                    }
                    // Copy 的方式移动过去
                    else {
                        moveSwapToBuck(swap, buck);
                    }
                    // 无论如何，空置一下缓冲
                    swap = null;
                }
            }

            // 记录引用
            if (!Wn.Io.isEmptySha1(oldSha1)) {
                long count = this.refers.remove(oldSha1, o.id());
                // 木有用了，删掉这个文件
                if (count <= 0) {
                    File oldBuck = this.getBucketFile(oldSha1);
                    Files.deleteFile(oldBuck);
                }
            }

            // 非空的 SHA1，增加引用计数
            if (!isEmptySha1) {
                this.refers.add(sha1, o.id());
            }

            // 更新索引
            indexer.set(o, "^(sha1|len|lm)$");

        }
        // 无论如何，尝试移除交换文件
        finally {
            if (null != swap) {
                Files.deleteFile(swap);
            }
        }
    }

    private void moveSwapToBuck(File swap, File buck) throws IOException {
        boolean needCopy = true;
        String buph = Files.getAbsPath(buck);
        if (!buck.exists()) {
            // 这个判断还是要在本节点同步一下
            synchronized (this) {
                if (!buck.exists()) {
                    // 创建，如果失败，那么就是已经存在了
                    // 这种情况，通常是其他节点写了这个 buck
                    // 那么就警告一下就算了
                    if (!Files.createNewFile(buck)) {
                        log.warnf("LocalIoBM: buck(%s) exists!", buph);
                        needCopy = false;
                    }
                }
            }
        }
        // 执行 copy
        if (needCopy) {
            if (!Files.copy(swap, buck)) {
                String swph = Files.getAbsPath(swap);
                log.warnf("LocalIoBM: buck(%s) Fail to copy from %s!", buph, swph);
            }
            // Copy 成功，删除缓冲
            // Copy 失败，则保留缓冲，以备后面调试
            else {
                Files.deleteFile(swap);
            }
        }
        // 不需要 Copy 的话，直接删缓冲
        else {
            Files.deleteFile(swap);
        }
    }

    public File getBucketFile(String buckId) {
        // 桶ID是空的，什么情况！
        if (Strings.isBlank(buckId)) {
            throw Er.create("e.io.bm.local.BlankBucketId");
        }
        // 桶ID有点短啊
        buckId = buckId.trim();
        if (buckId.length() < minBucketIdLen) {
            throw Er.create("e.io.bm.local.BucketIdTooShort");
        }
        // 得到桶的路径
        String ph = buckId.substring(0, 4) + "/" + buckId.substring(4);
        return Files.getFile(dBucket, ph);
    }

    public File checkBucketFile(String buckId) {
        File buck = this.getBucketFile(buckId);
        if (!buck.exists()) {
            throw Er.create("e.io.bm.local.LostBucket", buckId + "::" + Files.getAbsPath(buck));
        }
        return buck;
    }

    public File createSwapFile() {
        String nm = R.UU32();
        File f = Files.getFile(dSwap, nm);
        return Files.createFileIfNoExists(f);
    }

}
