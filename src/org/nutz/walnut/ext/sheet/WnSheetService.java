package org.nutz.walnut.ext.sheet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Mirror;
import org.nutz.lang.Streams;
import org.nutz.lang.born.Borning;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sheet.impl.CsvSheetHandler;
import org.nutz.walnut.ext.sheet.impl.JsonSheetHandler;
import org.nutz.walnut.ext.sheet.impl.XlsSheetHandler;
import org.nutz.walnut.ext.sheet.impl.XlsxSheetHandler;

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

    public List<NutMap> read(WnObj oSheet, NutMap conf) {
        InputStream ins = io.getInputStream(oSheet, 0);
        String type = oSheet.type();
        return readAndClose(ins, type, conf);
    }

    public List<NutMap> readAndClose(InputStream ins, String type, NutMap conf) {
        try {
            SheetHandler sh = __check_handler(type);
            return sh.read(ins, conf);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    public void write(WnObj oSheet, List<NutMap> list, NutMap conf) {
        OutputStream ops = io.getOutputStream(oSheet, 0);
        String type = oSheet.type();
        this.writeAndClose(ops, type, list, conf);
    }

    public void writeAndClose(OutputStream ops, String type, List<NutMap> list, NutMap conf) {
        this.writeAndClose(ops, type, list, conf, null, null);
    }

    public void writeAndClose(OutputStream ops,
                              String type,
                              List<NutMap> list,
                              NutMap conf,
                              WnOutputable out,
                              String process) {
        try {
            SheetHandler sh = __check_handler(type);
            sh.setProcess(out, process);
            sh.write(ops, list, conf);
            Streams.safeFlush(ops);
        }
        finally {
            Streams.safeClose(ops);
        }
    }
}
