package org.nutz.walnut.core.bm.local;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleManager;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.core.WnReferApi;
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
public class LocalIoBM extends AbstractIoBM {

    private File dBucket;

    private File dSwap;

    WnReferApi refers;

    private int minBucketIdLen;

    private int bufSize;

    public LocalIoBM(String home,
                     boolean autoCreate,
                     WnIoHandleManager handles,
                     WnReferApi refers) {
        super(handles);

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

        // 一些常量
        minBucketIdLen = 8;
        bufSize = 8192;
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
        return new LocalIoHandle(this);
    }

    @Override
    public WnIoHandle open(WnObj o, int mode, WnIoIndexer indexer) {
        // 先搞一个句柄
        WnIoHandle h = createHandle();
        h.setIndexer(indexer);
        h.setObj(o);
        h.setMode(mode);
        h.setOffset(0);

        // 只能有一个写,保存一下，不出错就成
        handles.save(h);

        return h;
    }

    @Override
    public long copy(String buckId, String referId) {
        return refers.add(buckId, referId);
    }

    @Override
    public long remove(String buckId, String referId) {
        long rec = refers.remove(buckId, referId);
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
        if (!o.hasData()) {
            if (len != 0) {
                throw Er.create("e.io.bm.trancate", "VirtualBucket to size(" + len + ")");
            }
            return 0;
        }
        // 呃，好像完全不需要剪裁的样子
        if (o.len() == len) {
            return len;
        }

        // 首先看看桶
        String buckId = o.data();
        long count = refers.count(buckId);

        // 如果桶的引用多余一个，那么建立一个新的桶
        if (count > 1) {
            return truncateToNewBucket(o, len, indexer);
        }
        // 否则直接剪裁桶文件
        if (count > 0) {
            return truncateSelfBucket(o, len, indexer);
        }
        // 木有引用，那么就是虚桶咯
        // 将一个虚桶剪裁到某个尺寸，臣妾做不到啊
        if (len > 0) {
            throw Er.create("e.io.bm.trancate", "VirtualBucket to size(" + len + ")");
        }
        return 0;

    }

    private long truncateSelfBucket(WnObj o, long len, WnIoIndexer indexer) {
        // 准备桶文件
        String oldBuckId = o.data();
        File fOldBucket = this.getBucketFile(oldBuckId);

        // 剪裁到 0 了，那么就虚桶吧
        if (0 == len) {
            if (fOldBucket.exists())
                fOldBucket.delete();
            o.len(0).sha1(Wn.Io.EMPTY_SHA1);
            indexer.set(o, "^(len|sha1)$");
            return 0;
        }

        // 虚桶
        if (!fOldBucket.exists()) {
            throw Er.create("e.io.bm.trancate", "VirtualBucket to size(" + len + ")");
        }

        // 剪裁
        FileOutputStream ops = null;
        FileChannel chan = null;
        try {
            ops = new FileOutputStream(fOldBucket);
            chan = ops.getChannel();
            chan.truncate(len);
            chan.close();
            Streams.safeClose(chan);
            Streams.safeClose(ops);
            chan = null;
            ops = null;
            String sha1 = Lang.sha1(fOldBucket);
            o.len(fOldBucket.length()).sha1(sha1);
            indexer.set(o, "^(len|sha1)$");
            return o.len();
        }
        catch (Exception e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(chan);
            Streams.safeClose(ops);
        }
    }

    private long truncateToNewBucket(WnObj o, long len, WnIoIndexer indexer) {
        // 那么首先 建立个新桶文件
        String newBuckId = this.genBuckId();

        // 剪裁到 0 了，那么就虚桶吧
        if (0 == len) {
            o.data(newBuckId).len(0).sha1(Wn.Io.EMPTY_SHA1);
            indexer.set(o, "^(data|len|sha1)$");
            return 0;
        }
        // 准备桶文件
        String oldBuckId = o.data();
        File fOldBucket = this.getBucketFile(oldBuckId);
        // 虚桶
        if (!fOldBucket.exists()) {
            throw Er.create("e.io.bm.trancate", "VirtualBucket to size(" + len + ")");
        }

        // 新的桶文件
        File fNewBucket = this.getBucketFile(newBuckId);
        fNewBucket = Files.createFileIfNoExists(fNewBucket);

        // 完整 copy
        if (len == o.len()) {
            Files.copy(fOldBucket, fNewBucket);
            o.data(newBuckId).len(len);
        }
        // copy 一部分
        else {
            InputStream ins = null;
            OutputStream ops = null;
            try {
                ins = Streams.fileIn(fOldBucket);
                ops = Streams.fileOut(fNewBucket);
                long len2 = Streams.write(ops, ins, len, bufSize);
                Streams.safeClose(ins);
                Streams.safeFlush(ops);
                Streams.safeClose(ops);
                ins = null;
                ops = null;
                String sha1 = Lang.sha1(fNewBucket);
                o.data(newBuckId).len(len2).sha1(sha1);
            }
            catch (IOException e) {
                throw Lang.wrapThrow(e);
            }
            finally {
                Streams.safeClose(ins);
                Streams.safeClose(ops);
            }
        }

        // 更新索引
        indexer.set(o, "^(data|len|sha1)$");
        return o.len();
    }

    String genBuckId() {
        return R.UU32();
    }

    File getBucketFile(String buckId) {
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

}
