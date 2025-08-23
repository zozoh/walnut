package com.site0.walnut.ext.xo.impl;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Each;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.bean.XoBean;

public interface XoService {

    boolean equals(Object other);

    void mkdir(String dirKey);

    void writeText(String objKey, String text, Map<String, Object> meta);

    void writeBytes(String objKey, byte[] bs, Map<String, Object> meta);

    void write(String objKey, InputStream ins, Map<String, Object> meta);

    void writeAndClose(String objKey,
                       InputStream ins,
                       Map<String, Object> meta);

    List<XoBean> listObj(String objKey);

    List<XoBean> listObj(String objKey, boolean delimiter);

    List<XoBean> listObj(String objKey, boolean delimiter, int limit);

    int eachObj(String objKey, Each<XoBean> callback);

    int eachObj(String objKey, boolean delimiter, Each<XoBean> callback);

    int eachObj(String objKey,
                boolean delimiter,
                int limit,
                Each<XoBean> callback);

    XoBean getObj(String objKey);

    void appendMeta(String objKey, Map<String, Object> delta);

    void renameObj(String oldKey, String newKey);

    String renameKey(String key, String newName);

    InputStream read(String objKey);

    String readText(String objKey);

    byte[] readBytes(String objKey);

    void deleteObj(String objKey);

    void copy(String srcKey, String dstKey);

    void clear(String objKey);
    
}