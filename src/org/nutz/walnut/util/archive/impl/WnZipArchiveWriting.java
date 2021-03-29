package org.nutz.walnut.util.archive.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.nutz.lang.Streams;

public class WnZipArchiveWriting extends AbstractWnArchiveWriting {

    private ZipOutputStream zip;

    public WnZipArchiveWriting(OutputStream ops) {
        this.prepare(ops);
    }

    @Override
    public void prepare(OutputStream ops) {
        zip = new ZipOutputStream(ops);
    }

    @Override
    public void addDirEntry(String entryName) throws IOException {
        ZipEntry ze = new ZipEntry(entryName);
        zip.putNextEntry(ze);
    }

    @Override
    public void addFileEntry(String entryName, byte[] b, int off, int len) throws IOException {
        ZipEntry ze = new ZipEntry(entryName);
        zip.putNextEntry(ze);
        zip.write(b, off, len);
        zip.flush();
        zip.closeEntry();
    }

    @Override
    public void addFileEntry(String entryName, InputStream ins, long len) throws IOException {
        ZipEntry ze = new ZipEntry(entryName);
        zip.putNextEntry(ze);
        Streams.write(zip, ins);
        zip.flush();
        zip.closeEntry();
        Streams.safeClose(ins);
    }

    @Override
    public void flush() throws IOException {
        zip.flush();
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }

}
