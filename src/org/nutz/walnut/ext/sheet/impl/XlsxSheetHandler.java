package org.nutz.walnut.ext.sheet.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XlsxSheetHandler extends AbstractPoiSheetHandler {

    protected Workbook createWorkbook(InputStream ins) throws IOException {
        return new HSSFWorkbook(ins);
    }

    protected Workbook createWorkbook() {
        return new XSSFWorkbook();
    }

}
