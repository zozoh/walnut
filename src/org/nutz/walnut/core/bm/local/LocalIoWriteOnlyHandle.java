package org.nutz.walnut.core.bm.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Wn;

public class LocalIoWriteOnlyHandle extends LocalIoHandle {

    private int written;

    /**
     * 记录是否已经写过一次了。在 flush 的时候，如果是第一次写，就 move file <br>
     * 之后都是追加文件，就用到了本类的第二个属性 appends
     */
    private int flushCount;

    /**
     * 第二次刷新的时候会被创建（桶文件的写入流）
     */
    private FileOutputStream buckOutput;

    private FileChannel buckChan;

    /**
     * 交换文件
     */
    private File swap;

    private RandomAccessFile swapOutput;

    /**
     * 缓冲用的读写通道
     */
    private FileChannel swapChan;

    LocalIoWriteOnlyHandle(LocalIoBM bm) {
        super(bm);
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        throw Er.create("e.io.bm.local.hdl.WriteOnly");
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        // 啥也没写
        if (len <= 0) {
            return;
        }
        // 确保创建写入流
        // 因为是第一次打开，所以就 "w" 就好，因为第一次 flush 会是直接移动文件的
        if (null == swap) {
            swap = bm.createSwapFile();
            swapOutput = new RandomAccessFile(swap, "w");
            swapChan = swapOutput.getChannel();
        }
        // 更新自身过期时间
        manager.touch(this.getId());

        // 写入
        ByteBuffer bb = ByteBuffer.wrap(buf, off, len);
        written += swapChan.write(bb);
    }

    @Override
    public void flush() throws IOException {
        try {
            // 如果是第一次刷新，那么直接就 copy 一下
            if (flushCount == 0) {
                firstFlush();
            }
            // 之后的，需要追加写入
            else if (written > 0) {
                otherFlush();
            }

        }
        finally {
            // 计数
            flushCount++;

            // 更新索引
            File buck = this.getBuckFile();
            long len = buck.length();
            String sha1 = 0 == len ? Wn.Io.EMPTY_SHA1 : Lang.sha1(buck);
            obj.len(len).sha1(sha1);
            this.indexer.set(obj, "^(len|sha1)$");
        }
    }

    private void otherFlush() throws FileNotFoundException, IOException {
        // 没有打开追加流，打开之
        if (null == buckOutput) {
            File buck = this.getBuckFile();
            // 不可能吧，第一次写入不是已经创建了嘛！
            if (!buck.exists()) {
                throw Lang.impossible();
            }
            buckOutput = new FileOutputStream(buck, true);
            buckChan = buckOutput.getChannel();
        }
        // 分配缓冲
        ByteBuffer buf = ByteBuffer.allocate(bm.bufferSize);
        // 开始追加写入
        int readed;
        while ((readed = swapChan.read(buf)) >= 0) {
            // 读到了东西
            if (readed > 0) {
                // 缓冲切换到读模式
                buf.flip();
                do {
                    buckChan.write(buf);
                } while (buf.hasRemaining());
                // 嗯，清空缓冲
                buf.clear();
            }
        }
        // 写入计数归零
        this.written = 0;
    }

    private void firstFlush() throws FileNotFoundException {
        File buck = this.getBuckFile();

        // 删掉旧桶,那么当前数据就是虚桶了
        if (buck.exists()) {
            buck.delete();
        }

        // 木有写过东东，那么直接更新一下索引
        if (null == swap) {
            return;
        }

        // 关闭交换文件，准备移动
        Streams.safeClose(swapChan);
        Streams.safeClose(swapOutput);

        // 将缓冲文件直接转移到桶的位置
        if (swap.renameTo(buck)) {
            // 缓冲归零
            swap = bm.createSwapFile();
            swapOutput = new RandomAccessFile(swap, "rw");
            swapChan = swapOutput.getChannel();
            this.buck = null;
            this.written = 0;
        }
        // 不可能吧
        else {
            throw Lang.impossible();
        }
    }

    @Override
    public void close() throws IOException {
        // 无论如何，刷一下
        this.flush();

        // 关闭资源
        Streams.safeClose(swapChan);
        Streams.safeClose(swapOutput);
        Streams.safeClose(buckChan);
        Streams.safeClose(buckOutput);

        // 删除句柄
        manager.remove(this.getId());

    }

}
