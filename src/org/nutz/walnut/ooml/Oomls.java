package org.nutz.walnut.ooml;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.xml.CheapXmlParsing;
import org.nutz.walnut.ooml.measure.*;
import static org.nutz.walnut.ooml.measure.DpiConvertor.DPI_WIN;
import static org.nutz.walnut.ooml.measure.DpiConvertor.DPI_MAC;

public class Oomls {

    private static final Map<String, MeasureConvertor> measures = new HashMap<>();

    static {
        //
        // 直接转换器
        //
        measures.put("win_pt_px", new PtPxConvertor(DPI_WIN));
        measures.put("win_px_pt", new PxPtConvertor(DPI_WIN));
        measures.put("mac_pt_px", new PtPxConvertor(DPI_MAC));
        measures.put("mac_px_pt", new PxPtConvertor(DPI_MAC));
        measures.put("dxa_pt", new DxaPtConvertor());
        measures.put("pt_dxa", new PtDxaConvertor());
        measures.put("in_pt", new InchPtConvertor());
        measures.put("pt_in", new PtInchConvertor());
        measures.put("in_cm", new InchCmConvertor());
        measures.put("cm_in", new CmInchConvertor());
        measures.put("in_emus", new InchEmusConvertor());
        measures.put("emus_in", new EmusInchConvertor());
        measures.put("cm_emus", new CmEmusConvertor());
        measures.put("emus_cm", new EmusCmConvertor());
        //
        // 复合转换器
        //
        measures.put("win_px_emus", new ComboMeasureConvertor("win_px_pt", "pt_in", "in_emus"));
        measures.put("win_px_dxa", new ComboMeasureConvertor("win_px_pt", "pt_dxa"));
        measures.put("win_px_in", new ComboMeasureConvertor("win_px_pt", "pt_in"));
        measures.put("win_px_cm", new ComboMeasureConvertor("win_px_pt", "pt_in", "in_cm"));
        measures.put("mac_px_emus", new ComboMeasureConvertor("mac_px_pt", "pt_in", "in_emus"));
        measures.put("mac_px_dxa", new ComboMeasureConvertor("mac_px_pt", "pt_dxa"));
        measures.put("mac_px_in", new ComboMeasureConvertor("mac_px_pt", "pt_in"));
        measures.put("mac_px_cm", new ComboMeasureConvertor("mac_px_pt", "pt_in", "in_cm"));
    }

    public static MeasureConvertor getMeasureConvertor(String name) {
        return measures.get(name);
    }

    public static double convertMeasure(String name, double input) {
        MeasureConvertor mc = measures.get(name);
        return mc.translate(input);
    }

    public static int convertMeasureI(String name, int input) {
        MeasureConvertor mc = measures.get(name);
        double v = mc.translate((double) input);
        return (int) Math.round(v);
    }

    public static long convertMeasureL(String name, long input) {
        MeasureConvertor mc = measures.get(name);
        double v = mc.translate((double) input);
        return Math.round(v);
    }

    public static CheapDocument parseEntryAsXml(OomlEntry en) {
        String str = en.getContentStr();
        CheapDocument doc = new CheapDocument(null);
        CheapXmlParsing ing = new CheapXmlParsing(doc);
        ing.parseDoc(str);
        return doc;
    }

}
