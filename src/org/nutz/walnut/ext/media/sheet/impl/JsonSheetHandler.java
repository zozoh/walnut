package org.nutz.walnut.ext.media.sheet.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Encoding;
import org.nutz.lang.util.NutMap;

public class JsonSheetHandler extends AbstractSheetHandler {

    @Override
    public SheetResult read(InputStream ins, NutMap conf) {
        Reader reader = new InputStreamReader(ins, Encoding.CHARSET_UTF8);
        return new SheetResult(Json.fromJsonAsList(NutMap.class, reader));
    }

    @Override
    public void write(OutputStream ops, List<NutMap> list, NutMap conf) {
        JsonFormat jfmt = JsonFormat.forLook().setQuoteName(true);
        jfmt.putAll(conf);
        Writer writer = new OutputStreamWriter(ops, Encoding.CHARSET_UTF8);
        Json.toJson(writer, list, jfmt);
    }

}