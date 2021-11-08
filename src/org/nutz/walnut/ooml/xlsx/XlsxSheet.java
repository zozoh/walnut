package org.nutz.walnut.ooml.xlsx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.JsonField;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapFilter;
import org.nutz.walnut.cheap.xml.CheapXmlParsing;
import org.nutz.walnut.util.Ws;

public class XlsxSheet extends XlsxObj {

    @JsonField(ignore = true)
    protected XlsxWorkbook workbook;

    protected List<XlsxRow> rows;

    protected List<XlsxMedia> medias;

    public XlsxSheet(XlsxWorkbook workbook, String rId) {
        this.workbook = workbook;
        this.ooml = workbook.ooml;
        String aph = workbook.rels.getTargetPath(rId);
        this.setObjPath(aph);
        String input = entry.getContentStr();

        // 获取工作簿的资源关系映射
        this.loadRelationships();

        // 解析工作表
        CheapDocument doc = new CheapDocument("worksheet");
        CheapXmlParsing parser = new CheapXmlParsing(doc);
        this.doc = parser.parseDoc(input);

        // 读取表格行 & 顺便得到一下 drawingId
        String[] drawingId = new String[1];
        List<CheapElement> els = new LinkedList<>();
        doc.walkElements(new CheapFilter() {
            public boolean match(CheapElement el) {
                if (el.isTagName("drawing")) {
                    drawingId[0] = el.attr("r:id");
                    return false;
                }
                if (el.isTagName("row")) {
                    els.add(el);
                    return false;
                }
                return true;
            }
        });

        // 解析表格中的行
        this.rows = new ArrayList<>(els.size());
        for (CheapElement el : els) {
            XlsxRow row = new XlsxRow(workbook, el);
            rows.add(row);
        }

        // 如果有外部嵌入的媒体图片等，需要分析一下属于哪个单元格
        if (drawingId[0] != null) {
            // 读取媒体列表
            XlsxDrawing drawing = new XlsxDrawing(this, drawingId[0]);
            this.medias = drawing.getMedias();

            // 编制媒体列表的索引
            Map<String, XlsxMedia> mediaMap = new HashMap<>();
            for (XlsxMedia media : medias) {
                String key = String.format("%d_%d",
                                           media.getFromRowIndex(),
                                           media.getFromColIndex());
                mediaMap.put(key, media);
            }

            // 搜索一遍所有的单元格
            int y = 0;
            for (XlsxRow row : this.rows) {
                int x = 0;
                for (XlsxCell cell : row.getCells()) {
                    String key = String.format("%d_%d", y, x);
                    XlsxMedia media = mediaMap.get(key);
                    cell.setMedia(media);
                    x++;
                }
                y++;
            }
        }
    }

    final static Pattern _K_M = Pattern.compile("^([A-Z]+)([0-9]+)$");

    /**
     * 根据某一行的单元格内容，生成一个 [A-Z] 对应的键值映射表。
     * 
     * @param rowIndex
     *            行下标
     * @return 列REFER 与一个键名的对应关系
     */
    public Map<String, String> getHeaderMapping(int rowIndex) {
        XlsxRow row = this.rows.get(rowIndex);
        Map<String, String> re = new HashMap<>();
        for (XlsxCell cell : row.getCells()) {
            Matcher m = _K_M.matcher(cell.getReference());
            if (m.find()) {
                String key = m.group(1);
                String val = Ws.trim(cell.getStringValue());
                re.put(key, val);
            }
        }
        return re;
    }

    public List<NutBean> toBeans(Map<String, String> header, int offset) {
        List<NutBean> list = new ArrayList<>(this.rows.size());
        int y = 0;
        for (XlsxRow row : this.rows) {
            if (y++ < offset) {
                continue;
            }
            NutBean bean = row.toBean(header);
            list.add(bean);
        }
        return list;
    }

    public List<XlsxRow> getRows() {
        return rows;
    }

    public List<XlsxMedia> getMedias() {
        return medias;
    }

}
