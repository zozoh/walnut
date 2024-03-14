package com.site0.walnut.ooml.xlsx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.cheap.dom.CheapDocument;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.dom.CheapFilter;
import com.site0.walnut.cheap.xml.CheapXmlParsing;

public class XlsxDrawing extends XlsxObj {

    private List<XlsxMedia> medias;

    public XlsxDrawing(XlsxSheet sheet, String rId) {
        this.ooml = sheet.ooml;
        String aph = sheet.rels.getTargetPath(rId);
        this.setObjPath(aph);
        String input = entry.getContentStr();

        // 获取资源关系映射表
        this.loadRelationships();

        // 解析
        CheapDocument doc = new CheapDocument("worksheet");
        CheapXmlParsing parser = new CheapXmlParsing(doc);
        this.doc = parser.parseDoc(input);

        // 查找每个 Meida
        List<CheapElement> list = new LinkedList<>();
        this.doc.walkElements(new CheapFilter() {
            public boolean match(CheapElement el) {
                if (el.isTagName("xdr:twoCellAnchor")) {
                    list.add(el);
                    return false;
                }
                return true;
            }
        });

        // 创建对应的媒体
        medias = new ArrayList<>(list.size());
        for (CheapElement el : list) {
            XlsxMedia xm = new XlsxMedia(this, el);
            medias.add(xm);
        }
    }

    public List<XlsxMedia> getMedias() {
        return medias;
    }

}
