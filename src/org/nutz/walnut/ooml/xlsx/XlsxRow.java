package org.nutz.walnut.ooml.xlsx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Ws;

/**
 * 相关文档：<br>
 * <a href=
 * "https://docs.microsoft.com/en-us/dotnet/api/documentformat.openxml.spreadsheet.row?view=openxml-2.8.1">row</a>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class XlsxRow {

    protected List<XlsxCell> cells;

    private int rowIndex;

    public XlsxRow(XlsxWorkbook workbook, CheapElement el) {
        this.rowIndex = el.attrInt("r");
        List<CheapElement> list = el.findElements(e -> e.isTagName("c"));
        this.cells = new ArrayList<>(list.size());
        for (CheapElement li : list) {
            XlsxCell cell = new XlsxCell(workbook, li);
            this.cells.add(cell);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(rowIndex).append(']');
        if (null == cells) {
            sb.append(" ~No Cells~ ");
        } else {
            for (XlsxCell cell : cells) {
                sb.append(cell.toString());
                sb.append(" | ");
            }
        }
        return sb.toString();
    }

    public NutBean toBean(Map<String, String> header) {
        NutMap bean = new NutMap();
        for (XlsxCell cell : cells) {
            Matcher m = XlsxSheet._K_M.matcher(cell.getReference());
            if (m.find()) {
                String key = m.group(1);
                Object val;
                if (cell.hasMedia()) {
                    val = cell.getMedia();
                } else {
                    val = cell.getValue();
                    if (null != val && val instanceof String) {
                        val = val.toString().trim();
                    }
                }
                String k2 = header.get(key);
                if (!Ws.isBlank(k2)) {
                    bean.put(k2, val);
                }
            }
        }
        return bean;
    }

    public List<XlsxCell> getCells() {
        return cells;
    }

    public int getRowIndex() {
        return rowIndex;
    }

}
