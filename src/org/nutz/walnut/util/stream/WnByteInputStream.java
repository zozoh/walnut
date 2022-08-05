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

    public WnByteInputStream(byte[] bytes, int off, int len) {
        this.bytes = bytes;
        this.cursor = off;
        this.length = off + len;
        if (this.length > bytes.length)
            this.length = bytes.length;
    }

    @Override
    public int read() throws IOException {
        if (cursor < length)
            return bytes[cursor++] & 0xff;
        return -1;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

}
