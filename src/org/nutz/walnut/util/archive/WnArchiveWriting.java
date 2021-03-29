package org.nutz.walnut.util.archive;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface WnArchiveWriting extends Closeable, Flushable {

    void prepare(OutputStream ops) throws IOException;

    void addDirEntry(String entryName) throws IOException;

    void addFileEntry(String entryName, InputStream ins, long len) throws IOException;

    void addFileEntry(String entryName, byte[] b, int off, int len) throws IOException;

    void addFileEntry(String entryName, byte[] b) throws IOException;

    void close() throws IOException;

}
