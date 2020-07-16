package org.nutz.walnut.core;

import org.nutz.walnut.api.io.WnObj;

public interface WnIoHandle {

    String getId();

    String getTargetId();

    long getCreatTime();

    int getPosition();

    int read(byte[] bs, int off, int len);

    void write(byte[] bs, int off, int len);

    void seek(long pos);

    void flush();

    void close();

    WnObj getObj();

    WnIoMapping getMapping();

}
