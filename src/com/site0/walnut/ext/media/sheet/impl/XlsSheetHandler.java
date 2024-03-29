package com.site0.walnut.ext.media.sheet.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public class XlsSheetHandler extends AbstractPoiSheetHandler {

    protected Workbook createWorkbook(InputStream ins) throws IOException {
        return new HSSFWorkbook(ins);
    }

    protected Workbook createWorkbook() {
        return new HSSFWorkbook();
    }

    @Override
    protected List<SheetImage> exportImages(Workbook wb, List<NutBean> list, NutMap conf) {
        return null;
    }

}
