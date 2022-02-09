package org.nutz.walnut.cheap.dom.docx.en;

import org.nutz.walnut.cheap.dom.CheapElement;

public class DocxInd {

    private int left;

    private int hanging;

    public DocxInd() {}

    public DocxInd(int left, int hanging) {
        this.left = left;
        this.hanging = hanging;
    }

    public DocxInd clone() {
        DocxInd re = new DocxInd();
        re.left = this.left;
        re.hanging = this.hanging;
        return re;
    }

    public boolean equals(Object ta) {
        if (null != ta && (ta instanceof DocxInd)) {
            DocxInd ind = (DocxInd) ta;
            return this.left == ind.left && this.hanging == ind.hanging;
        }
        return false;
    }

    public CheapElement toElement() {
        CheapElement el = new CheapElement("w:ind");
        el.setClosed(true);
        el.attr("w:left", left);
        el.attr("w:hanging", hanging);
        return el;
    }

    public String toString() {
        CheapElement el = this.toElement();
        return el.toString();
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getHanging() {
        return hanging;
    }

    public void setHanging(int hanging) {
        this.hanging = hanging;
    }

}
