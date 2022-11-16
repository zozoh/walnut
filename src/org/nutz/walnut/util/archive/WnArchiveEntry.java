package org.nutz.walnut.util.archive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Streams;

public class WnArchiveEntry {

    public String name;

    public long len;

    public boolean dir;

    public String toString() {
        return String.format(" %s: %s : %dbytes", dir ? "D" : "F", name, len);
    }

    public void writeAndClose(InputStream ins, OutputStream ops, byte[] buf) throws IOException {
        try {
            int readed;
            while ((readed = ins.read(buf)) >= 0) {
                ops.write(buf, 0, readed);
            }
        }
        finally {
            Streams.safeClose(ops);
        }
    }
}
