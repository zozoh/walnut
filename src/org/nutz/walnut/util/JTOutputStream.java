package org.nutz.walnut.util;

import java.io.OutputStream;

public class JTOutputStream extends OutputStream {

    private JvmTurnnel tnl;

    public JTOutputStream(JvmTurnnel turnnel) {
        this.tnl = turnnel;
    }

    @Override
    public void write(int b) {
        tnl.write((byte) b);
    }

    @Override
    public void write(byte[] bs, int off, int len) {
        tnl.write(bs, off, len);
    }

    @Override
    public void close() {
        tnl.r4W = null;
    }

}
