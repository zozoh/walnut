package com.site0.walnut.ext.media.sheet.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.util.Ws;

public class JsonSheetHandler extends AbstractSheetHandler {

    @Override
    public SheetResult read(InputStream ins, NutMap conf) {
        Reader reader = new InputStreamReader(ins, java.nio.charset.StandardCharsets.UTF_8);
        return new SheetResult(Json.fromJsonAsList(NutMap.class, reader));
    }

    @Override
    public void write(OutputStream ops, List<NutBean> list, List<String> headKeys, NutMap conf) {
        JsonFormat jfmt = JsonFormat.forLook().setQuoteName(true);
        jfmt.putAll(conf);
        if (headKeys != null && !headKeys.isEmpty()) {
            jfmt.setActived("^(" + Ws.join(headKeys, "|") + ")$");
        }
        Writer writer = new OutputStreamWriter(ops, java.nio.charset.StandardCharsets.UTF_8);
        Json.toJson(writer, list, jfmt);
    }

}
