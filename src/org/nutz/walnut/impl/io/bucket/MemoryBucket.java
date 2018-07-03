package org.nutz.walnut.impl.io.bucket;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.nutz.filepool.FilePool;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketBlockInfo;
import org.nutz.walnut.impl.io.AbstractBucket;
import org.nutz.web.Webs.Err;

/**
 * 内存存储块, 第一个块在内存中,其余的在临时文件中
 * @author wendal
 *
 */
public class MemoryBucket extends AbstractBucket {
    
    private static final Log log = Logs.get();

    private static final byte B0 = (byte) 0;
    
    /**
     * 临时文件池
     */
    public static FilePool pool;

    private boolean sealed;
    private long createTime;
    private long lastModified;
    private long lastReaded;
    private long lastWrited;
    private long lastSealed;
    private long lastOpened;
    private long countRefer;
    private long countRead;
    private int blockSize;
    private int blockNumber;
    private int size;
    private String sha1;
    private byte[] membuf;
    private File f;

    public MemoryBucket(int blockSize) {
        this.blockSize = blockSize;
    }

    public String getSha1() {
        if (null == sha1)
            sha1 = _gen_sha1();

        return sha1;
    }

    public int read(int index, byte[] bs, WnBucketBlockInfo bi) {
        // 防御一下,难道想反向读吗?
        if (index < 0)
            return 0;
        // 超过边界了没?
        if (getBlockNumber() < index + 1)
            return 0;
        int sz;
        // 读内存
        if (index == 0) {
            if (membuf == null) {
                sz = 0;
            }
            else {
                // 算出可以读取的部分
                int len = Math.min(bs.length, membuf.length);
                len = Math.min(len, size);
                System.arraycopy(membuf, 0, bs, 0, Math.min(bs.length, membuf.length));
                sz = len;
            }
        } else { // 读文件
            if (f == null) {
                sz = 0;
            } else {
                sz = Files.readRange(f, index*blockSize, bs, 0, bs.length);
            }
        }

        if (null != bi) {
            // 啊啊啊, 必须填充对
            int pl = 0;
            int pr = bs.length - sz;
            bi.set(pl, sz, pr);
        }
        return sz;
    }

    @Override
    public int read(long _pos, byte[] bs, int off, int len) {
        // 暂不允许读取太大的文件, int long各种强转不靠谱
        if (_pos > Integer.MAX_VALUE)
            throw Err.create("e.memory.bucket.read_too_big");
        
        int re = 0;
        int pos = (int)_pos;
        // 不能越界读取
        if (pos + len > size)
            len = size - pos;
        // 呵呵, 0字节也想读?
        if (len < 1)
            return 0;
        // 从内存读, 因为pos落在第0块上
        if (pos < blockSize) {
            int mem_read_size = Math.min(blockSize - pos, len);
            if (membuf != null) {
                System.arraycopy(membuf, pos, bs, off, mem_read_size);
            } else {
                Arrays.fill(bs, off, mem_read_size, B0);
            }
            re += mem_read_size;
            // 读完内存,还需要继续吗?
            if (mem_read_size != len) {
                off += mem_read_size;
                len += mem_read_size;
                pos = blockSize;
            }
        }
        // 剩余的数据从文件读,如果有的话
        int file_read_count = Files.readRange(f, pos, bs, off, len);
        if (file_read_count > 0)
            re += file_read_count;

        // 计数
        this.countRead++;

        // 返回读取的有效字节数
        return re;
    }

    @Override
    public int write(int index, int padding, byte[] bs, int off, int len) {
        // 想干嘛? index还想负数?
        if (index < 0)
            return 0;
        // 写内存
        if (index == 0) {
            len = Math.min(blockSize - padding, Math.min(bs.length, len));
            if (membuf == null)
                membuf = new byte[blockSize];
            System.arraycopy(bs, off, membuf, padding, len);
        }
        // 写文件
        else {
            if (f == null) {
                try {
                    if (pool == null)
                        f = File.createTempFile("membuf", ".dat");
                    else
                        f = pool.createFile("dat");
                    log.info("buffer to " + f.getPath());
                }
                catch (IOException e) {
                    log.warn("create membuf fail", e);
                }
            }
            if (f != null)
                Files.writeRange(f, index*blockSize+padding, bs, off, Math.min(bs.length, len));
        }
        
        // 算一下当前大小
        if (f != null) {
            size = (int) f.length();
        } else {
            if (membuf != null) {
                if (size < padding + len)
                    size = padding + len;
            }
        }
        
        // 再算一下blockNumber
        blockNumber = (int) Math.ceil(((double) size) / ((double) blockSize));

        // 删除指纹缓冲
        sha1 = null;

        // 返回实际写入的字节数
        return len;
    }

    @Override
    public void trancateBlock(int nb) {
        if (nb == blockNumber) {
            return;
        }
        // 清零
        if (nb == 0) {
            membuf = null;
            clearFile();
            blockNumber = 0;
        }
        // 剪裁
        else if (nb == 1) {
            clearFile();
            blockNumber = nb;
        }
        // 清除指纹
        sha1 = null;
    }

    @Override
    public String seal() {
        return getSha1();
    }

    @Override
    public void unseal() {}

    @Override
    public void update() {}

    @Override
    public WnBucket duplicateVirtual() {
        throw Lang.noImplement();
    }

    @Override
    public long refer() {
        return 1;
    }

    @Override
    public long free() {
        return 1;
    }

    public boolean isSealed() {
        return sealed;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getLastReaded() {
        return lastReaded;
    }

    public long getLastWrited() {
        return lastWrited;
    }

    public long getLastSealed() {
        return lastSealed;
    }

    public long getLastOpened() {
        return lastOpened;
    }

    public long getCountRefer() {
        return countRefer;
    }

    public long getCountRead() {
        return countRead;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public String getParentBucketId() {
        throw Lang.noImplement();
    }

    public void setParentBucketId(String pbid) {
        throw Lang.noImplement();
    }

    public boolean isDuplicated() {
        return false;
    }

    public long getSize() {
        return size;
    }

    public void trancateSize(long size) {
        if (this.size == size)
            return;

        // 应该有多少块
        int b_nb = (int) Math.ceil(((double) size) / ((double) blockSize));
        trancateBlock(b_nb);

        // 保存有效尺寸
        this.size = (int)size;

        // 清除指纹
        this.sha1 = null;
    }

    public void clearFile() {
        if (f != null) {
            f.delete();
            f = null;
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        clearFile();
        // super.finalize();
    }
}
