package org.nutz.walnut.ext.sheet.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.sheet.SheetHandler;

public class XlsSheetHandler implements SheetHandler {

    @Override
    public List<NutMap> read(InputStream ins, NutMap conf) {
        List<NutMap> list = new LinkedList<>();
        Workbook wb = null;
        // 读取工作簿
        try {
            wb = new HSSFWorkbook(ins);
            Sheet sheet;

            String sheetName = conf.getString("sheetName");
            // 优先用工作簿的名称
            if (!Strings.isBlank(sheetName)) {
                sheet = wb.getSheet(sheetName);
            }
            // 没名字用下标
            else {
                int sheetIndex = conf.getInt("sheetIndex", 0);
                sheet = wb.getSheetAt(sheetIndex);
            }

            // ................................
            // 分析工作簿
            Iterator<Row> itRow = sheet.rowIterator();
            // 第一行作为 Key
            if (!itRow.hasNext()) {
                return list;
            }
            List<String> keyList = new LinkedList<>();
            Row row = itRow.next();
            Iterator<Cell> itCell = row.cellIterator();
            while (itCell.hasNext()) {
                Cell cell = itCell.next();
                String key = cell.getStringCellValue();
                keyList.add(key);
            }
            String[] keys = keyList.toArray(new String[keyList.size()]);

            // 读取后续的数据
            while (itRow.hasNext()) {
                row = itRow.next();
                // 准备对象
                NutMap obj = new NutMap();
                // 分析行
                itCell = row.cellIterator();
                int i = 0;
                while (itCell.hasNext()) {
                    // 确保没有超出范围
                    if (i >= keys.length)
                        break;
                    // 得到键
                    String key = keys[i++];

                    // 得到值
                    Cell cell = itCell.next();
                    Object val = __cell_value(cell);

                    // 计入
                    obj.put(key, val);
                }
                // 计入结果
                list.add(obj);
            }

        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        finally {
            Streams.safeClose(wb);
        }

        return list;
    }

    private Object __cell_value(Cell cell) {
        CellType cellType = cell.getCellTypeEnum();
        switch (cellType) {
        case NUMERIC:
            return cell.getNumericCellValue();
        case STRING:
            return cell.getStringCellValue();
        case FORMULA:
            return cell.getCellFormula();
        case BLANK:
            return null;
        case BOOLEAN:
            return cell.getBooleanCellValue();
        case ERROR:
            return cell.getErrorCellValue();
        }
        throw Er.create("e.sheet.xls.unknownCellType", cellType);
    }

    @Override
    public void write(OutputStream ops, List<NutMap> list, NutMap conf) {}

}
