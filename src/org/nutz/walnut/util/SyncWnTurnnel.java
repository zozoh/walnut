package org.nutz.walnut.util;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.walnut.api.box.WnTurnnel;

public class SyncWnTurnnel implements WnTurnnel {

    private WnTurnnel tnl;

    private SyncJTInputStream _ins;

    private SyncJTOutputStream _ops;

    public SyncWnTurnnel(WnTurnnel tnl) {
        this.tnl = tnl;
    }

    @Override
    public synchronized void reset() {
        this.tnl.reset();
    }

    @Override
    public synchronized InputStream asInputStream() {
        if (null == _ins) {
            _ins = new SyncJTInputStream(this);
        }
        return _ins;
    }

    @Override
    public synchronized OutputStream asOutputStream() {
        if (null == _ops) {
            _ops = new SyncJTOutputStream(this);
        }
        return _ops;
    }

    public synchronized boolean isReadable() {
        return tnl.isReadable();
    }

    public synchronized boolean isWritable() {
        return tnl.isWritable();
    }

    public synchronized void close() {
        tnl.close();
    }

    @Override
    public void closeRead() {
        tnl.closeRead();
    }

    @Override
    public void closeWrite() {
        tnl.closeWrite();
    }

    public synchronized byte read() {
        return tnl.read();
    }

    public synchronized int read(byte[] bs) {
        return tnl.read(bs);
    }

    public synchronized int read(byte[] bs, int off, int len) {
        return tnl.read(bs, off, len);
    }

    public synchronized void write(byte b) {
        tnl.write(b);
    }

    public synchronized void write(byte[] bs, int off, int len) {
        tnl.write(bs, off, len);
    }

    public synchronized void write(byte[] bs) {
        tnl.write(bs);
    }

    public synchronized long getReadSum() {
        return tnl.getReadSum();
    }

    @Override
    public synchronized String toString() {
        return tnl.toString();
    }

}
