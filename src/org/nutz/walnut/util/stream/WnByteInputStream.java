package org.nutz.walnut.util.stream;

import java.io.IOException;
import java.io.InputStream;

/**
 * 根据一个 byte[] 数组，构建一个 InputStream
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnByteInputStream extends InputStream {

    private byte[] bytes;

    private int cursor;

    private int length;

    public WnByteInputStream(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    @Override
    public int available() throws IOException {
        return bytes.length - cursor;
    }

    public WnByteInputStream(byte[] bytes, int off, int len) {
        this.bytes = bytes;
        this.cursor = off;
        this.length = off + len;
        if (this.length > bytes.length)
            this.length = bytes.length;
    }

    @Override
    public synchronized void mark(int readlimit) {
        this.cursor = readlimit;
    }

    @Override
    public synchronized void reset() {
        this.cursor = 0;
    }

    @Override
    public int read() {
        if (cursor < length)
            return bytes[cursor++] & 0xff;
        return -1;
    }

    @Override
    public int read(byte[] b) {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (cursor >= bytes.length)
            return -1;
        return readNBytes(b, off, len);
    }

    @Override
    public byte[] readAllBytes() {
        int c = bytes.length - cursor;
        return readNBytes(c);
    }

    @Override
    public byte[] readNBytes(int len) {
        int c = bytes.length - cursor;
        int n = Math.min(len, c);
        byte[] bs = new byte[n];
        if (n > 0) {
            System.arraycopy(bytes, cursor, bs, 0, n);
            cursor += n;
        }
        return bs;
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) {
        int c = bytes.length - cursor;
        int n = Math.min(len, c);
        n = Math.min(n, b.length - off);
        if (n > 0) {
            System.arraycopy(bytes, cursor, b, off, n);
            cursor += n;
        }
        return n;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

}
