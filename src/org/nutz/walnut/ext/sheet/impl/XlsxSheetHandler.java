package org.nutz.walnut.ext.sheet.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nutz.lang.util.NutMap;

public class XlsxSheetHandler extends AbstractPoiSheetHandler {

    // private static final Log log = Logs.get();

    protected Workbook createWorkbook(InputStream ins) throws IOException {
        return new XSSFWorkbook(ins);
    }

    protected Workbook createWorkbook() {
        return new XSSFWorkbook();
    }

    @Override
    protected List<SheetImage> exportImages(Workbook _wb, List<NutMap> list, NutMap conf) {
        List<SheetImage> images = new ArrayList<SheetImage>();
        XSSFWorkbook wb = (XSSFWorkbook) _wb;
        int sheetCount = wb.getNumberOfSheets();
        int sheetIndex = conf.getInt("sheetIndex", -1);
        int rowOffset = conf.getInt("rowOffset", 0);
        int colOffset = conf.getInt("colOffset", 0);
        String sheetName = conf.getString("sheetName");
        Set<Integer> rows = new HashSet<Integer>();
        for (NutMap re : list) {
            if (re.containsKey("rowIndex")) {
                rows.add(re.getInt("rowIndex"));
            }
        }
        for (int i = 0; i < sheetCount; i++) {
            XSSFSheet sheet = wb.getSheetAt(i);
            if (sheetName != null) {
                if (!sheet.getSheetName().equals(sheetName))
                    continue;
            } else if (sheetIndex > -1) {
                if (i != sheetIndex)
                    continue;
            }
            XSSFDrawing drawing = sheet.getDrawingPatriarch();
            List<XSSFShape> shapes = drawing.getShapes();
            for (XSSFShape shape : shapes) {
                if (shape instanceof XSSFPicture) {
                    XSSFPicture hssfPicture = (XSSFPicture) shape;
                    XSSFPictureData data = hssfPicture.getPictureData();
                    // byte [] picData = data.getData();
                    int rowIndex = hssfPicture.getClientAnchor().getRow2();
                    int colIndex = hssfPicture.getClientAnchor().getCol2();
                    // System.out.println(">>>> " + rowIndex + " -- " +
                    // colIndex);
                    if (rowIndex < rowOffset || colIndex < colOffset) {
                        continue;
                    }
                    if (!rows.isEmpty() && !rows.contains(rowIndex)) {
                        continue;
                    }
                    SheetImage image = new SheetImage();
                    image.data = data.getData();
                    image.row = rowIndex;
                    image.col = colIndex;
                    image.type = data.getPictureType() == org.apache.poi.ss.usermodel.Workbook.PICTURE_TYPE_PNG ? 1
                                                                                                                : 0;
                    images.add(image);
                    // log.infof("row=%d col=%d", rowIndex, colIndex);
                }
            }
        }
        return images;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void addImage(Workbook wb, Drawing patriarch, byte[] image, int row, int col) {
        int P = wb.addPicture(image, XSSFWorkbook.PICTURE_TYPE_JPEG);
        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, col, row, col + 1, row + 1);
        anchor.setAnchorType(AnchorType.MOVE_AND_RESIZE);
        patriarch.createPicture(anchor, P);
    }
}
