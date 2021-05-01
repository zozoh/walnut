package org.nutz.walnut.ext.media.sheet.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.nutz.castor.Castors;
import org.nutz.lang.Mirror;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.media.sheet.SheetImageHolder;

public abstract class AbstractPoiSheetHandler extends AbstractSheetHandler {

    protected Workbook tmpl;

    public void setTmpl(InputStream ins) throws IOException {
        tmpl = createWorkbook(ins);
    }

    public AbstractPoiSheetHandler() {
        super();
    }

    protected abstract Workbook createWorkbook(InputStream ins) throws IOException;

    protected abstract Workbook createWorkbook();

    protected abstract List<SheetImage> exportImages(Workbook wb, List<NutMap> list, NutMap conf);

    @Override
    public SheetResult read(InputStream ins, NutMap conf) {
        SheetResult result = new SheetResult();
        List<NutMap> list = new LinkedList<>();
        Workbook wb = null;
        try {
            // 从输入流创建工作表
            wb = createWorkbook(ins);

            // 从工作表读取数据
            readFromSheet(wb, list, conf);

            if (conf.has("images")) {
                result.images = exportImages(wb, list, conf);
            }
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        finally {
            Streams.safeClose(wb);
        }
        result.list = list;
        return result;
    }

    @Override
    public void write(OutputStream ops, List<NutMap> list, NutMap conf) {
        Workbook wb = null;
        try {
            // 加载工作表
            if (tmpl != null) {
                wb = tmpl;
            } else {
                wb = new HSSFWorkbook();
            }

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

    @SuppressWarnings({"deprecation", "rawtypes"})
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

        Drawing drawing = sheet.createDrawingPatriarch();
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
        int i = 1;
        int len = list.size();
        for (NutMap obj : list) {
            // 日志
            this._on_process(i++, len, obj);

            // 如果没有初始化标题的 keys，那么搞一下
            if (null == keys) {
                keys = obj.keySet().toArray(new String[obj.size()]);
            }
            // 来，创建数据行吧
            int _row = rowOffset++;
            Row row = sheet.getRow(_row);
            if (row == null) {
                row = sheet.createRow(_row);
            }
            for (int col = 0; col < keys.length; col++) {
                String key = keys[col];
                Object val = obj.get(key);
                Cell cell = row.getCell(colOffset + col);
                if (cell == null)
                    cell = row.createCell(colOffset + col);
                this.__set_cell_val(wb, drawing, cell, val);
            }
        }
        // 有没有额外数据呀
        if (conf.has("exts")) {
            List<NutMap> exts = conf.getList("exts", NutMap.class);
            for (NutMap ext : exts) {
                int ext_col_index = ext.getInt("col");
                int ext_row_index = ext.getInt("row");
                String value = ext.getString("val");
                Row ext_row = sheet.getRow(ext_row_index);
                if (ext_row == null) {
                    ext_row = sheet.createRow(ext_row_index);
                }
                Cell ext_cell = ext_row.getCell(ext_col_index);
                if (ext_cell == null) {
                    ext_cell = ext_row.createCell(ext_col_index);
                }
                this.__set_cell_val(wb, drawing, ext_cell, value);
            }
        }

        // 结束日志
        this._on_end(len);

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
        // 是否自动添加行号
        boolean addRowIndex = conf.getBoolean("addRowIndex");
        NutMap matcher = conf.getAs("matcher", NutMap.class);

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

        // 跳过空行
        while (null != row && __is_empty_row(row)) {
            row = itRow.next();
        }

        // 根本啥都木有
        if (null == row) {
            return;
        }

        int i = 0;
        short lc = row.getLastCellNum();
        for (i = 0; i <= lc; i++) {
            Cell cell = row.getCell(i, MissingCellPolicy.CREATE_NULL_AS_BLANK);
            String key = cell.getStringCellValue();
            keyList.add(key);
        }
        String[] keys = keyList.toArray(new String[keyList.size()]);

        // 读取后续的数据
        while (itRow.hasNext()) {
            row = itRow.next();

            // 如果是空行，则跳过
            if (__is_empty_row(row))
                continue;

            // 准备对象
            NutMap obj = new NutMap();
            // 分析行
            lc = row.getLastCellNum();
            for (i = 0; i <= lc; i++) {
                Cell cell = row.getCell(i, MissingCellPolicy.CREATE_NULL_AS_BLANK);

                // 确保没有超出范围
                if (i >= keys.length)
                    break;
                // 得到键
                String key = Strings.trim(keys[i]);

                if (Strings.isBlank(key))
                    continue;

                // 得到值
                Object val = __get_cell_value(cell);
                if (null != val && val instanceof CharSequence) {
                    val = Strings.trim(val.toString());
                }

                // 计入
                obj.put(key, val);
            }
            if (addRowIndex) {
                obj.put("rowIndex", row.getRowNum());
            }
            // 原始数据过来
            if (matcher != null) {
                boolean flag = true;
                for (Map.Entry<String, Object> en : matcher.entrySet()) {
                    Object tmpval = obj.get(en.getKey());
                    if (tmpval == null && en.getValue() != null) {
                        flag = false;
                        break;
                    }
                    if (!tmpval.equals(en.getValue())) {
                        flag = false;
                        break;
                    }
                }
                if (!flag) {
                    continue; // 跳过当前记录
                }
            }

            // 计入结果
            list.add(obj);
        }
    }

    private boolean __is_empty_row(Row row) {
        Iterator<Cell> it = row.cellIterator();
        while (it.hasNext()) {
            Cell cell = it.next();
            Object val = __get_cell_value(cell);
            if (null != val)
                return false;
        }
        return true;
        // return !row.cellIterator().hasNext();
    }

    @SuppressWarnings({"rawtypes", "deprecation"})
    private void __set_cell_val(Workbook wb, Drawing drawing, Cell cell, Object val) {
        // 空的
        if (null == val) {
            cell.setCellType(CellType.BLANK);
            return;
        }
        // 图片
        if (val instanceof SheetImageHolder) {
            // TODO 改成可配置的
            cell.getRow().setHeight((short) 1500);
            addImage(wb,
                     drawing,
                     ((SheetImageHolder) val).getImage(400, 400),
                     cell.getRowIndex(),
                     cell.getColumnIndex());
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
            String str = Castors.me().castToString(val);
            cell.setCellValue(str);
            if (str.contains("\n") || str.contains("\r")) {
                CellStyle cs = cell.getCellStyle();
                cs.setWrapText(true);
                cell.setCellStyle(cs);
                cell.setCellValue(str);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    protected void addImage(Workbook wb, Drawing patriarch, byte[] image, int row, int col) {
        // 子类自行搞定
    }

    @SuppressWarnings("deprecation")
    private Object __get_cell_value(Cell cell) {
        CellType cellType = cell.getCellTypeEnum();
        switch (cellType) {
        case NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            }
            double n = cell.getNumericCellValue();
            long ni = (long) n;
            if (ni == n)
                return ni;
            return n;
        case STRING:
            return Strings.trim(cell.getStringCellValue());
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