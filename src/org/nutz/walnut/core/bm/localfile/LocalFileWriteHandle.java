package org.nutz.walnut.core.bm.localfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoHandle;

public class LocalFileWriteHandle extends WnIoHandle {

    private LocalFileBM bm;

    private FileOutputStream output;

    private FileChannel chan;

    LocalFileWriteHandle(LocalFileBM bm) {
        this.bm = bm;
    }

    @Override
    public void setObj(WnObj obj) {
        super.setObj(obj);
        File f = bm.checkFile(obj.data());
        try {
            this.output = new FileOutputStream(f);
            this.chan = this.output.getChannel();
        }
        catch (FileNotFoundException e) {
            throw Lang.wrapThrow(e);
        }
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
        throw Er.create("e.io.bm.localfile.hdl.writeonly");
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(buf, off, len);
        int re = chan.write(bb);

        if (re > 0) {
            this.offset += re;
        }

        // 更新自身过期时间
        this.touch();
    }

    @Override
    public void flush() throws IOException {
        try {
            chan.force(true);
        }
        catch (Exception e) {}
        Streams.safeFlush(output);
    }

    @Override
    public void close() throws IOException {}

}
