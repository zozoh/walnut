package org.nutz.walnut.api.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.nutz.walnut.util.UnitTestable;

public interface WnStore extends UnitTestable {

    InputStream getInputStream(WnObj o, long off);

    /**
     * @param o
     * @param off
     *            -1 表示从尾部写，0 表示从头覆盖
     * @return
     */
    OutputStream getOutputStream(WnObj o, long off);

    List<WnHistory> getHistory(WnObj o);

}
