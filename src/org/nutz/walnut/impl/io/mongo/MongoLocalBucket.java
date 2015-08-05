package org.nutz.walnut.impl.io.mongo;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.nutz.walnut.api.io.AbstractBucket;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketBlockInfo;

public class MongoLocalBucket extends AbstractBucket {

    private static final byte B0 = (byte) 0;

    @MoIgnore
    private ZMoCo _co;

    @MoIgnore
    private File dir;

    @MoIgnore
    private WnBucket pbu;

    public MongoLocalBucket co(ZMoCo co) {
        this._co = co;
        return this;
    }

    public MongoLocalBucket dir(File dir) {
        this.dir = dir;
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
        int fLen = (int) Math.min(f.length(), blockSize);

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

        // 按块读取
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
                n = Math.min((int) f.length(), len);
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
        this.setCountRead(countRead + 1);
        this.setLastReaded(System.currentTimeMillis());

        // 返回
        return re;
    }

    @Override
    public int write(int index, int padding, byte[] bs, int off, int len) {
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

        // 写文件
        OutputStream ops = null;
        try {
            ops = Streams.fileOut(f);

            // 写左边距
            if (padding > 0) {
                byte[] pls = new byte[padding];
                ops.write(pls);
            }

            // 填充字节
            int n = Math.min(blockSize - padding, len);
            ops.write(bs, off, n);
            re = n + padding;
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(ops);
        }

        // 看看是否需要修改块的数量
        if (index >= blockNumber) {
            blockNumber = index + 1;
        }

        // 如果写的是最后的块，修改有效逻辑长度
        if (index == (blockNumber - 1)) {
            size = index * blockSize + re;
        }

        // 清除指纹
        sha1 = null;

        // 记录
        this.setLastWrited(System.currentTimeMillis());

        // 返回实际写入的字节数
        return re;

    }

    @Override
    public void trancate(int nb) {
        if (nb < blockNumber) {
            for (long i = nb; i < blockNumber; i++) {
                File f = Files.getFile(dir, "" + i);
                if (f.exists())
                    f.delete();
            }
            this.setBlockNumber(nb);
        }
    }

    @Override
    public String seal() {
        return null;
    }

    @Override
    public void unseal() {
        throw Lang.noImplement();
    }

    @Override
    public void update() {
        ZMoDoc doc = ZMo.me().toDoc(this);
        _co.save(doc);
    }

    @Override
    public WnBucket duplicateVirtual() {
        return null;
    }

    @Override
    public long refer() {
        return ++countRefer;
    }

    @Override
    public long free() {
        countRefer--;
        if (countRefer <= 0) {
            Files.deleteDir(dir);
            _co.remove(WnMongos.qID(id));
        }
        return this.countRefer;
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

    @MoField("crefer")
    private long countRefer;

    @MoField("cread")
    private long countRead;

    @MoField("b_sz")
    private int blockSize;

    @MoField("b_nb")
    private int blockNumber;

    @MoField("pbid")
    private String parentBucketId;

    @MoField("sz")
    private long size;

    @MoField("sha1")
    private String sha1;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSealed() {
        return sealed;
    }

    public void setSealed(boolean sealed) {
        this.sealed = sealed;
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
        return countRefer;
    }

    public void setCountRefer(long countRefer) {
        this.countRefer = countRefer;
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

    public void setParentBucket(WnBucket bu) {
        this.parentBucketId = bu.getId();
        this.pbu = bu;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
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
