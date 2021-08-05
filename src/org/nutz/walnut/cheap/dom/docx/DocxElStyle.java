package org.nutz.walnut.cheap.dom.docx;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.cheap.css.CheapStyle;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Ws;

class DocxElStyle {

    static Map<String, Integer> FONT_PTS = new HashMap<>();

    static {
        FONT_PTS.put("42pt", 84);
        FONT_PTS.put("36pt", 72);
        FONT_PTS.put("26pt", 52);
        FONT_PTS.put("24pt", 48);
        FONT_PTS.put("22pt", 44);
        FONT_PTS.put("18pt", 36);
        FONT_PTS.put("16pt", 32);
        FONT_PTS.put("15pt", 30);
        FONT_PTS.put("14pt", 28);
        FONT_PTS.put("12pt", 24);
        FONT_PTS.put("10.5pt", 21);
        FONT_PTS.put("9pt", 18);
        FONT_PTS.put("7.5pt", 15);
        FONT_PTS.put("6.5pt", 13);
        FONT_PTS.put("5.5pt", 11);
        FONT_PTS.put("5pt", 10);
    }

    boolean bold;

    boolean italic;

    boolean underline;

    String fontSize;

    String fontFamily;

    public DocxElStyle clone() {
        DocxElStyle ds = new DocxElStyle();
        ds.bold = this.bold;
        ds.italic = this.italic;
        ds.underline = this.underline;
        ds.fontSize = this.fontSize;
        ds.fontFamily = this.fontFamily;
        return ds;
    }

    boolean hasStyle() {
        return bold || italic || underline || !Ws.isBlank(fontSize) || !Ws.isBlank(fontFamily);
    }

    int getFontSizeValue() {
        Integer v = FONT_PTS.get(fontSize);
        if (null == v) {
            return -1;
        }
        return v;
    }

    void updateByElement(CheapElement el) {
        CheapStyle style = el.getStyleObj();
        // 字号(pt)
        this.fontSize = style.getString("font-size");
        // 字体
        this.fontFamily = style.getString("font-family");
        // 加粗
        if (style.isMatch("font-weight", "bold")) {
            this.bold = true;
        }
        // 斜体
        if (style.isMatch("font-style", "italic")) {
            this.italic = true;
        }
        // 下划线
        if (style.isMatch("text-decoration", "underline")) {
            this.underline = true;
        }
    }

}
