package org.nutz.walnut.impl.io.mongo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.mongo.annotation.MoField;
import org.nutz.mongo.annotation.MoIgnore;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketBlockInfo;
import org.nutz.walnut.impl.io.AbstractBucket;

public class MongoLocalBucket extends AbstractBucket {

    private static final byte B0 = (byte) 0;

    @MoIgnore
    private ZMoCo _co;

    @MoIgnore
    private File dir;

    @MoIgnore
    private WnBucket _pbu;

    @MoIgnore
    private MongoLocalBucketManager buckets;

    private WnBucket pbu() {
        if (null == _pbu && this.isDuplicated()) {
            if (!Strings.isBlank(parentBucketId)) {
                _pbu = buckets.checkById(parentBucketId);
            }
        }
        return _pbu;
    }

    public MongoLocalBucket co(ZMoCo co) {
        this._co = co;
        return this;
    }

    public MongoLocalBucket dir(File dir) {
        this.dir = dir;
        return this;
    }

    public MongoLocalBucket manager(MongoLocalBucketManager buckets) {
        this.buckets = buckets;
        return this;
    }

    public MongoLocalBucket() {}

    @Override
    public boolean isDuplicated() {
        return !Strings.isBlank(parentBucketId);
    }

    @Override
    public int read(int index, byte[] bs, WnBucketBlockInfo bi) {
        this._assert_index_not_out_of_range(index);

        // 读虚桶
        if (index < beginIndex) {
            return this.pbu().read(index, bs, bi);
        }

        // 文件大小与块不相等
        if (blockSize != bs.length) {
            throw Er.create("e.io.bucket.block.nomatch",
                            "[" + index + "]:" + blockSize + ", bs[]=" + bs.length);
        }

        File f = Files.getFile(dir, "" + index);

        // 文件不存在，跳过读取
        if (!f.exists()) {
            return blockSize;
        }

        // 最多读取一个块大小，如果文件比一个块长，则忽略长出的部分
        int fLen = (int) Math.min(f.length(),
                                  index + 1 == blockNumber ? size - index * blockSize : blockSize);

        // 填充字节
        byte[] buf = new byte[blockSize];
        Arrays.fill(buf, B0);
        int re = __fill_buffer_by_file(f, 0, buf, 0, fLen);

        // 分析左边距
        int pl = 0;
        for (; pl < buf.length; pl++)
            if (buf[pl] != 0)
                break;

        // 填充到输出数组，边距透明
        int sz = fLen - pl;
        System.arraycopy(buf, pl, bs, pl, sz);

        // 分析布局
        if (null != bi) {
            bi.paddingLeft = pl;
            bi.size = sz;
            bi.paddingRight = blockSize - fLen;
            bi.len = pl + sz;
        }

        // 记录
        this.setCountRead(countRead + 1);
        this.setLastReaded(System.currentTimeMillis());

        // 返回读取的字节数
        return re;
    }

    @Override
    public int read(long pos, byte[] bs, int off, int len) {
        int re = 0;

        // 读虚桶
        int vsz = beginIndex * blockSize;
        if (pos < vsz) {
            int vlen = vsz - (int) pos;
            this.pbu().read(pos, bs, off, vlen);
            pos += vlen;
            off += vlen;
            len -= vlen;
        }

        // 按块读取本桶
        while (pos < size && len > 0) {
            // 找到块
            long index = pos / blockSize;
            _assert_index_not_out_of_range(index);

            // 找到偏移
            long from = pos - index * blockSize;

            // 找到文件
            File f = Files.getFile(dir, "" + index);
            int n = -1;

            // 文件存在才读取
            if (f.exists()) {
                n = Math.min((int) (size - pos), len);
                n = this.__fill_buffer_by_file(f, from, bs, off, n);
            }
            // 否则全当读过了一个块
            else {
                int logicSize = (int) Math.min(size - pos, blockSize);
                n = Math.min(logicSize, len);
            }

            // 计数
            re += n;
            len -= n;
            off += n;
            pos += n;
        }

        // 记录
        countRead++;
        lastReaded = System.currentTimeMillis();

        // 返回
        return re;
    }

