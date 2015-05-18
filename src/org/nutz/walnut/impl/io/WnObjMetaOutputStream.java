package org.nutz.walnut.impl.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.box.WnTunnel;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.JvmTunnel;

public class WnObjMetaOutputStream extends OutputStream {

    private WnTunnel tnl;

    private WnObj o;

    private WnIndexer indexer;

    private boolean append;

    public WnObjMetaOutputStream(WnObj o, WnIndexer indexer, boolean append) {
        this.o = o;
        this.indexer = indexer;
        this.tnl = new JvmTunnel(256);
        this.append = append;
    }

    public void flush() throws IOException {
        // 读取全部的内容
        Reader reader = Streams.utf8r(tnl.asInputStream());
        String json = Streams.readAndClose(reader);

        if (!Strings.isBlank(json)) {
            NutMap newObj = Json.fromJson(NutMap.class, json);
            // 追加模式，必须得给咱点啥才能追加呀
            if (!append || newObj.size() > 0) {
                // 追加字段
                if (append) {
                    for (String key : newObj.keySet()) {
                        Object v = newObj.get(key);
                        o.setv(key, v);
                    }
                }
                // 更新字段
                else {
                    for (String key : o.keySet()) {
                        // 必须保留的
                        if (key.matches("^id|nm|tp|ph|race|pid|mnt|len|sha1|data|nano|ct|lm|c|m|g|md|d[0-9]$")) {
                            continue;
                        }
                        // 其他的设置
                        Object v = newObj.get(key);
                        o.setv(key, v);
                    }
                }
                // 添加更多的字段
                for (String key : newObj.keySet()) {
                    if (!o.containsKey(key)) {
                        Object v = newObj.get(key);
                        o.setv(key, v);
                    }
                }
                // 如果是追加，限制一下
                String regex = null;
                if (append) {
                    regex = "^" + Lang.concat("|", newObj.keySet()) + "$";
                }
                // 计入索引
                indexer.set(o, regex);
            }
        }

        // 清空缓存
        tnl.reset();
    }

    public void close() {
        tnl = null;
    }

    public void write(int b) throws IOException {
        if (null == tnl)
            throw new IOException("Stream is closed");
        tnl.write((byte) b);
    }

    public void write(byte[] bs, int off, int len) throws IOException {
        if (null == tnl)
            throw new IOException("Stream is closed");
        tnl.write(bs, off, len);
    }

    public void write(byte[] bs) throws IOException {
        if (null == tnl)
            throw new IOException("Stream is closed");
        tnl.write(bs);
    }

}
