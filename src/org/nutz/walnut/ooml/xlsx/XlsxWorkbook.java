package org.nutz.walnut.ooml.xlsx;

import java.util.List;

import org.nutz.json.JsonField;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapFilter;
import org.nutz.walnut.cheap.html.CheapHtmlParsing;
import org.nutz.walnut.ooml.OomlEntry;
import org.nutz.walnut.ooml.OomlPackage;
import org.nutz.walnut.ooml.OomlRelType;
import org.nutz.walnut.ooml.OomlRels;
import org.nutz.walnut.util.Wpath;
import org.nutz.walnut.util.Ws;

public class XlsxWorkbook extends XlsxObj {

    @JsonField(ignore = true)
    protected CheapElement body;

    protected String[] sharedStrings;

    public XlsxWorkbook(OomlPackage ooml) {
        this.ooml = ooml;

        // 读取工作表内容
        this.setObjPath("xl/workbook.xml");
        String input = entry.getContentStr();

        // 获取工作簿的资源关系映射
        this.loadRelationships();

        // 解析统一字符串表
        String target = this.rels.getTargetBy(OomlRelType.SHARED_STRING);
        String aph = Wpath.appendPath(this.parentPath, target);
        this.loadSharedStrings(aph);

        // 解析工作簿
        CheapDocument doc = new CheapDocument("workbook", null);
        CheapHtmlParsing parser = new CheapHtmlParsing(doc, null);
        this.doc = parser.invoke(input);
        this.body = doc.findElement(e -> e.isTagName("sheets"));
    }

    private void loadSharedStrings(String aph) {
        OomlEntry en = ooml.getEntry(aph);
        String input = en.getContentStr();
        CheapDocument doc = new CheapDocument("sst", null);
        CheapHtmlParsing parser = new CheapHtmlParsing(doc, null);
        doc = parser.invoke(input);
        List<CheapElement> list = doc.findElements(e -> e.isTagName("t"));
        sharedStrings = new String[list.size()];
        int i = 0;
        for (CheapElement el : list) {
            sharedStrings[i++] = Ws.trim(el.getText());
        }
    }

    /**
     * 获取工作表对象
     * 
     * @param sheetId
     *            工作表的 ID
     * @return 工作表对象
     */
    public XlsxSheet getSheet(String sheetId) {
        CheapElement el = body.findElement(new CheapFilter() {
            public boolean match(CheapElement e) {
                return e.isTagName("sheet") && e.isAttr("sheetId", sheetId);
            }
        });
        if (null != el) {
            String rId = el.attr("r:id");
            return new XlsxSheet(this, rId);
        }
        return null;
    }

    public String[] getSharedStrings() {
        return sharedStrings;
    }

    public String getSharedString(int index) {
        return sharedStrings[index];
    }

    public CheapDocument getDoc() {
        return doc;
    }

    public OomlRels getRels() {
        return this.rels;
    }

}
