package org.nutz.walnut.ext.sheet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sheet.impl.CsvSheetHandler;
import org.nutz.walnut.ext.sheet.impl.JsonSheetHandler;

public class WnSheetService {

    private static final Map<String, SheetHandler> handlers;

    static {
        handlers = new HashMap<String, SheetHandler>();
        handlers.put("csv", new CsvSheetHandler());
        handlers.put("json", new JsonSheetHandler());
    }

    private static SheetHandler __check_handler(String type) {
        SheetHandler sh = handlers.get(type);
        if (null == sh) {
            throw Er.create("e.sheet.noHandler", type);
        }
        return sh;
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
        SheetHandler sh = __check_handler(type);
        try {
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
        SheetHandler sh = __check_handler(type);
        try {
            sh.write(ops, list, conf);
            Streams.safeFlush(ops);
        }
        finally {
            Streams.safeClose(ops);
        }
    }
}
