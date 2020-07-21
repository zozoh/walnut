package org.nutz.walnut.core.bm.localfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoHandle;

public class LocalFileReadHandle extends WnIoHandle {

    private LocalFileBM bm;

    private FileInputStream input;

    private FileChannel chan;

    LocalFileReadHandle(LocalFileBM bm) {
        this.bm = bm;
    }

    @Override
    public void setObj(WnObj obj) {
        super.setObj(obj);
        File f = bm.checkFile(obj.data());
        try {
            this.input = new FileInputStream(f);
            this.chan = this.input.getChannel();
        }
        catch (FileNotFoundException e) {
            throw Lang.wrapThrow(e);
        }
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        // 更新自身过期时间
        this.touch();
        // 填充缓冲
        ByteBuffer bb = ByteBuffer.wrap(buf, off, len);
        return chan.read(bb);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        throw Er.create("e.io.bm.localfile.hdl.readonly");
    }

    @Override
    public void flush() throws IOException {
        throw Er.create("e.io.bm.localfile.hdl.Readonly");
    }

    @Override
    public void close() throws IOException {
        Streams.safeClose(chan);
        Streams.safeClose(input);
    }

}
