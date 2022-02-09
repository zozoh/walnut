package org.nutz.walnut.cheap.dom.docx.num;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.docx.Docxs;

public class DocxNumbering {

    private static final NutMap xmlnss = new NutMap();

    static {
        xmlnss.put("wpc", "http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas");
        xmlnss.put("cx", "http://schemas.microsoft.com/office/drawing/2014/chartex");
        xmlnss.put("cx1", "http://schemas.microsoft.com/office/drawing/2015/9/8/chartex");
        xmlnss.put("cx2", "http://schemas.microsoft.com/office/drawing/2015/10/21/chartex");
        xmlnss.put("cx3", "http://schemas.microsoft.com/office/drawing/2016/5/9/chartex");
        xmlnss.put("cx4", "http://schemas.microsoft.com/office/drawing/2016/5/10/chartex");
        xmlnss.put("cx5", "http://schemas.microsoft.com/office/drawing/2016/5/11/chartex");
        xmlnss.put("cx6", "http://schemas.microsoft.com/office/drawing/2016/5/12/chartex");
        xmlnss.put("cx7", "http://schemas.microsoft.com/office/drawing/2016/5/13/chartex");
        xmlnss.put("cx8", "http://schemas.microsoft.com/office/drawing/2016/5/14/chartex");
        xmlnss.put("mc", "http://schemas.openxmlformats.org/markup-compatibility/2006");
        xmlnss.put("aink", "http://schemas.microsoft.com/office/drawing/2016/ink");
        xmlnss.put("am3d", "http://schemas.microsoft.com/office/drawing/2017/model3d");
        xmlnss.put("o", "urn:schemas-microsoft-com:office:office");
        xmlnss.put("r", "http://schemas.openxmlformats.org/officeDocument/2006/relationships");
        xmlnss.put("m", "http://schemas.openxmlformats.org/officeDocument/2006/math");
        xmlnss.put("v", "urn:schemas-microsoft-com:vml");
        xmlnss.put("wp14", "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing");
        xmlnss.put("wp", "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing");
        xmlnss.put("w10", "urn:schemas-microsoft-com:office:word");
        xmlnss.put("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main");
        xmlnss.put("w14", "http://schemas.microsoft.com/office/word/2010/wordml");
        xmlnss.put("w15", "http://schemas.microsoft.com/office/word/2012/wordml");
        xmlnss.put("w16cex", "http://schemas.microsoft.com/office/word/2018/wordml/cex");
        xmlnss.put("w16cid", "http://schemas.microsoft.com/office/word/2016/wordml/cid");
        xmlnss.put("w16", "http://schemas.microsoft.com/office/word/2018/wordml");
        xmlnss.put("w16sdtdh", "http://schemas.microsoft.com/office/word/2020/wordml/sdtdatahash");
        xmlnss.put("w16se", "http://schemas.microsoft.com/office/word/2015/wordml/symex");
        xmlnss.put("wpg", "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup");
        xmlnss.put("wpi", "http://schemas.microsoft.com/office/word/2010/wordprocessingInk");
        xmlnss.put("wne", "http://schemas.microsoft.com/office/word/2006/wordml");
        xmlnss.put("wps", "http://schemas.microsoft.com/office/word/2010/wordprocessingShape");
    }

    private static final String mcIgnorable = "w14 w15 w16se w16cid w16 w16cex w16sdtdh wp14";

    static class NumId {
        int id;
        int aIndex;

        NumId(int id, int aIndex) {
            this.id = id;
            this.aIndex = aIndex;
        }
    }

    private List<DocxAbstractNum> abstractNums;

    private List<NumId> nums;

    public DocxNumbering() {
        abstractNums = new ArrayList<>(20);
        nums = new LinkedList<>();
    }

    public CheapDocument toDocument() {
        CheapDocument doc = new CheapDocument("w:numbering");
        CheapElement el = doc.root();

        // 名称空间
        for (Map.Entry<String, Object> en : xmlnss.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            el.attr("xmlns:" + key, val);
        }
        el.attr("mc:Ignorable", mcIgnorable);

        // 抽象编号
        int aIndex = 0;
        for (DocxAbstractNum an : abstractNums) {
            CheapElement sub = an.toElement(aIndex++);
            el.append(sub);
        }

        // 引用编号
        for (NumId num : nums) {
            CheapElement sub = new CheapElement("w:num");
            sub.attr("w:numId", num.id);
            CheapElement ar = Docxs.genElVal("w:abstractNumId", num.aIndex);
            sub.append(ar);
            el.append(sub);
        }

        return doc;
    }

    public String toMarkup() {
        CheapDocument doc = this.toDocument();
        return doc.toMarkup();
    }

    public String toFormatMarkup() {
        CheapDocument doc = this.toDocument();
        doc.formatAsXml();
        return doc.toMarkup();
    }

    public String toString() {
        CheapDocument doc = this.toDocument();
        return doc.toString();
    }

    public boolean isEmpty() {
        return this.nums.isEmpty();
    }

    public void addNum(int numId, DocxAbstractNum num) {
        // 找到一个重复的抽象编号
        int aIndex = -1;
        int len = this.abstractNums.size();
        for (int i = 0; i < len; i++) {
            DocxAbstractNum an = this.abstractNums.get(i);
            if (an.equals(num)) {
                aIndex = i;
                break;
            }
        }
        // 否则增加一个
        if (aIndex < 0) {
            aIndex = this.abstractNums.size();
            this.abstractNums.add(num.clone());
        }
        // 添加引用编号
        this.nums.add(new NumId(numId, aIndex));
    }

    public List<DocxAbstractNum> getAbstractNums() {
        return abstractNums;
    }

    public void setAbstractNums(List<DocxAbstractNum> abstractNums) {
        this.abstractNums = abstractNums;
    }

    public List<NumId> getNums() {
        return nums;
    }

    public void setNums(List<NumId> nums) {
        this.nums = nums;
    }

}
