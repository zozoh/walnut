package com.site0.walnut.util.stream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MarkableInputStream extends InputStream {

    public static InputStream WRAP(InputStream ins) {
        if (ins.markSupported()) {
            return ins;
        }
        return new MarkableInputStream(ins);
    }

    /**
     * 包裹的内部输入流
     */
    private InputStream ins;

    /**
     * 分配一个固定缓冲，当调用 mark 的时候，会分配这个属性实例。 <br>
     * 如果它分配了实例，每次读取字节的时候都会同时写入到这个缓冲里。 <br>
     * 当调用 reset 的时候，则会将内容写入到
     */
    private ByteBuffer buf;

    /**
     * 当 reset 的时候，如果 buf 里有内容，会读取出来，放到这个属性里. <br>
     * 同时会将 readed 属性重置为 0
     */
    private byte[] readedBytes;

    /**
     * 标识 readedBytes 被 reset 后，再次被读取的下标
     */
    private int readed;

    public MarkableInputStream(InputStream ins) {
        this.ins = ins;
    }

    @Override
    public int read() throws IOException {
        // 首先检查是否有已读取的字节在缓存中
        if (null != readedBytes && readed < readedBytes.length) {
            return readedBytes[readed++] & 0xFF;
        }

        // 如果没有缓存的字节，则从输入流读取
        int b = ins.read();

        // 如果设置了标记缓冲，将读取的字节写入缓冲
        if (null != buf && b != -1) {
            if (buf.remaining() > 0) {
                buf.put((byte) b);
            }
        }

        return b;
    }

    @Override
    public int read(byte[] b) throws IOException {
        // 委托给 read(byte[] b, int off, int len) 方法，该方法已经处理了 readedBytes 的逻辑
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // 参数校验
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        // 首先检查是否有已读取的字节在缓存中
        int count = 0;
        if (null != readedBytes && readed < readedBytes.length) {
            int remain = readedBytes.length - readed;
            int read = Math.min(remain, len);
            System.arraycopy(readedBytes, readed, b, off, read);
            readed += read;
            count += read;
            off += read;
            len -= read;

            // 如果已经满足需求长度，直接返回
            if (len == 0) {
                return count;
            }
        }

        // 如果没有缓存的字节或缓存已读完，则从输入流读取
        int n = ins.read(b, off, len);
        if (n > 0) {
            count += n;

            // 如果设置了标记缓冲，将读取的字节写入缓冲
            if (null != buf) {
                int putCount = Math.min(n, buf.remaining());
                if (putCount > 0) {
                    buf.put(b, off, putCount);
                }
            }
        }

        return count > 0 ? count : -1;
    }

    @Override
    public synchronized void mark(int readlimit) {
        // 分配一个新的缓冲区
        this.buf = ByteBuffer.allocate(readlimit);
        // 清空已读取的字节数组和读取索引
        this.readedBytes = null;
        this.readed = 0;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (null == buf) {
            throw new IOException("Mark not set");
        }

        // 将缓冲区的数据转移到已读取字节数组中
        buf.flip();
        this.readedBytes = new byte[buf.remaining()];
        buf.get(this.readedBytes);
        this.readed = 0;

        // 清空缓冲区
        this.buf = null;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void close() throws IOException {
        if (ins != null) {
            ins.close();
        }
        // 释放资源
        buf = null;
        readedBytes = null;
    }

    @Override
    public int available() throws IOException {
        // 已缓存的可用字节数
        int cached = 0;
        if (null != readedBytes) {
            cached = readedBytes.length - readed;
        }
        // 输入流可用的字节数
        int inAvailable = ins.available();
        return cached + inAvailable;
    }

}
