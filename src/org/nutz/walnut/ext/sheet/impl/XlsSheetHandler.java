package org.nutz.walnut.ext.sheet.impl;

import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

public class XlsSheetHandler extends AbstractPoiSheetHandler {

    protected Workbook createWorkbook(InputStream ins) throws IOException {
        return new HSSFWorkbook(ins);
    }

    protected Workbook createWorkbook() {
        return new HSSFWorkbook();
    }

}
