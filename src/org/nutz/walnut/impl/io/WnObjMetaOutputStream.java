package org.nutz.walnut.impl.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.box.WnTunnel;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.JvmTunnel;
import org.nutz.walnut.util.Wn;

public class WnObjMetaOutputStream extends OutputStream {

    private WnTunnel tnl;

    private WnObj o;

    private boolean append;

    private WnIoImpl io;

    public WnObjMetaOutputStream(WnObj o, WnIoImpl io, boolean append) {
        this.o = o;
        this.io = io;
        this.tnl = new JvmTunnel(256);
        this.append = append;
    }

    public void flush() throws IOException {
        // 读取全部的内容
        Reader reader = Streams.utf8r(tnl.asInputStream());
        String json = Streams.readAndClose(reader);

        if (!Strings.isBlank(json)) {
            NutMap newObj = Json.fromJson(NutMap.class, json);

            // 计入最后修改时间
            newObj.put("lm", Wn.now());

            // 准备统计要修改的 keys
            List<String> keys = new ArrayList<String>(newObj.size() + o.size());
            // 追加模式，必须得给咱点啥才能追加呀
            if (newObj.size() > 0) {
                // 追加模式
                if (append) {
                    for (String key : newObj.keySet()) {
                        // id 等是绝对不可以改的
                        if (key.matches("^(ph|id|race|ct|d[0-9])$")) {
                            continue;
                        }
                        // 获取值
                        Object v = newObj.get(key);
                        // 不能为空
                        if (null == v
                            && key.matches("^(nm|tp|pid|len|sha1|data|ct|lm|c|m|g|md)$")) {
                            continue;
                        }
                        // 计入对象
                        keys.add(key);
                        o.setv(key, v);
                    }
                }
                // 更新模式，要检查全部字段
                else {
                    // 循环对象
                    for (String key : o.keySet()) {
                        // id 等是绝对不可以改的
                        if (key.matches("^(ph|id|race|ct|d[0-9])$")) {
                            continue;
                        }
                        // 获取值
                        Object v = newObj.get(key);
                        // 不能为空
                        if (null == v
                            && key.matches("^(nm|tp|pid|len|sha1|data|ct|lm|c|m|g|md)$")) {
                            continue;
                        }
                        // 计入对象
                        keys.add(key);
                        o.setv(key, v);
                    }
                    // 添加更多的字段
                    for (String key : newObj.keySet()) {
                        // id 等是绝对不可以改的
                        if (key.matches("^(ph|id|race|ct|d[0-9])$")) {
                            continue;
                        }
                        // 如果原始对象不包括这个字段，追加
                        if (!o.containsKey(key)) {
                            Object v = newObj.get(key);
                            keys.add(key);
                            o.setv(key, v);
                        }
                    }

                }
            }

            // 没啥好改的，就手工咯
            if (keys.isEmpty())
                return;
            
            // 确保有最后修改时间
            keys.add("lm");
            
            String regex = "^(" + Lang.concat("|", keys) + ")$";
            // 计入索引
            io.set(o, regex);
        }

        // 清空缓存
        tnl.reset();

    }

    public void close() throws IOException {
        this.flush();
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
