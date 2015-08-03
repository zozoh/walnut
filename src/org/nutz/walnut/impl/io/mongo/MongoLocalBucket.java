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
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.mongo.annotation.MoField;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.AbstractBucket;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketBlockInfo;

public class MongoLocalBucket extends AbstractBucket {

    private static final byte B0 = (byte) 0;

    private ZMoCo _co;

    private File home;

    public MongoLocalBucket co(ZMoCo co) {
        this._co = co;
        return this;
    }

    public MongoLocalBucket home(File home) {
        this.home = home;
        return this;
    }

    public MongoLocalBucket() {}

    @Override
    public boolean isDuplicated() {
        return !Strings.isBlank(fromBucketId);
    }

    @Override
    public int read(long index, byte[] bs, WnBucketBlockInfo bi) {
        this._assert_index_not_out_of_range(index);

        // 文件大小与块不相等
        if (blockSize != bs.length) {
            throw Er.create("e.io.bucket.block.nomatch",
                            "[" + index + "]:" + blockSize + ", bs[]=" + bs.length);
        }

        File f = Files.getFile(__dir(), "" + index);

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
        File dir = __dir();

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
    public void write(long index, int padding, byte[] bs, int off, int len) {
        // 不能超出块的边界
        int sz = Math.min(blockSize, padding + len);

        // 准备写的字节
        byte[] buf = new byte[sz];
        Arrays.fill(buf, B0);
        System.arraycopy(bs, off, buf, padding, sz);

        // 写入
        write(index, buf);
    }

    @Override
    public void write(long index, byte[] bs) {
        // 分析右边距
        int pl = 0;
        for (; pl < bs.length; pl++) {
            int i = bs.length - pl - 1;
            if (bs[i] != 0)
                break;
        }

        // 得到要写的长度
        int sz = bs.length - pl;

        // 创建文件
        File f = Files.getFile(__dir(), "" + index);
        Files.createFileIfNoExists(f);

        // 写文件
        OutputStream ops = null;
        try {
            ops = Streams.fileOut(f);
            ops.write(bs, 0, sz);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(ops);
        }

        // 看看是否需要修改块的大小
        if (index >= blockNumber) {
            this.setBlockNumber(index + 1);
        }

        // 记录
        this.setLastWrited(System.currentTimeMillis());

        // 写入期间不计算 SHA1
        if (null != sha1) {
            this.setSha1(null);
        }
        // 写入期间，不计算尺寸
        if (-1 != size) {
            this.setSize(-1);
        }

    }

    @Override
    public void trancate(long nb) {
        if (nb < blockNumber) {
            File dir = __dir();
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
    public void unseal() {}

    @Override
    public WnBucket duplicate(boolean dropData) {
        return null;
    }

    @Override
    public WnBucket margeWith(WnBucket bucket) {
        return null;
    }

    @Override
    public long refer() {
        this.setCountRefer(this.countRefer + 1);
        return this.countRefer;
    }

    @Override
    public long free() {
        this.setCountRefer(this.countRefer - 1);
        if (this.countRefer <= 0) {
            Files.deleteDir(__dir());
            _co.remove(WnMongos.qID(id));
        }
        return this.countRefer;
    }

    @MoField("id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @MoField("seal")
    private boolean sealed;

    public boolean isSealed() {
        return sealed;
    }

    public void setSealed(boolean sealed) {
        this.sealed = sealed;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("seal", sealed));
    }

    @MoField("ct")
    private long createTime;

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("ct", createTime));
    }

    @MoField("lm")
    private long lastModified;

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("lm", lastModified));
    }

    @MoField("lread")
    private long lastReaded;

    public long getLastReaded() {
        return lastReaded;
    }

    public void setLastReaded(long lastReaded) {
        this.lastReaded = lastReaded;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("lread", lastReaded));
    }

    @MoField("lwrite")
    private long lastWrited;

    public long getLastWrited() {
        return lastWrited;
    }

    public void setLastWrited(long lastWrited) {
        this.lastWrited = lastWrited;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("lwrite", lastWrited));
    }

    @MoField("lseal")
    private long lastSealed;

    public long getLastSealed() {
        return lastSealed;
    }

    public void setLastSealed(long lastSealed) {
        this.lastSealed = lastSealed;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("lseal", lastSealed));
    }

    @MoField("lopen")
    private long lastOpened;

    public long getLastOpened() {
        return lastOpened;
    }

    public void setLastOpened(long lastOpened) {
        this.lastOpened = lastOpened;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("lopen", lastOpened));
    }

    @MoField("crefer")
    private long countRefer;

    public long getCountRefer() {
        return countRefer;
    }

    public void setCountRefer(long countRefer) {
        this.countRefer = countRefer;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("crefer", countRefer));
    }

    @MoField("cread")
    private long countRead;

    public long getCountRead() {
        return countRead;
    }

    public void setCountRead(long countRead) {
        this.countRead = countRead;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("cread", countRead));
    }

    @MoField("b_sz")
    private int blockSize;

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("b_sz", blockSize));
    }

    @MoField("b_nb")
    private long blockNumber;

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("b_nb", blockNumber));
    }

    @MoField("fbid")
    private String fromBucketId;

    public String getFromBucketId() {
        return fromBucketId;
    }

    public void setFromBucketId(String fromBucketId) {
        this.fromBucketId = fromBucketId;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("fbid", fromBucketId));
    }

    @MoField("sz")
    private long size;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("sz", sha1));
    }

    @MoField("sha1")
    private String sha1;

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
        _co.update(WnMongos.qID(id), ZMoDoc.SET("sha1", sha1));
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

    private File __dir() {
        String ph = id.substring(0, 2) + "/" + id.substring(2);
        File d = Files.getFile(home, ph);
        if (!d.exists())
            throw Lang.makeThrow("!!!bucket dir '%s' noexists!!!", ph);
        return d;
    }
}
