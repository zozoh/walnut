package com.site0.walnut.ext.biz.abb.hdl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmHdl;

public abstract class abb_abstract_ckimp implements JvmHdl {

    protected static final Log log = Logs.getLog(abb_abstract_ckimp.class);

    @SuppressWarnings("deprecation")
    protected Object __get_cell_value(Cell cell) {
        if (cell == null)
            return null;
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
