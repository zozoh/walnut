package com.site0.walnut.ext.media.sheet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Streams;
import org.nutz.lang.born.Borning;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.WnOutputable;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.media.sheet.impl.AbstractPoiSheetHandler;
import com.site0.walnut.ext.media.sheet.impl.CsvSheetHandler;
import com.site0.walnut.ext.media.sheet.impl.JsonSheetHandler;
import com.site0.walnut.ext.media.sheet.impl.SheetImage;
import com.site0.walnut.ext.media.sheet.impl.SheetResult;
import com.site0.walnut.ext.media.sheet.impl.XlsSheetHandler;
import com.site0.walnut.ext.media.sheet.impl.XlsxSheetHandler;

public class WnSheetService {

    private static final Map<String, Borning<? extends SheetHandler>> handlers;

    static {
        handlers = new HashMap<String, Borning<? extends SheetHandler>>();
        handlers.put("csv", Mirror.me(CsvSheetHandler.class).getBorning());
        handlers.put("json", Mirror.me(JsonSheetHandler.class).getBorning());
        handlers.put("xls", Mirror.me(XlsSheetHandler.class).getBorning());
        handlers.put("xlsx", Mirror.me(XlsxSheetHandler.class).getBorning());
    }

    private static SheetHandler __check_handler(String type) {
        Borning<? extends SheetHandler> shb = handlers.get(type);
        if (null == shb) {
            throw Er.create("e.sheet.noHandler", type);
        }
        return shb.born();
    }

    private WnIo io;

    public WnSheetService(WnIo io) {
        this.io = io;
    }

    public List<? extends NutBean> read(WnObj oSheet, NutMap conf) {
        InputStream ins = io.getInputStream(oSheet, 0);
        String type = oSheet.type();
        return readAndClose(ins, type, conf);
    }

    public List<? extends NutBean> readAndClose(InputStream ins, String type, NutMap conf) {
        try {
            SheetHandler sh = __check_handler(type);
            SheetResult result = sh.read(ins, conf);
            if (result.images != null) {
                String root = conf.getString("images", "~/.tmp/sheet_images/");
                for (SheetImage image : result.images) {
                    String name = String.format("%d_%d_%d.%s",
                                                image.sheetIndex,
                                                image.row,
                                                image.col,
                                                image.type == 0 ? "jpg" : "png");
                    WnObj wobj = io.createIfNoExists(null, root + "/" + name, WnRace.FILE);
                    io.writeAndClose(wobj, new ByteArrayInputStream(image.data));
                }
            }
            return result.list;
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    public void write(WnObj oSheet, List<NutBean> list, List<String> headKeys, NutMap conf) {
        OutputStream ops = io.getOutputStream(oSheet, 0);
        String type = oSheet.type();
        this.writeAndClose(ops, type, list, headKeys, conf);
    }

    public void writeAndClose(OutputStream ops,
                              String type,
                              List<NutBean> list,
                              List<String> headKeys,
                              NutMap conf) {
        this.writeAndClose(ops, type, list, headKeys, conf, null, null);
    }

    public void writeAndClose(OutputStream ops,
                              String type,
                              List<NutBean> list,
                              List<String> headKeys,
                              NutMap conf,
                              WnOutputable out,
                              String process) {
        try {
            SheetHandler sh = __check_handler(type);
            sh.setProcess(out, process);
            // 加载模板文件
            if (conf.has("tmpl") && sh instanceof AbstractPoiSheetHandler) {
                WnObj tmp = io.check(null, conf.getString("tmpl"));
                try {
                    ((AbstractPoiSheetHandler) sh).setTmpl(io.getInputStream(tmp, 0));
                }
                catch (IOException e) {
                    throw Lang.wrapThrow(e);
                }
            }
            // 有图片的话,处理一下
            for (NutBean tmp : list) {
                NutMap t = null;
                for (Map.Entry<String, Object> en : tmp.entrySet()) {
                    if (en.getValue() != null && en.getValue() instanceof String) {
                        String val = (String) en.getValue();
                        if (val.startsWith("image:")) {
                            SheetImageHolder holder = new WnSheetImageHolder(io,
                                                                             val.substring("image:".length()));
                            if (t == null)
                                t = new NutMap();
                            t.put(en.getKey(), holder);
                        }
                    }
                }
                if (t != null) {
                    tmp.putAll(t);
                }
            }
            sh.write(ops, list, headKeys, conf);
            Streams.safeFlush(ops);
        }
        finally {
            Streams.safeClose(ops);
        }
    }
}
