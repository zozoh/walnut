package org.nutz.walnut.core.bm.localbm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;

public class LocalIoReadOnlyHandle extends LocalIoHandle {

    private FileInputStream input;

    private FileChannel chan;

    LocalIoReadOnlyHandle(LocalIoBM bm) {
        super(bm);
    }

    @Override
    public long skip(long n) throws IOException {
        this.offset += n;
        chan.position(this.offset);

        this.touch();

        return this.offset;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        if (null == input) {
            File buck = this.getBuckFile();
            // 虚桶
            if (!buck.exists()) {
                return 0;
            }
            // 准备吧
            input = new FileInputStream(buck);
            chan = input.getChannel();
        }

        // 包裹一下
        ByteBuffer bb = ByteBuffer.wrap(buf, off, len);

        // 读取
        int re = chan.read(bb);
        if (re > 0) {
            this.offset += re;
        }

        // 更新自身过期时间
        this.touch();

        return re;
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        throw Er.create("e.io.bm.localbm.hdl.readonly");
    }

    @Override
    public void flush() {}

    @Override
    public void close() {
        Streams.safeClose(chan);
        Streams.safeClose(input);

        // 删除句柄
        manager.remove(this.getId());
    }
}
