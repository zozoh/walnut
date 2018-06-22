package org.nutz.walnut.ext.sheet.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.nutz.castor.Castors;
import org.nutz.lang.Mirror;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.sheet.SheetHandler;

public abstract class AbstractSheetHandler implements SheetHandler {

    public AbstractSheetHandler() {
        super();
    }

    protected abstract Workbook createWorkbook(InputStream ins) throws IOException;

    protected abstract Workbook createWorkbook();

    @Override
    public List<NutMap> read(InputStream ins, NutMap conf) {
        List<NutMap> list = new LinkedList<>();
        Workbook wb = null;
        try {
            // 从输入流创建工作表
            wb = createWorkbook(ins);

            // 从工作表读取数据
            readFromSheet(wb, list, conf);

        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        finally {
            Streams.safeClose(wb);
        }

        return list;
    }

    @Override
    public void write(OutputStream ops, List<NutMap> list, NutMap conf) {
        Workbook wb = null;
        try {
            // 加载工作表
            wb = new HSSFWorkbook();

            // 从工作表读取数据
            this.writeToSheet(wb, list, conf);

            // 写入输出流
            wb.write(ops);

        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        finally {
            Streams.safeClose(wb);
        }
    }

    protected void writeToSheet(Workbook wb, List<NutMap> list, NutMap conf) {
        // 分析参数
        boolean noheader = conf.getBoolean("noheader");
        int rowOffset = conf.getInt("rowOffset", 0);
        int colOffset = conf.getInt("colOffset", 0);
        // 读取工作表
        Sheet sheet = __get_sheet(wb, conf);
        // 木有的话，就创建一个
        if (sheet == null) {
            sheet = wb.createSheet(conf.getString("sheetName", "sheet1"));
        }
        // 准备迭代器
        Iterator<NutMap> it = list.iterator();
        if (!it.hasNext())
            return;
        // ................................
        // 用第一个对象的键作为标题栏
        String[] keys = null; // 准备用第一个对象的 Key 来归纳列标题
        if (!noheader) {
            NutMap first = list.get(0);
            Row row = sheet.createRow(rowOffset++);
            keys = first.keySet().toArray(new String[first.size()]);
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                Cell cell = row.createCell(colOffset + i);
                cell.setCellType(CellType.STRING);
                cell.setCellValue(key);
            }
        }
        // ................................
        // 输出后续数据
        for (NutMap obj : list) {
            // 如果没有初始化标题的 keys，那么搞一下
            if (null == keys) {
                keys = obj.keySet().toArray(new String[obj.size()]);
            }
            // 来，创建数据行吧
            Row row = sheet.createRow(rowOffset++);
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                Object val = obj.get(key);
                Cell cell = row.createCell(colOffset + i);
                this.__set_cell_val(cell, val);
            }
        }

    }

    private Sheet __get_sheet(Workbook wb, NutMap conf) {
        Sheet sheet = null;

        String sheetName = conf.getString("sheetName");
        // 优先用工作簿的名称
        if (!Strings.isBlank(sheetName)) {
            sheet = wb.getSheet(sheetName);
        }
        // 没名字用下标
        else {
            int sheetIndex = conf.getInt("sheetIndex", 0);
            // 如果没有工作表，那么就返回 null，这个时候传什么下标进去都会是 OutOfRange 的
            try {
                sheet = wb.getSheetAt(sheetIndex);
            }
            catch (Exception e) {}
        }
        return sheet;
    }

    protected void readFromSheet(Workbook wb, List<NutMap> list, NutMap conf) {
        // 读取工作表
        Sheet sheet = __get_sheet(wb, conf);
        if (null == sheet)
            return;
        // ................................
        // 分析工作簿
        Iterator<Row> itRow = sheet.rowIterator();
        // 第一行作为 Key
        if (!itRow.hasNext()) {
            return;
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
                Object val = __get_cell_value(cell);

                // 计入
                obj.put(key, val);
            }
            // 计入结果
            list.add(obj);
        }
    }

    private void __set_cell_val(Cell cell, Object val) {
        // 空的
        if (null == val) {
            cell.setCellType(CellType.BLANK);
            return;
        }
        // 开始判断吧
        Mirror<?> mi = Mirror.me(val);

        // 数字
        if (mi.isNumber()) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue(Castors.me().castTo(val, Double.class));
        }
        // 日期
        else if (mi.isDateTimeLike()) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue(Castors.me().castTo(val, Date.class));
        }
        // 布尔
        else if (mi.isBoolean()) {
            cell.setCellType(CellType.BOOLEAN);
            cell.setCellValue(Castors.me().castTo(val, Boolean.class));
        }
        // 公式
        else if (mi.isStringLike() && val.toString().startsWith("=")) {
            cell.setCellType(CellType.FORMULA);
            cell.setCellValue(val.toString());
        }
        // 其他的就是字符串咯
        else {
            cell.setCellType(CellType.STRING);
            cell.setCellValue(Castors.me().castToString(val));
        }
    }

    @SuppressWarnings("deprecation")
    private Object __get_cell_value(Cell cell) {
        CellType cellType = cell.getCellTypeEnum();
        switch (cellType) {
        case NUMERIC:
            double n = cell.getNumericCellValue();
            long ni = (long) n;
            if (ni == n)
                return ni;
            return n;
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
        default:
            break;
        }
        throw Er.create("e.sheet.xls.unknownCellType", cellType);
    }

}