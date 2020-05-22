package org.nutz.walnut.ext.sheet.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nutz.lang.util.NutMap;

public class XlsxSheetHandler extends AbstractPoiSheetHandler {
	
	//private static final Log log = Logs.get();

	protected Workbook createWorkbook(InputStream ins) throws IOException {
		return new XSSFWorkbook(ins);
	}

	protected Workbook createWorkbook() {
		return new XSSFWorkbook();
	}

	@Override
    protected List<SheetImage> exportImages(Workbook _wb, NutMap conf) {
		List<SheetImage> images = new ArrayList<SheetImage>();
    	XSSFWorkbook wb = (XSSFWorkbook)_wb;
    	int sheetCount = wb.getNumberOfSheets();
    	int sheetIndex = conf.getInt("sheetIndex", -1);
    	int rowOffset = conf.getInt("rowOffset", 0);
    	int colOffset = conf.getInt("colOffset", 0);
    	String sheetName = conf.getString("sheetName");
    	for (int i = 0; i < sheetCount; i++) {
			XSSFSheet sheet = wb.getSheetAt(i);
    		if (sheetName != null) {
    			if (!sheet.getSheetName().equals(sheetName))
    				continue;
    		}
    		else if (sheetIndex > -1) {
    			if (i != sheetIndex)
    				continue;
    		}
			XSSFDrawing drawing = sheet.getDrawingPatriarch();
			List<XSSFShape> shapes = drawing.getShapes();
            for (XSSFShape shape : shapes) {
            	if (shape instanceof XSSFPicture) {
            		XSSFPicture hssfPicture = (XSSFPicture) shape;
            		XSSFPictureData data = hssfPicture.getPictureData();
                    //byte [] picData = data.getData();
                    int rowIndex = hssfPicture.getClientAnchor().getRow1();
                    int colIndex = hssfPicture.getClientAnchor().getCol1();
                    if (rowIndex < rowOffset || colIndex < colOffset) {
                    	continue;
                    }
                    SheetImage image = new SheetImage();
                    image.data = data.getData();
                    image.row = rowIndex;
                    image.col = colIndex;
                    image.type = data.getPictureType() == org.apache.poi.ss.usermodel.Workbook.PICTURE_TYPE_PNG ? 1 : 0;
                    images.add(image);
                    //log.infof("row=%d col=%d", rowIndex, colIndex);
            	}
            }
		}
    	return images;
    }
	
	@Override
	protected void addImage(Workbook wb, BufferedImage image, int row, int col) {
		// TODO Auto-generated method stub
		
	}
}
