package org.nutz.walnut.ooml.xlsx;

import org.nutz.walnut.cheap.dom.CheapElement;

public class XlsxMedia {

    private int fromColIndex;

    private int fromRowIndex;

    private String referId;

    private String path;

    public XlsxMedia(XlsxDrawing drawing, CheapElement el) {
        // 得到 from
        CheapElement eFrom = el.findElement(e -> e.isTagName("xdr:from"));
        this.fromColIndex = eFrom.findElement(e -> e.isTagName("xdr:col")).getAsInt();
        this.fromRowIndex = eFrom.findElement(e -> e.isTagName("xdr:row")).getAsInt();

        // 找到填充方式
        CheapElement eBlip = el.findElement(e -> e.isTagName("a:blip"));
        this.referId = eBlip.attr("r:embed");

        // 得到媒体路径
        this.path = drawing.rels.getTargetPath(this.referId);
    }

    public int getFromColIndex() {
        return fromColIndex;
    }

    public void setFromColIndex(int fromColIndex) {
        this.fromColIndex = fromColIndex;
    }

    public int getFromRowIndex() {
        return fromRowIndex;
    }

    public void setFromRowIndex(int fromRowIndex) {
        this.fromRowIndex = fromRowIndex;
    }

    public String getReferId() {
        return referId;
    }

    public void setReferId(String referId) {
        this.referId = referId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}