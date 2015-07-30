package org.nutz.walnut.api.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import org.nutz.json.JsonFormat;

public interface WnIo extends WnStore, WnTree {

    void setMount(WnObj o, String mnt);

    void writeMeta(WnObj o, Object meta);

    void appendMeta(WnObj o, Object meta);

    String readText(WnObj o);

    long readAndClose(WnObj o, OutputStream ops);

    <T> T readJson(WnObj o, Class<T> classOfT);

    long writeText(WnObj o, CharSequence cs);

    long appendText(WnObj o, CharSequence cs);

    long writeJson(WnObj o, Object obj, JsonFormat fmt);

    long writeAndClose(WnObj o, InputStream ins);

    Reader getReader(WnObj o, long off);

    Writer getWriter(WnObj o, long off);

    MimeMap mimes();

}
