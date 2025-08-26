package com.site0.walnut.ext.xo.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Each;
import org.nutz.lang.Streams;
import org.nutz.lang.util.ByteInputStream;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.xo.bean.XoBean;
import com.site0.walnut.ext.xo.util.XoClientGetter;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

public abstract class AbstractXoService<T> implements XoService {

    protected XoClientGetter<T> getter;

    protected static class XoMeta {
        Map<String, String> userMeta;
        String mime = null;
        String title = null;
        String sha1 = null;
        long len = -1;

        void dftMime(String ct) {
            if (null == mime && !Ws.isBlank(ct)) {
                mime = ct;
            }
        }

        void dftTitle(String cd) {
            if (null == title && !Ws.isBlank(cd)) {
                title = cd;
            }
        }

        void dftSha1(String sha1) {
            if (null == this.sha1 && !Ws.isBlank(sha1)) {
                this.sha1 = sha1;
            }
        }

        void dftLen(long len) {
            if (len < 0) {
                this.len = len;
            }
        }
    }

    protected XoMeta to_meta_data(Map<String, Object> meta,
                                  boolean noSha1,
                                  boolean ignoreNull) {
        XoMeta re = new XoMeta();
        re.userMeta = new HashMap<>();
        if (meta != null && !meta.isEmpty()) {
            for (Map.Entry<String, Object> en : meta.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                // 防空
                if (null == val && ignoreNull) {
                    continue;
                }

                String s = null == val ? null : val.toString();

                // 固定 mime
                if ("mime".equals(key)) {
                    re.mime = s;
                }
                // 固定 mime/Content-Type
                else if ("Content-Type".equalsIgnoreCase(key)) {
                    re.mime = s;
                }
                // 固定 len
                if ("len".equals(key)) {
                    re.len = null == s ? -1 : Long.parseLong(s);
                }
                // 固定 mime/Content-Length
                else if ("Content-Length".equalsIgnoreCase(key)) {
                    re.len = null == s ? -1 : Long.parseLong(s);
                }
                // 固定 title
                else if ("title".equals(key)) {
                    re.title = s;
                }
                // SHA1 指纹
                else if (!noSha1 && "sha1".equals(key)) {
                    re.sha1 = s;
                }
                // 内容长度
                else if (!noSha1 && "sha1".equals(key)) {
                    re.sha1 = s;
                }
                // 其他就是自定义属性
                // 如果指定 noSha1 那么， sha1 也会加入到自定义元数据
                // S3 在这点与 COS/OSS 有不同，它不支持
                else {
                    re.userMeta.put(key, s);
                }
            }
        }
        return re;
    }

    public boolean equals(Object other) {
        if (null == other)
            return false;
        if (!this.getClass().equals(other.getClass()))
            return false;

        return true;
    }

    public void mkdir(String dirKey) {
        String[] path = Ws.splitIgnoreBlank(dirKey, "/");
        String folder = Ws.join(path, "/") + "/";

        writeText(folder, "", null);
    }

    @Override
    public void writeText(String objKey,
                          String text,
                          Map<String, Object> meta) {
        InputStream ins = Wlang.ins(text);
        writeAndClose(objKey, ins, meta);
    }

    @Override
    public void writeBytes(String objKey, byte[] bs, Map<String, Object> meta) {
        InputStream ins = new ByteInputStream(bs);
        writeAndClose(objKey, ins, meta);
    }

    @Override
    public void writeAndClose(String objKey,
                              InputStream ins,
                              Map<String, Object> meta) {
        try {
            write(objKey, ins, meta);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    @Override
    public List<XoBean> listObj(String objKey) {
        return listObj(objKey, true, 1000);
    }

    @Override
    public List<XoBean> listObj(String objKey, boolean delimiter) {
        return listObj(objKey, delimiter, 1000);
    }

    public List<XoBean> listObj(String objKey,
                                boolean delimiterBySlash,
                                int limit) {
        List<XoBean> list = new LinkedList<>();
        this.eachObj(objKey, delimiterBySlash, limit, (index, xo, len) -> {
            list.add(xo);
        });
        return list;
    }

    @Override
    public int eachObj(String objKey, Each<XoBean> callback) {
        return eachObj(objKey, true, 1000, callback);
    }

    @Override
    public int eachObj(String objKey,
                       boolean delimiter,
                       Each<XoBean> callback) {
        return eachObj(objKey, delimiter, 1000, callback);
    }

    @Override
    public String readText(String objKey) {
        InputStream ins = read(objKey);
        Reader r = Streams.utf8r(ins);
        return Streams.readAndClose(r);
    }

    @Override
    public byte[] readBytes(String objKey) {
        InputStream ins = read(objKey);
        try {
            return Streams.readBytes(ins);
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    @Override
    public String renameKey(String key, String newName) {
        // 防空
        if (Ws.isBlank(key) || Ws.isBlank(newName)) {
            return key;
        }
        // 确保是一个名字
        if (newName.endsWith("/")) {
            newName = newName.substring(0, newName.length() - 1);
        }
        // 替换到原路径
        String[] path = Ws.splitIgnoreBlank(key, "/");
        boolean isDir = key.endsWith("/");
        // 替换名称
        path[path.length - 1] = newName;

        // 得到新的 key
        String newKey = Ws.join(path, "/");

        // 如果源是目录
        // 那么新名字也必须是目录
        if (isDir) {
            newKey += "/";
        }

        renameObj(key, newKey);

        return newKey;

    }

    @Override
    public void clear(String objKey) {
        List<XoBean> list = this.listObj(objKey, false);
        for (XoBean li : list) {
            this.deleteObj(li.getKey());
        }
    }

}