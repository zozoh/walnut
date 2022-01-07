package org.nutz.walnut.ooml.xlsx;

import java.util.HashMap;
import java.util.Map;

import org.nutz.castor.Castors;
import org.nutz.json.JsonField;
import org.nutz.walnut.cheap.dom.CheapElement;

/**
 * 相关文档:<br>
 * <a href=
 * "https://docs.microsoft.com/en-us/dotnet/api/documentformat.openxml.spreadsheet.cell?view=openxml-2.8.1">cell</a>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class XlsxCell {

    private static final Map<String, XlsxCellType> TYPES = new HashMap<>();

    static {
        TYPES.put("b", XlsxCellType.Boolean);
        TYPES.put("e", XlsxCellType.Error);
        TYPES.put("inlineStr", XlsxCellType.InlineString);
        TYPES.put("n", XlsxCellType.Number);
        TYPES.put("s", XlsxCellType.SharedString);
        TYPES.put("str", XlsxCellType.String);
    }

    @JsonField(ignore = true)
    protected XlsxWorkbook workbook;

    private String reference;

    private int styleIndex;

    private XlsxCellType dataType;

    private Object value;

    private XlsxMedia media;

    public XlsxCell(XlsxWorkbook workbook, CheapElement el) {
        this.workbook = workbook;
        this.reference = el.attr("r");
        this.styleIndex = el.attrInt("s");
        this.dataType = TYPES.get(el.attr("t"));
        String v = null;

        // 直接指定了行内值
        if (this.isDataInlineString()) {
            CheapElement vEl = el.findElement(e -> e.isTagName("t"));
            if (null != vEl) {
                this.value = vEl.getText();
            }
        }
        // 引用了共享字符串 <v>23</v>
        else if (this.isDataSharedString()) {
            CheapElement vEl = el.findElement(e -> e.isTagName("v"));
            if (null != vEl)
                v = vEl.getText();

            // 空值
            if (null == v) {
                this.value = null;
            }
            // 获取共享字符串
            else {
                int i = Integer.parseInt(v);
                this.value = workbook.getSharedString(i);
            }
        }
        // 布尔
        else if (this.isDataBoolean()) {
            CheapElement vEl = el.findElement(e -> e.isTagName("v"));
            if (null != vEl)
                v = vEl.getText();
            this.value = Castors.me().castTo(v, Boolean.class);
        }
        // 其他值
        else {
            CheapElement vEl = el.findElement(e -> e.isTagName("v"));
            if (null != vEl)
                v = vEl.getText();
            this.value = Castors.me().castTo(v, Boolean.class);
        }
    }

    public String toString() {
        return String.format("[<%s>:%s]", reference, value);
    }

    public XlsxWorkbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(XlsxWorkbook workbook) {
        this.workbook = workbook;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public int getStyleIndex() {
        return styleIndex;
    }

    public void setStyleIndex(int styleIndex) {
        this.styleIndex = styleIndex;
    }

    public String getStringValue() {
        return null == value ? null : value.toString();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isDataBoolean() {
        return XlsxCellType.Boolean == this.dataType;
    }

    public boolean isDataError() {
        return XlsxCellType.Error == this.dataType;
    }

    public boolean isDataInlineString() {
        return XlsxCellType.InlineString == this.dataType;
    }

    public boolean isDataNumber() {
        return XlsxCellType.Number == this.dataType;
    }

    public boolean isDataSharedString() {
        return XlsxCellType.SharedString == this.dataType;
    }

    public boolean isDataString() {
        return XlsxCellType.String == this.dataType;
    }

    public XlsxCellType getDataType() {
        return dataType;
    }

    public void setDataType(XlsxCellType dataType) {
        this.dataType = dataType;
    }

    public boolean hasMedia() {
        return null != media;
    }

    public XlsxMedia getMedia() {
        return media;
    }

    public void setMedia(XlsxMedia media) {
        this.media = media;
    }

}
