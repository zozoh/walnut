package com.site0.walnut.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class RandomAccessFileInputStream extends InputStream {

    private RandomAccessFile raf;

    public RandomAccessFileInputStream(File f) {
        this(f, "r");
    }

    public RandomAccessFileInputStream(File f, String mode) {
        try {
            this.raf = new RandomAccessFile(f, mode);
        }
        catch (FileNotFoundException e) {
            throw Wlang.wrapThrow(e);
        }
    }

    public RandomAccessFileInputStream(RandomAccessFile raf) {
        this.raf = raf;
    }

    @Override
    public int read() throws IOException {
        return raf.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return raf.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return raf.skipBytes((int) n);
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

    @Override
    public synchronized void reset() throws IOException {
        raf.seek(0);
    }

}
