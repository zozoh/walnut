package org.nutz.walnut.ext.sheet.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.sheet.SheetHandler;

public class CsvSheetHandler implements SheetHandler {

    @Override
    public List<NutMap> read(NutMap opt, InputStream ins) {
        return null;
    }

    @Override
    public void write(NutMap opt, OutputStream ops, List<NutMap> list) {}

}
