package org.nutz.walnut.ext.net.xapi.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.util.LinkedByteBuffer;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.xml.CheapXmlParsing;
import org.nutz.walnut.ext.net.xapi.XApiCacheObj;
import org.nutz.walnut.ext.net.xapi.XApiException;
import org.nutz.walnut.ext.net.xapi.bean.XApiRequest;
import org.nutz.walnut.util.stream.WnStreams;

public class NilXapiCacheObj implements XApiCacheObj {

    protected XApiRequest req;

    NilXapiCacheObj(XApiRequest req) {
        this.req = req;
    }

    @Override
    public boolean isMatched() {
        return false;
    }

    @Override
    public <T> T getOutput(Class<T> classOfT) {
        return null;
    }

    @Override
    public <T> T saveAndOutput(InputStream resp, Class<T> classOfT) {
        return doOutput(resp, classOfT);
    }

    @SuppressWarnings("unchecked")
    protected <T> T doOutput(InputStream resp, Class<T> classOfT) {
        // 输出成字节数组
        if (classOfT.isArray() && byte.class == classOfT.getComponentType()) {
            return (T) asBytes(resp);
        }
        // 输出成 Reader
        if (classOfT == Reader.class || classOfT == BufferedReader.class) {
            return (T) asReader(resp, classOfT);
        }
        // 输出成 InputStream
        if (classOfT == InputStream.class) {
            return (T) asInputStream(resp);
        }

        // 根据数据类型转换
        String dataType = req.getDataType();

        // JSON
        if (req.isDataAsJson()) {
            String json = WnStreams.readTextAndClose(resp);
            return asJson(json, classOfT);
        }

        // Pure Text
        if (req.isDataAsText()) {
            String str = WnStreams.readTextAndClose(resp);
            return asText(str, classOfT);
        }

        // XML
        if (req.isDataAsXml()) {
            String xml = WnStreams.readTextAndClose(resp);
            return asXml(classOfT, xml);
        }

        // 不能支持哦
        throw new XApiException(req, "resp.convert", dataType);
    }

    @SuppressWarnings("unchecked")
    protected <T> T asXml(Class<T> classOfT, String xml) {
        // 要解析成 Document
        if (classOfT.isAssignableFrom(CheapDocument.class)) {
            CheapDocument doc = new CheapDocument(null);
            CheapXmlParsing ing = new CheapXmlParsing(doc);
            doc = ing.parseDoc(xml);
            return (T) doc;
        }
        // 要解析成 Element
        if (classOfT.isAssignableFrom(CheapElement.class)) {
            CheapDocument doc = new CheapDocument(null);
            CheapXmlParsing ing = new CheapXmlParsing(doc);
            doc = ing.parseDoc(xml);
            return (T) doc;
        }
        // 尝试转换
        return Castors.me().castTo(xml, classOfT);
    }

    protected <T> T asText(String str, Class<T> classOfT) {
        return Castors.me().castTo(str, classOfT);
    }

    @SuppressWarnings("unchecked")
    protected <T> T asJson(String json, Class<T> classOfT) {
        if (classOfT.isAssignableFrom(String.class)) {
            return (T) json;
        }
        // 其他的类字符串
        if (classOfT.isAssignableFrom(StringBuilder.class)) {
            return (T) new StringBuilder(json);
        }
        // 转换一下
        return Json.fromJson(classOfT, json);
    }

    protected InputStream asInputStream(InputStream resp) {
        return resp;
    }

    protected <T> Reader asReader(InputStream resp, Class<T> classOfT) {
        try {
            Reader r = new InputStreamReader(resp, Encoding.UTF8);
            if (classOfT == BufferedReader.class) {
                return Streams.buffr(r);
            }
            return r;
        }
        catch (UnsupportedEncodingException e) {
            throw Lang.wrapThrow(e);
        }
    }

    protected byte[] asBytes(InputStream resp) {
        LinkedByteBuffer bytes = new LinkedByteBuffer(8192, 10, 1024 * 1024 * 100);
        byte[] buf = new byte[8192];
        int len;
        try {
            while ((len = resp.read(buf)) >= 0) {
                bytes.write(buf, 0, len);
            }
            return bytes.toArray();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

}