    @Override
    public int write(int index, int padding, byte[] bs, int off, int len) {
        // 不能写爆掉
        if (padding + len > blockSize)
            throw Er.createf("e.io.bucket.block.w.OutOfRange",
                             "index=%d,padding=%d,len=%d",
                             index,
                             padding,
                             len);

        // 如果写在了桶的虚空间里，则立刻将桶虚空间变实
        if (index < this.beginIndex) {
            // copy 之前的桶块
            byte[] buf = new byte[blockSize];
            for (int i = 0; i < this.beginIndex; i++) {
                Arrays.fill(buf, B0);
                this.read(i, buf, null);
                File f = Files.getFile(dir, "" + i);
                Files.createFileIfNoExists(f);
                Files.write(f, buf);
            }

            // 脱离桶链
            this.parentBucketId = null;
            this.beginIndex = 0;
            this.pbu().free();

            // 更新
            this.update();
        }

        // 实际写入的字节数
        int re;

        // 创建文件
        File f = Files.getFile(dir, "" + index);
        if (!f.exists())
            try {
                f.createNewFile();
            }
            catch (IOException e1) {
                throw Lang.wrapThrow(e1);
            }

        // 改写文件中间
        if (padding < f.length()) {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(f, "rw");
                if (padding > 0)
                    raf.seek(padding);
                raf.write(bs, off, len);
                re = len;
            }
            catch (IOException e1) {
                throw Lang.wrapThrow(e1);
            }
            finally {
                Streams.safeClose(raf);
            }
        }
        // 追加文件末尾
        else {
            OutputStream ops = null;
            try {
                ops = new FileOutputStream(f, true);
                // 首先写入空字节
                int n = padding - (int) f.length();
                if (n > 0) {
                    byte[] buf = new byte[n];
                    ops.write(buf);
                }
                // 写入真正的内容
                ops.write(bs, off, len);
                re = n + len;
            }
            catch (IOException e2) {
                throw Lang.wrapThrow(e2);
            }
            finally {
                Streams.safeClose(ops);
            }
        }

        // 看看是否需要修改块的数量
        if (index >= blockNumber) {
            blockNumber = index + 1;
        }

        // 看看是否导致桶的长度改变
        long sz = ((long) index) * ((long) blockSize) + ((long) padding) + ((long) len);
        if (sz > size) {
            size = sz;
        }

        // 清除指纹
        sha1 = null;

        // 记录
        this.lastWrited = System.currentTimeMillis();
        this.lastModified = this.lastWrited;

