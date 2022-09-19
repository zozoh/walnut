package org.nutz.walnut.cheap.dom.docx.en;

import java.util.List;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.ooml.measure.bean.OomlMeasure;

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

    public List<CheapElement[]> grid;

}
