
package org.nutz.lang.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 根据一个 byte[] 数组，构建一个 InputStream
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ByteInputStream extends InputStream {

    private byte[] bytes;

    private int offset;

    private int cursor;

    private int tailIndex;

    private int markPosition;

    public ByteInputStream(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public ByteInputStream(byte[] bytes, int offset, int len) {
        if (bytes == null)
            throw new NullPointerException("bytes is null");
        if (offset < 0 || len < 0 || offset + len > bytes.length)
            throw new IndexOutOfBoundsException();
        this.bytes = bytes;
        this.offset = Math.max(0, offset);
        this.tailIndex = Math.min(offset + len, bytes.length);
        
        this.cursor = this.offset;
        this.markPosition = this.offset;
    }

    @Override
    public int read() throws IOException {
        if (cursor < tailIndex)
            return bytes[cursor++] & 0xff;
        return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null)
            throw new NullPointerException();
        if (off < 0 || len < 0 || len > b.length - off)
            throw new IndexOutOfBoundsException();
        if (len == 0)
            return 0;

        if (cursor >= tailIndex) {
            return -1;
        }

        int available = tailIndex - cursor;
        int bytesToRead = Math.min(available, len);
        if (bytesToRead <= 0) {
            return -1;
        }

        System.arraycopy(bytes, cursor, b, off, bytesToRead);
        cursor += bytesToRead;
        return bytesToRead;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }

        int newI = Math.min(tailIndex, cursor + (int) n);
        int skip = newI - cursor;
        cursor = newI;
        return skip;
    }

    @Override
    public int available() throws IOException {
        return Math.max(0, tailIndex - cursor);
    }

    @Override
    public synchronized void mark(int readlimit) {
        markPosition = cursor;
    }

    @Override
    public synchronized void reset() throws IOException {
        cursor = markPosition;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        if (out == null) {
            throw new NullPointerException();
        }
        
        int available = tailIndex - cursor;
        if (available <= 0) {
            return 0;
        }
        
        out.write(bytes, cursor, available);
        long transferred = available;
        cursor = tailIndex;
        return transferred;
    }

}