package org.nutz.walnut.util.archive.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.nutz.lang.Streams;

public class WnTarArchiveWriting extends AbstractWnArchiveWriting {

    private TarArchiveOutputStream tar;

    public WnTarArchiveWriting(OutputStream ops) {
        this.prepare(ops);
    }

    @Override
    public void prepare(OutputStream ops) {
        tar = new TarArchiveOutputStream(ops);
    }

    @Override
    public void addDirEntry(String entryName) throws IOException {
        ArchiveEntry ae = new TarArchiveEntry(entryName);
        tar.putArchiveEntry(ae);
    }

    @Override
    public void addFileEntry(String entryName, byte[] b, int off, int len) throws IOException {
        TarArchiveEntry ae = new TarArchiveEntry(entryName);
        ae.setSize(len - off);
        tar.putArchiveEntry(ae);
        tar.write(b, off, len);
        tar.flush();
        tar.closeArchiveEntry();
    }

    @Override
    public void addFileEntry(String entryName, InputStream ins, long len) throws IOException {
        TarArchiveEntry ae = new TarArchiveEntry(entryName);
        ae.setSize(len);
        tar.putArchiveEntry(ae);
        Streams.write(tar, ins);
        tar.flush();
        tar.closeArchiveEntry();
        Streams.safeClose(ins);
    }

    @Override
    public void flush() throws IOException {
        tar.flush();
    }

    @Override
    public void close() throws IOException {
        tar.close();
    }

}