        // 返回实际写入的字节数
        return re;

    }

    @Override
    public void trancateBlock(int nb) {
        if (nb < blockNumber) {
            for (long i = nb; i < blockNumber; i++) {
                File f = Files.getFile(dir, "" + i);
                if (f.exists())
                    f.delete();
            }
            this.blockNumber = nb;
            this.lastModified = System.currentTimeMillis();
        }
    }

    @Override
    public WnBucket duplicateVirtual() {
        // 未封盖的桶不能被复制
        if (!isSealed()) {
            throw Er.create("e.io.bucket.noseal", id);
        }

        // 创建一个桶
        MongoLocalBucket bu = (MongoLocalBucket) buckets.alloc(blockSize);

        // 标记头等元数据
        bu.setBlockNumber(blockNumber);

        // 如果桶是有内容的，考虑一下最后一块
        if (size > 0) {
            // 新桶下标从原桶结尾开始
            bu.beginIndex = blockNumber - 1;

            // 最后一块没满复制最后一个块
            if (blockNumber * blockSize != size) {
                int li = bu.beginIndex;
                byte[] bs = new byte[blockSize];
                int sz = this.read(li, bs, null);
                bu.write(li, 0, bs, 0, sz);
            }

            // 桶多于一个块，或者第一个块满了，则需要引用原桶
            if (blockNumber > 1 || size >= blockSize) {
                bu.setParentBucketId(id);
                this.refer();
            }
        }

        // 最后更新桶的信息
        bu.update();

        // 返回
        return bu;
    }

    @Override
    public String seal() {
        sealed = true;
        nodup = true;
        lastSealed = System.currentTimeMillis();
        lastModified = lastSealed;

        // 确保生成指纹
        getSha1();

        // 更新索引
        this.update();

        // 返回的 sha1
        return sha1;
    }

    @Override
    public void unseal() {
        sealed = false;
        lastOpened = System.currentTimeMillis();

        update();

    }

    @Override
    public void update() {
        ZMoDoc doc = ZMoDoc.SET(ZMo.me().toDoc(this));
        _co.update(WnMongos.qID(id), doc, true, false);
    }

    @Override
    public long refer() {
        ZMoDoc doc = _co.findAndModify(WnMongos.qID(id),
                                       ZMoDoc.NEW("refer", 1),
                                       null,
                                       false,
                                       ZMoDoc.M("$inc", "refer", 1),
                                       true,
                                       false);
        return doc.getLong("refer");
    }

    @Override
    public long free() {
        ZMoDoc doc = _co.findAndModify(WnMongos.qID(id),
                                       ZMoDoc.NEW("refer", 1),
                                       null,
                                       false,
                                       ZMoDoc.M("$inc", "refer", -1),
                                       true,
                                       false);
        long refer = doc.getLong("refer");
        if (refer <= 0) {
            Files.deleteDir(dir);
            _co.remove(WnMongos.qID(id));
        }
        return refer;
    }

    @MoField("id")
    private String id;

    @MoField("seal")
    private boolean sealed;

    @MoField("ct")
    private long createTime;

    @MoField("lm")
    private long lastModified;

    @MoField("lread")
    private long lastReaded;

    @MoField("lwrite")
    private long lastWrited;

    @MoField("lseal")
    private long lastSealed;

    @MoField("lopen")
    private long lastOpened;

    @MoField("cread")
    private long countRead;

    @MoField("b_sz")
    private int blockSize;

    @MoField("b_nb")
    private int blockNumber;

    @MoField("pbid")
    private String parentBucketId;

    @MoField("bei")
    private int beginIndex;

    @MoField("sz")
    private long size;

    @MoField("sha1")
    private String sha1;

    @MoField("nodup")
    private boolean nodup;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSealed() {
        return sealed;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLastReaded() {
        return lastReaded;
    }

    public void setLastReaded(long lastReaded) {
        this.lastReaded = lastReaded;
    }

    public long getLastWrited() {
        return lastWrited;
    }

    public void setLastWrited(long lastWrited) {
        this.lastWrited = lastWrited;
    }

    public long getLastSealed() {
        return lastSealed;
    }

    public void setLastSealed(long lastSealed) {
        this.lastSealed = lastSealed;
    }

    public long getLastOpened() {
        return lastOpened;
    }

    public void setLastOpened(long lastOpened) {
        this.lastOpened = lastOpened;
    }

    public long getCountRefer() {
        ZMoDoc doc = _co.findOne(WnMongos.qID(id), ZMoDoc.NEW("refer", 1));
        return doc.getLong("refer");
    }

    public long getCountRead() {
        return countRead;
    }

    public void setCountRead(long countRead) {
        this.countRead = countRead;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getParentBucketId() {
        return parentBucketId;
    }

    public void setParentBucketId(String pbid) {
        this.parentBucketId = pbid;
    }

    public long getSize() {
        return size;
    }

    public void trancateSize(long size) {
        if (this.size != size) {
            this.size = size;
            this.sha1 = null;
            this.lastModified = System.currentTimeMillis();

            int lb_sz = (int) size % blockSize;
            int b_nb = (int) (size / blockSize) + (lb_sz > 0 ? 1 : 0);

            if (b_nb != blockNumber) {
                this.trancateBlock(b_nb);
            }
        }
    }

    public String getSha1() {
        if (null == sha1)
            sha1 = _gen_sha1();

        return sha1;
    }

    private int __fill_buffer_by_file(File f, long pos, byte[] bs, int off, int len) {
        if (pos == 0) {
            InputStream ins = Streams.fileIn(f);

            try {
                return ins.read(bs, off, len);
            }
            catch (IOException e) {
                throw Lang.wrapThrow(e);
            }
            finally {
                Streams.safeClose(ins);
            }
        } else {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(f, "r");
                raf.seek(pos);
                return raf.read(bs, off, len);
            }
            catch (FileNotFoundException e) {
                throw Lang.wrapThrow(e);
            }
            catch (IOException e) {
                throw Lang.wrapThrow(e);
            }
            finally {
                Streams.safeClose(raf);
            }
        }
    }
}
