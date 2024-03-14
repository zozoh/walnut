package com.site0.walnut.cheap.dom.docx.en;

import java.util.List;

import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.ooml.measure.bean.OomlMeasure;

public class DocxTable {

    public OomlMeasure width;

    public OomlMeasure borderH;
    public OomlMeasure borderV;
    public OomlMeasure cellPaddingH;
    public OomlMeasure cellPaddingV;

    public int borderH8p;
    public int borderV8p;
    public int cellPaddingHdxa;
    public int cellPaddingVdxa;

    public int colN;

    public int[] colsW;
    public int[] colsDxas;

    public List<CheapElement[]> grid;

    /**
     * 转换列宽(PCT => DXA)
     * <p>
     * <b>!!注意</b> 本函数必须要等 <code>width</code> 以及 <code>colsW</code>
     * 设置完成后，才能调用，否则会出现不可知错误
     */
    public void convertColsWidthToDxa(String osName) {
        colsDxas = new int[colsW.length];
        for (int i = 0; i < colsW.length; i++) {
            int cellWpct = colsW[i];
            OomlMeasure cellW = OomlMeasure.PCT(cellWpct);
            OomlMeasure cellWp = cellW.toDXA(width, osName);
            colsDxas[i] = cellWp.getInt();
        }
    }

}
