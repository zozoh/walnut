package com.site0.walnut.cheap.dom.docx.num;

import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.dom.docx.Docxs;
import com.site0.walnut.cheap.dom.docx.en.DocxInd;
import com.site0.walnut.cheap.dom.docx.en.DocxRFonts;
import com.site0.walnut.util.Wlang;

public class DocxNumLvl {

    private int value;

    private int start;

    private String numFmt;

    private String text;

    private String jc;

    private DocxInd indent;

    private DocxRFonts fonts;

    public DocxNumLvl clone() {
        DocxNumLvl re = new DocxNumLvl();
        re.value = this.value;
        re.start = this.start;
        re.numFmt = this.numFmt;
        re.text = this.text;
        re.jc = this.jc;
        re.indent = null == this.indent ? null : this.indent.clone();
        re.fonts = null == this.fonts ? null : this.fonts.clone();
        return re;
    }

    public boolean equals(Object ta) {
        if (null == ta) {
            return false;
        }
        if (!(ta instanceof DocxNumLvl)) {
            return false;
        }
        DocxNumLvl lvl = (DocxNumLvl) ta;

        if (this.value != lvl.value) {
            return false;
        }

        if (this.start != lvl.start) {
            return false;
        }

        if (!Wlang.isEqual(numFmt, lvl.numFmt)) {
            return false;
        }

        if (!Wlang.isEqual(text, lvl.text)) {
            return false;
        }

        if (!Wlang.isEqual(jc, lvl.jc)) {
            return false;
        }

        if (!Wlang.isEqual(indent, lvl.indent)) {
            return false;
        }

        if (!Wlang.isEqual(fonts, lvl.fonts)) {
            return false;
        }

        return true;
    }

    /**
     * 返回一个 XML 元素节点
     * 
     * <pre>
     * <w:lvl w:ilvl="0" w:tplc="0409000F">
     *    <w:start w:val="1"/>
     *    <w:numFmt w:val="decimal"/>
     *    <w:lvlText w:val="%1."/>
     *    <w:lvlJc w:val="left"/>
     *    <w:pPr>
     *       <w:ind w:left="420" w:hanging="420"/>
     *    </w:pPr>
     *    <w:rPr>
     *       <w:rFonts w:ascii="Wingdings" w:hAnsi="Wingdings" w:hint=
    "default"/>
     *    </w:rPr>
     * </w:lvl>
     * </pre>
     * 
     * @return 一个 XML 元素节点
     */
    public CheapElement toElement() {
        CheapElement el = new CheapElement("w:lvl");
        el.attr("w:ilvl", this.value);

        CheapElement sub = Docxs.genElVal("w:start", this.start);
        el.append(sub);

        if (null != this.numFmt) {
            sub = Docxs.genElVal("w:numFmt", this.numFmt);
            el.append(sub);
        }

        if (null != this.text) {
            sub = Docxs.genElVal("w:lvlText", this.text);
            el.append(sub);
        }

        if (null != this.jc) {
            sub = Docxs.genElVal("w:lvlJc", this.jc);
            el.append(sub);
        }

        if (null != this.indent) {
            CheapElement pPr = new CheapElement("w:pPr");
            sub = this.indent.toElement();
            pPr.append(sub);
            el.append(pPr);
        }

        if (null != this.fonts) {
            CheapElement rPr = new CheapElement("w:rPr");
            sub = this.fonts.toElement();
            rPr.append(sub);
            el.append(rPr);
        }

        return el;
    }

    public String toString() {
        CheapElement el = this.toElement();
        return el.toString();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public String getNumFmt() {
        return numFmt;
    }

    public void setNumFmt(String numFmt) {
        this.numFmt = numFmt;
    }

    public String getText() {
        return text;
    }

    public void setText(String lvlText) {
        this.text = lvlText;
    }

    public String getJc() {
        return jc;
    }

    public void setJc(String lvlJc) {
        this.jc = lvlJc;
    }

    public DocxInd getIndent() {
        return indent;
    }

    public void setIndent(DocxInd indent) {
        this.indent = indent;
    }

    public DocxRFonts getFonts() {
        return fonts;
    }

    public void setFonts(DocxRFonts fonts) {
        this.fonts = fonts;
    }

}
