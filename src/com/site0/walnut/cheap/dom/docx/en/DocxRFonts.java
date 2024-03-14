package com.site0.walnut.cheap.dom.docx.en;

import com.site0.walnut.cheap.dom.CheapElement;

public class DocxRFonts {

    public static DocxRFonts genWingdings() {
        DocxRFonts rf = new DocxRFonts();
        rf.setAscii("Wingdings");
        rf.sethAnsi("Wingdings");
        rf.setHint("default");
        return rf;
    }

    private String ascii;

    private String hAnsi;

    private String hint;

    public DocxRFonts() {}

    public DocxRFonts(String ascii, String hAnsi, String hint) {
        this.ascii = ascii;
        this.hAnsi = hAnsi;
        this.hint = hint;
    }

    public CheapElement toElement() {
        CheapElement el = new CheapElement("w:rFonts");
        el.setClosed(true);
        el.attr("w:ascii", ascii);
        el.attr("w:hAnsi", hAnsi);
        el.attr("w:hint", hint);
        return el;
    }

    public String toString() {
        CheapElement el = this.toElement();
        return el.toString();
    }

    public DocxRFonts clone() {
        DocxRFonts re = new DocxRFonts();
        re.ascii = ascii;
        re.hAnsi = hAnsi;
        re.hint = hint;
        return re;
    }

    public boolean equals(Object ta) {
        if (null == ta) {
            return false;
        }
        if (!(ta instanceof DocxRFonts)) {
            return false;
        }
        DocxRFonts rf = (DocxRFonts) ta;
        if (null != this.ascii) {
            if (!this.ascii.equals(rf.ascii)) {
                return false;
            }
        } else if (null != rf.ascii) {
            return false;
        }

        if (null != this.hAnsi) {
            if (!this.hAnsi.equals(rf.hAnsi)) {
                return false;
            }
        } else if (null != rf.hAnsi) {
            return false;
        }

        if (null != this.hint) {
            if (!this.hint.equals(rf.hint)) {
                return false;
            }
        } else if (null != rf.hint) {
            return false;
        }

        return true;
    }

    public String getAscii() {
        return ascii;
    }

    public void setAscii(String ascii) {
        this.ascii = ascii;
    }

    public String gethAnsi() {
        return hAnsi;
    }

    public void sethAnsi(String hAnsi) {
        this.hAnsi = hAnsi;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

}
