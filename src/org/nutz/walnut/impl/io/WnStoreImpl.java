package org.nutz.walnut.impl.io;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnStore;

public class WnStoreImpl implements WnStore {

    @Override
    public void _clean_for_unit_test() {}

    @Override
    public String open(WnObj o, int mode) {
        return null;
    }

    @Override
    public void close(String hid) {}

    @Override
    public int read(String hid, byte[] bs, int off, int len) {
        return 0;
    }

    @Override
    public void write(String hid, byte[] bs, int off, int len) {}

    @Override
    public int read(String hid, byte[] bs) {
        return 0;
    }

    @Override
    public void write(String hid, byte[] bs) {}

    @Override
    public void delete(WnObj o) {}

    @Override
    public InputStream getInputStream(WnObj o, long off) {
        return null;
    }

    @Override
    public OutputStream getOutputStream(WnObj o, long off) {
        return null;
    }

}
