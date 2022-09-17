package org.nutz.walnut.ooml;

import static org.nutz.walnut.ooml.measure.convertor.DpiConvertor.DPI_MAC;
import static org.nutz.walnut.ooml.measure.convertor.DpiConvertor.DPI_WIN;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.xml.CheapXmlParsing;
import org.nutz.walnut.ooml.measure.*;
import org.nutz.walnut.ooml.measure.convertor.CmEmusConvertor;
import org.nutz.walnut.ooml.measure.convertor.CmInchConvertor;
import org.nutz.walnut.ooml.measure.convertor.ComboMeasureConvertor;
import org.nutz.walnut.ooml.measure.convertor.DxaPtConvertor;
import org.nutz.walnut.ooml.measure.convertor.EmusCmConvertor;
import org.nutz.walnut.ooml.measure.convertor.EmusInchConvertor;
import org.nutz.walnut.ooml.measure.convertor.InchCmConvertor;
import org.nutz.walnut.ooml.measure.convertor.InchEmusConvertor;
import org.nutz.walnut.ooml.measure.convertor.InchPtConvertor;
import org.nutz.walnut.ooml.measure.convertor.MeasureConvertor;
import org.nutz.walnut.ooml.measure.convertor.PtDxaConvertor;
import org.nutz.walnut.ooml.measure.convertor.PtInchConvertor;
import org.nutz.walnut.ooml.measure.convertor.PtPxConvertor;
import org.nutz.walnut.ooml.measure.convertor.PxPtConvertor;

public class Oomls {

    private static final Map<String, MeasureConvertor> convertors = new HashMap<>();

    static {
        //
        // 直接转换器
        //
        convertors.put("win_pt_px", new PtPxConvertor(DPI_WIN));
        convertors.put("win_px_pt", new PxPtConvertor(DPI_WIN));
        convertors.put("mac_pt_px", new PtPxConvertor(DPI_MAC));
        convertors.put("mac_px_pt", new PxPtConvertor(DPI_MAC));
        // 二十分点
        convertors.put("dxa_pt", new DxaPtConvertor());
        // 磅
        convertors.put("pt_dxa", new PtDxaConvertor());
        convertors.put("pt_in", new PtInchConvertor());
        // 英寸
        convertors.put("in_pt", new InchPtConvertor());
        convertors.put("in_cm", new InchCmConvertor());
        convertors.put("in_emus", new InchEmusConvertor());
        // 厘米
        convertors.put("cm_in", new CmInchConvertor());
        convertors.put("cm_emus", new CmEmusConvertor());
        // 英米单位
        convertors.put("emus_in", new EmusInchConvertor());
        convertors.put("emus_cm", new EmusCmConvertor());
        //
        // 复合转换器: 组合
        //
        // 磅
        convertors.put("pt_cm", CMC("pt_in", "in_cm"));
        convertors.put("pt_emus", CMC("pt_in", "in_emus"));
        // 二十分点
        convertors.put("dxa_in", CMC("dxa_pt", "pt_in"));
        convertors.put("dxa_cm", CMC("dxa_pt", "pt_cm"));
        convertors.put("dxa_emus", CMC("dxa_pt", "pt_emus"));
        // 英寸
        convertors.put("in_dxa", CMC("in_pt", "pt_dxa"));
        // 厘米
        convertors.put("cm_pt", CMC("cm_in", "in_pt"));
        convertors.put("cm_dxa", CMC("cm_pt", "pt_dxa"));
        // 英米单位
        convertors.put("emus_pt", CMC("emus_in", "in_pt"));
        convertors.put("emus_dxa", CMC("emus_in", "in_dxa"));
        //
        // 复合转换器: Windows
        //
        convertors.put("win_px_emus", CMC("win_px_pt", "pt_emus"));
        convertors.put("win_px_dxa", CMC("win_px_pt", "pt_dxa"));
        convertors.put("win_px_in", CMC("win_px_pt", "pt_in"));
        convertors.put("win_px_cm", CMC("win_px_pt", "pt_cm"));

        convertors.put("win_emus_px", CMC("emus_pt", "win_pt_px"));
        convertors.put("win_cm_px", CMC("cm_pt", "win_pt_px"));
        convertors.put("win_dxa_px", CMC("dxa_pt", "win_pt_px"));
        convertors.put("win_in_px", CMC("in_pt", "win_pt_px"));
        //
        // 复合转换器: Mac
        //
        convertors.put("mac_px_emus", CMC("mac_px_pt", "pt_emus"));
        convertors.put("mac_px_dxa", CMC("mac_px_pt", "pt_dxa"));
        convertors.put("mac_px_in", CMC("mac_px_pt", "pt_in"));
        convertors.put("mac_px_cm", CMC("mac_px_pt", "pt_cm"));

        convertors.put("mac_emus_px", CMC("emus_pt", "mac_pt_px"));
        convertors.put("mac_cm_px", CMC("cm_pt", "mac_pt_px"));
        convertors.put("mac_dxa_px", CMC("dxa_pt", "mac_pt_px"));
        convertors.put("mac_in_px", CMC("in_pt", "mac_pt_px"));
    }

    private static ComboMeasureConvertor CMC(String... names) {
        return new ComboMeasureConvertor(names);
    }

    public static MeasureConvertor getMeasureConvertor(String name) {
        return convertors.get(name);
    }

    public static double convertMeasure(String name, double input) {
        MeasureConvertor mc = convertors.get(name);
        return mc.translate(input);
    }

    public static int convertMeasureI(String name, int input) {
        MeasureConvertor mc = convertors.get(name);
        double v = mc.translate((double) input);
        return (int) Math.round(v);
    }

    public static long convertMeasureL(String name, long input) {
        MeasureConvertor mc = convertors.get(name);
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
