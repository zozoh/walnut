package com.site0.walnut.core.io;

import java.io.IOException;
import java.io.OutputStream;

import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.api.lock.WnLockInvalidKeyException;

public class WnIoSaftyWrappedOutputStream extends OutputStream {

    private OutputStream ops;
    private WnLockApi lockApi;
    private WnLock lock;

    public WnIoSaftyWrappedOutputStream(OutputStream ops, WnLockApi locks, WnLock lock) {
        this.ops = ops;
        this.lockApi = locks;
        this.lock = lock;
    }

    public int hashCode() {
        return ops.hashCode();
    }

    public void write(int b) throws IOException {
        ops.write(b);
    }

    public void write(byte[] b) throws IOException {
        ops.write(b);
    }

    public boolean equals(Object obj) {
        return ops.equals(obj);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        ops.write(b, off, len);
    }

    public void flush() throws IOException {
        ops.flush();
    }

    public void close() throws IOException {
        ops.close();
        try {
            lockApi.freeLock(lock);
        }
        catch (WnLockInvalidKeyException e) {
            new IOException(e);
        }
    }

    public String toString() {
        return ops.toString();
    }

}
