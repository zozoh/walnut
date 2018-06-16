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

public class WnSheetService {

    private static final Map<String, SheetHandler> handlers;

    static {
        handlers = new HashMap<String, SheetHandler>();
        handlers.put("csv", new CsvSheetHandler());
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

    public List<NutMap> read(NutMap opt, WnObj oSheet) {
        InputStream ins = io.getInputStream(oSheet, 0);
        String type = oSheet.type();
        return readAndClose(opt, ins, type);
    }

    public List<NutMap> readAndClose(NutMap opt, InputStream ins, String type) {
        SheetHandler sh = __check_handler(type);
        try {
            return sh.read(opt, ins);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    public void write(NutMap opt, WnObj oSheet, List<NutMap> list) {
        OutputStream ops = io.getOutputStream(oSheet, 0);
        String type = oSheet.type();
        this.writeAndClose(opt, ops, type, list);
    }

    public void writeAndClose(NutMap opt, OutputStream ops, String type, List<NutMap> list) {
        SheetHandler sh = __check_handler(type);
        try {
            sh.write(opt, ops, list);
        }
        finally {
            Streams.safeClose(ops);
        }
    }
}
