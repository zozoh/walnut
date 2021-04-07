package org.nutz.walnut.cheap.dom.docx;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;

import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTTblCellMar;
import org.docx4j.wml.CTTblLayoutType;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.PPrBase.NumPr.NumId;
import org.docx4j.wml.PPrBase.PStyle;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STTblLayoutType;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.cheap.css.CheapSize;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapNode;
import org.nutz.walnut.cheap.dom.CheapResourceLoader;
import org.nutz.walnut.cheap.dom.CheapText;
import org.nutz.walnut.cheap.dom.bean.CheapResource;
import org.nutz.walnut.util.Ws;

public class CheapDocxRendering {

    private CheapDocument doc;

    private WordprocessingMLPackage wp;

    /**
     * 如何加载资源
     */
    private CheapResourceLoader loader;

    private ObjectFactory factory;

    private NutBean varsData;

    private Map<String, CheapResource> resources;

    /* 采用奇数 */
    private int _seq_id1;
    /* 采用偶数 */
    private int _seq_id2;

    public CheapDocxRendering(CheapDocument doc,
                              WordprocessingMLPackage wp,
                              NutBean styleMapping,
                              NutBean varsData,
                              CheapResourceLoader loader) {
        this.doc = doc;
        this.wp = wp;
        this.loader = loader;
        this.factory = new ObjectFactory();

        this.resources = new HashMap<>();
        this._seq_id1 = 1;
        this._seq_id2 = 2;

        this.buildStyleMapping(styleMapping);

        this.varsData = null == varsData ? new NutMap() : varsData;
    }

    private Map<String, String> pageStyleMapping;
    private Map<String, String> tagNameStyleMapping;
    private Map<String, String> classNameStyleMapping;
    private List<AttrStyleMapping> attrStyleMapping;
    private static Pattern P_S = Pattern.compile("@([^=]+)=(.+)$");

    /**
     * 元素（特别是<code>H1~6</cod>），应该应对到哪个 w:style 呢？<br>
     * 元素（特别是<code>DIV</cod>）采用了特别的 className，应该应对到哪个 w:style 呢？ 元素标记了特殊属性应该应对哪个
     * w:style 呢？
     * <p>
     * 这里是一个映射表:
     * 
     * <pre>
     * {
     *    "${tagName}" : "${styleId}",
     *    "${className}" : "${styleId}",
     *    "@${attrName}=${attrValue}" : "${styleId}",
     *    "#header" : "${styleId}",
     *    "#footer" : "${styleId}"
     * }
     * 譬如
     * {
     *    "H1" : "10",
     *    "H2" : "20",
     *    "my-title" : "ab",
     *    "@doc-p=title" : "a0",
     * }
     * </pre>
     */
    private void buildStyleMapping(NutBean styleMapping) {
        pageStyleMapping = new HashMap<>();
        tagNameStyleMapping = new HashMap<>();
        classNameStyleMapping = new HashMap<>();
        attrStyleMapping = new LinkedList<>();

        // 循环处理映射表
        if (null != styleMapping) {
            for (Map.Entry<String, Object> en : styleMapping.entrySet()) {
                String key = en.getKey();
                String val = en.getValue().toString();
                // 页眉页脚
                if (key.startsWith("#")) {
                    pageStyleMapping.put(key, val);
                    continue;
                }
                // 全大写，表示元素名称
                if (key.matches("^([A-Z]+[A-Z0-9]*)$")) {
                    tagNameStyleMapping.put(key, val);
                    continue;
                }
                // 指定属性
                Matcher m = P_S.matcher(key);
                if (m.find()) {
                    String attrName = m.group(1);
                    String attrVal = m.group(2);
                    attrStyleMapping.add(new AttrStyleMapping(attrName, attrVal, val));
                    continue;
                }
                // 那么就是 className 咯
                classNameStyleMapping.put(key, val);
            }
        }
    }

    private String getStyleId(CheapElement el) {
        String styleId = null;
        // 尝试属性
        for (AttrStyleMapping asm : this.attrStyleMapping) {
            styleId = asm.tryGetStyle(el);
            if (null != styleId)
                return styleId;
        }
        // 尝试 className
        if (el.hasClassName()) {
            for (String className : el.getClassList()) {
                styleId = classNameStyleMapping.get(className);
                if (null != styleId)
                    return styleId;
            }
        }
        // 尝试标签名
        return tagNameStyleMapping.get(el.getStdTagName());
    }

    public Inline createImage(String id, int w, int h) {
        try {
            CheapResource cr = this.getResourceById(id);
            if (null == cr)
                return null;

            BinaryPartAbstractImage ip = cr.getImage(wp);

            _seq_id1 += 2;
            _seq_id2 += 2;

            String fnm = cr.getName();
            String alt = cr.getAlt();
            Inline inline = ip.createImageInline(fnm, alt, _seq_id1, _seq_id2, false);

            // 处理图像宽高...
            // TODO 蛋疼 啊，得不到图像的分辨率，先当作 96 来处理吧
            CTPositiveSize2D aExt = inline.getExtent();
            if (w > 0) {
                double dW = (double) w;
                long wEMU = (long) ((dW / 96.0) * 914400L);
                aExt.setCx(wEMU);
            }
            if (h > 0) {
                double dH = (double) h;
                long hEMU = (long) ((dH / 96.0) * 914400L);
                aExt.setCy(hEMU);
            }

            return inline;
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
    }

    public CheapResource getResourceById(String id) {
        CheapResource cr = resources.get(id);
        if (null == cr) {
            cr = loader.loadById(id);
            if (null != cr)
                resources.put(id, cr);
        }
        return cr;
    }

    private void joinBlockquote(List<Object> partItems, CheapElement el) {
        String styleId = this.getStyleId(el);
        P p = factory.createP();
        if (null != styleId) {
            setPStyle(p, styleId);
        }
        // 记入自己内容
        if (appendBlockChildren(p, el)) {
            partItems.add(p);
        }
        // 得到自己的子段落节点
        List<CheapElement> subPs = el.getChildElements(child -> child.isStdTagName("P"));
        for (CheapElement subP : subPs) {
            P p2 = factory.createP();
            if (null != styleId) {
                setPStyle(p2, styleId);
            }
            if (appendBlockChildren(p2, subP)) {
                partItems.add(p2);
            }
        }
    }

    private void setPStyle(P p, String styleId, String justifyContent, int indent) {
        boolean hasStyleId = !Ws.isBlank(styleId);
        boolean hasJustifyContent = !Ws.isBlank(justifyContent);
        boolean setPPr = false;
        if (!hasStyleId && !hasJustifyContent && indent < 0) {
            return;
        }
        PPr pPr = factory.createPPr();
        if (hasStyleId) {
            PStyle pStyle = factory.createPPrBasePStyle();
            pStyle.setVal(styleId);
            pPr.setPStyle(pStyle);
            setPPr = true;
        }
        if (indent >= 0) {
            Ind ind = new Ind();
            BigInteger v = BigInteger.valueOf(indent);
            ind.setFirstLine(v);
            ind.setFirstLineChars(v);
            pPr.setInd(ind);
            setPPr = true;
        }
        if (hasJustifyContent) {
            Jc jc = factory.createJc();
            String jcs = justifyContent.toUpperCase();
            try {
                jc.setVal(JcEnumeration.valueOf(jcs));
                pPr.setJc(jc);
                setPPr = true;
            }
            catch (Exception e) {}
        }

        if (setPPr) {
            p.setPPr(pPr);
        }
    }

    private void setPStyle(P p, String styleId, String justifyContent) {
        setPStyle(p, styleId, justifyContent, -1);
    }

    private void setPStyle(P p, String styleId) {
        setPStyle(p, styleId, null, -1);
    }

    private void joinHeading(List<Object> partItems, CheapElement el) {
        String styleId = this.getStyleId(el);
        String align = el.getStyle("text-align");
        P p = factory.createP();
        if (null != styleId) {
            setPStyle(p, styleId, align);
        }
        if (appendBlockChildren(p, el)) {
            partItems.add(p);
        }
    }

    private void joinP(List<Object> partItems, CheapElement el) {
        String styleId = this.getStyleId(el);
        String align = el.getStyle("text-align");
        boolean inCell = el.attrBoolean("in-table-cell");

        // 首行缩进
        int indent = -1;
        if (inCell || "center".equals(align)) {
            indent = 0;
        }

        P p = factory.createP();
        setPStyle(p, styleId, align, indent);

        // 段落是一定要加入的，因为为了显示空行
        appendBlockChildren(p, el);
        partItems.add(p);
    }

    private int listLvl = 0;

    private int listNumId = 0;

    private void joinLi(List<Object> partItems, CheapElement el) {
        // 搞自己
        P p = factory.createP();

        // 增加自己的属性
        PPr pPr = factory.createPPr();
        p.setPPr(pPr);

        String styleId = this.getStyleId(el);
        if (null != styleId) {
            PStyle pStyle = factory.createPPrBasePStyle();
            pStyle.setVal(styleId);
            pPr.setPStyle(pStyle);
        }

        // Create and add <w:numPr>
        NumPr numPr = factory.createPPrBaseNumPr();
        pPr.setNumPr(numPr);

        // The <w:ilvl> element
        Ilvl ilvlElement = factory.createPPrBaseNumPrIlvl();
        numPr.setIlvl(ilvlElement);
        ilvlElement.setVal(BigInteger.valueOf(this.listLvl));

        // The <w:numId> element
        NumId numIdElement = factory.createPPrBaseNumPrNumId();
        numPr.setNumId(numIdElement);
        numIdElement.setVal(BigInteger.valueOf(this.listNumId));

        // 设置自己的内容
        if (appendBlockChildren(p, el)) {
            partItems.add(p);
        }

        // 搞子节点：增加级别
        this.listLvl++;
        for (CheapElement child : el.getChildElements()) {
            this.dispatchBlock(partItems, child);
        }
        // 回退级别
        this.listLvl--;
    }

    private void joinOl(List<Object> partItems, CheapElement el) {
        int oldNumId = this.listNumId;
        this.listNumId = 1;
        for (CheapElement li : el.getChildElements()) {
            joinLi(partItems, li);
        }
        this.listNumId = oldNumId;
    }

    private void joinUl(List<Object> partItems, CheapElement el) {
        int oldNumId = this.listNumId;
        this.listNumId = 2;
        for (CheapElement li : el.getChildElements()) {
            joinLi(partItems, li);
        }
        this.listNumId = oldNumId;
    }

    private void joinTableCell(Tr tr, CheapElement el) {
        Tc td = factory.createTc();
        // TODO 设置 Table cell 的属性

        // 获取单元格段落样式
        String styleId = this.getStyleId(el);
        String align = el.getStyle("text-align");

        // 依次搞单元格内容
        List<Object> tdContent = td.getContent();

        // 收集行内元素
        List<CheapNode> inlines = new LinkedList<>();

        // 逐次处理表格单元格内部的子元素
        for (CheapNode node : el.getChildren()) {
            // 文本节点：先保存
            if (node.isText()) {
                inlines.add(node);
                continue;
            }
            // Inline 元素：先保存
            if (node.isElement()) {
                CheapElement ce = (CheapElement) node;
                if (ce.isStdTagAs("^(BR|IMG|B|I|U|STRONG|EM|A|SPAN)$")) {
                    inlines.add(ce);
                    continue;
                }
                // 让下面的块也都具备统一的排列方式
                if (!Ws.isBlank(align)) {
                    ce.setStyle("text-align", align);
                }
                // 标识一下这个元素在表格内，譬如 P，以便强制去掉首行缩进
                ce.attr("in-table-cell", true);
            }
            // 处理块元素之前，看看有没有必要先处理一下已有的元素
            if (!inlines.isEmpty()) {
                P p = factory.createP();
                this.setPStyle(p, styleId, align);
                List<Object> pContent = p.getContent();
                if (joinInlineElements(pContent, inlines)) {
                    tdContent.add(p);
                }
                inlines.clear();
            }

            // 处理块元素
            this.dispatchBlock(tdContent, node);
        }

        // 处理最后一个
        if (!inlines.isEmpty()) {
            P p = factory.createP();
            this.setPStyle(p, styleId, align);
            List<Object> pContent = p.getContent();
            if (joinInlineElements(pContent, inlines)) {
                tdContent.add(p);
            }
            inlines.clear();
        }

        // 如果为空，则搞一个空行出来
        if (tdContent.isEmpty()) {
            P p = factory.createP();
            tdContent.add(p);
        }

        // 计入行
        tr.getContent().add(td);
    }

    private void joinTableRow(Tbl table, CheapElement el) {
        Tr tr = factory.createTr();
        for (CheapElement child : el.getChildElements()) {
            if (child.isStdTagName("TD") || child.isStdTagName("TH")) {
                joinTableCell(tr, child);
            }
        }
        table.getContent().add(tr);
    }

    private int getTableColumnsCount(CheapElement eTab) {
        CheapElement eTr = eTab.findElement(el -> el.isStdTagName("TR"));
        return eTr.countChildElements(el -> el.isStdTagName("TD"));
    }

    private void joinTable(List<Object> partItems, CheapElement el) {
        Tbl table = factory.createTbl();
        TblPr tPr = factory.createTblPr();
        table.setTblPr(tPr);
        // 计算表格边框
        CheapSize bo = el.attrSize("border", "0px");
        int border = bo.getIntValue();

        // 设置表格边框
        TblBorders tBs = factory.createTblBorders();
        tBs.setTop(createBorder(border));
        tBs.setBottom(createBorder(border));
        tBs.setLeft(createBorder(border));
        tBs.setRight(createBorder(border));
        tBs.setInsideH(createBorder(border > 0 ? 1 : 0));
        tBs.setInsideV(createBorder(border > 0 ? 1 : 0));
        tPr.setTblBorders(tBs);

        // 设置表格边距
        CheapSize pad = el.attrSize("cellpadding", "0px");
        int padding = pad.getIntValue();

        int tbPadding = padding * 6;
        CTTblCellMar mar = factory.createCTTblCellMar();
        mar.setTop(createWidth(tbPadding));
        mar.setBottom(createWidth(tbPadding));
        mar.setLeft(createWidth(tbPadding));
        mar.setRight(createWidth(tbPadding));
        tPr.setTblCellMar(mar);

        // 设置表格宽度
        int colN = this.getTableColumnsCount(el);
        tPr.setTblW(createWidth(8613));
        CTTblLayoutType clt = new CTTblLayoutType();
        clt.setType(STTblLayoutType.FIXED);
        tPr.setTblLayout(clt);
        // 设置单元格宽度
        int cellWidth = 8613 / colN;
        int remainWidth = 8613 - (cellWidth * colN);
        TblGrid tGrid = factory.createTblGrid();
        for (int i = 0; i < colN; i++) {
            int w = cellWidth;
            if (i == (colN - 1)) {
                w += remainWidth;
            }
            TblGridCol col = factory.createTblGridCol();
            col.setW(BigInteger.valueOf(w));
            tGrid.getGridCol().add(col);
        }
        table.setTblGrid(tGrid);

        // 设置单元格
        for (CheapElement child : el.getChildElements()) {
            // THEAD
            // TBODY
            if (child.isStdTagName("THEAD") || child.isStdTagName("TBODY")) {
                for (CheapElement c2 : child.getChildElements()) {
                    if (c2.isStdTagName("TR")) {
                        joinTableRow(table, c2);
                    }
                }
            }
            // TR
            else if (child.isStdTagName("TR")) {
                joinTableRow(table, child);
            }
        }
        partItems.add(table);
    }

    private TblWidth createWidth(int tableWidth) {
        TblWidth tW = factory.createTblWidth();
        tW.setW(BigInteger.valueOf(tableWidth));
        tW.setType("dxa");
        return tW;
    }

    private CTBorder createBorder(int border) {
        CTBorder b = factory.createCTBorder();
        b.setVal(0 == border ? STBorder.NONE : STBorder.SINGLE);
        b.setSz(BigInteger.valueOf(6 * border));
        b.setSpace(BigInteger.valueOf(0));
        b.setColor("auto");
        return b;
    }

    private void joinDiv(List<Object> partItems, CheapElement el) {
        // 如果找到了样式映射，就不是简单的一层包裹咯
        String styleId = this.getStyleId(el);

        // 简单的包裹，递进一层
        if (null == styleId) {
            if (el.hasChildren()) {
                for (CheapNode child : el.getChildren()) {
                    dispatchBlock(partItems, child);
                }
            }
        }
        // 不是简单的包裹，那么就当作段落处理
        else {
            P p = factory.createP();
            this.setPStyle(p, styleId);
            if (appendBlockChildren(p, el)) {
                partItems.add(p);
            }
        }
    }

    private boolean appendBlockChildren(P p, CheapElement el) {
        List<CheapNode> children = el.getChildren();
        List<Object> pContent = p.getContent();
        return joinInlineElements(pContent, children);
    }

    private boolean joinInlineElements(List<Object> pContent, List<CheapNode> children) {
        boolean re = false;
        if (null != children) {
            for (CheapNode node : children) {
                // 文本节点
                if (node.isText()) {
                    re |= appendText(pContent, (CheapText) node, null);
                }
                // 行内元素
                else if (node.isElement()) {
                    re |= appendInline(pContent, (CheapElement) node, null);
                }
            }
        }
        return re;
    }

    private void joinText(List<Object> partItems, CheapText txt) {
        P p = factory.createP();
        if (this.appendText(p.getContent(), txt, null)) {
            partItems.add(p);
        }
    }

    private final static BooleanDefaultTrue YES = new BooleanDefaultTrue();

    private boolean appendText(List<Object> pContent, CheapText txt, DocxElStyle es) {
        if (txt.isBlank()) {
            return false;
        }

        R r = factory.createR();
        if (null != es && es.hasStyle()) {
            // 增加自己的属性
            RPr rPr = factory.createRPr();
            r.setRPr(rPr);
            if (es.bold) {
                rPr.setB(YES);
            }
            if (es.italic) {
                rPr.setI(YES);
            }
            if (es.underline) {
                rPr.setU(factory.createU());
            }
        }
        Text t = factory.createText();
        t.setValue(txt.decodeText());
        r.getContent().add(t);
        pContent.add(r);

        return true;
    }

    private boolean appendBr(List<Object> pContent, CheapElement el) {
        // 计入文档
        R r = factory.createR();
        Br br = factory.createBr();
        r.getContent().add(br);
        pContent.add(r);

        return true;
    }

    private boolean appendImage(List<Object> pContent, CheapElement el) {
        // 得到图片的 ID
        String imgId = el.attr("wn-obj-id");
        if (Ws.isBlank(imgId)) {
            return false;
        }

        // 得到元素里声明的宽高
        int w = el.attrInt("width", -1);
        if (w < 0) {
            w = el.attrInt("wn-obj-width", -1);
        }
        int h = el.attrInt("height", -1);
        if (h < 0) {
            h = el.attrInt("wn-obj-height", -1);
        }

        // 得到图片元素
        Inline img = this.createImage(imgId, w, h);
        if (null == img) {
            return false;
        }

        // 计入文档
        R r = factory.createR();
        Drawing drawing = factory.createDrawing();
        r.getContent().add(drawing);
        drawing.getAnchorOrInline().add(img);
        pContent.add(r);

        return true;
    }

    private boolean appendBold(List<Object> pContent, CheapElement el, DocxElStyle es) {
        boolean old = false;
        if (null == es) {
            es = new DocxElStyle();
        } else {
            old = es.bold;
        }
        try {
            es.bold = true;
            return dispatchInlineChildren(pContent, el, es);
        }
        finally {
            es.bold = old;
        }
    }

    private boolean appendItalic(List<Object> pContent, CheapElement el, DocxElStyle es) {
        boolean old = false;
        if (null == es) {
            es = new DocxElStyle();
        } else {
            old = es.italic;
        }
        try {
            es.italic = true;
            return dispatchInlineChildren(pContent, el, es);
        }
        finally {
            es.italic = old;
        }
    }

    private boolean appendUnderline(List<Object> pContent, CheapElement el, DocxElStyle es) {
        boolean old = false;
        if (null == es) {
            es = new DocxElStyle();
        } else {
            old = es.underline;
        }
        try {
            es.underline = true;
            return dispatchInlineChildren(pContent, el, es);
        }
        finally {
            es.underline = old;
        }
    }

    private boolean appendSpan(List<Object> pContent, CheapElement el, DocxElStyle es) {
        if (null == es) {
            es = new DocxElStyle();
        }
        return dispatchInlineChildren(pContent, el, es);
    }

    private boolean dispatchInlineChildren(List<Object> pContent, CheapElement el, DocxElStyle es) {
        // System.out.printf("dispatchInlineChildren: %s\n", el.toBrief());
        boolean re = false;
        if (el.hasChildren()) {
            for (CheapNode node : el.getChildren()) {
                // 文本节点
                if (node.isText()) {
                    re |= appendText(pContent, (CheapText) node, es);
                }
                // 行内元素
                else if (node.isElement()) {
                    re |= appendInline(pContent, (CheapElement) node, es);
                }
            }
        }
        return re;
    }

    private boolean appendInline(List<Object> pContent, CheapElement el, DocxElStyle es) {
        // System.out.printf("appendInline: %s\n", el.toBrief());
        boolean re = false;

        // 处理 BR
        if (el.isStdTagName("BR")) {
            re |= appendBr(pContent, el);
        }
        // 处理图像
        else if (el.isStdTagName("IMG")) {
            re |= appendImage(pContent, el);
        }
        // 处理加粗
        else if (el.isStdTagName("STRONG") || el.isStdTagName("B")) {
            re |= appendBold(pContent, el, es);
        }
        // 处理斜体
        else if (el.isStdTagName("EM") || el.isStdTagName("I")) {
            re |= appendItalic(pContent, el, es);
        }
        // 处理下划线
        else if (el.isStdTagName("U")) {
            re |= appendUnderline(pContent, el, es);
        }
        // 处理超链接
        else if (el.isStdTagName("A")) {
            re |= appendUnderline(pContent, el, es);
        }
        // 处理普通文字
        else if (el.isStdTagName("SPAN")) {
            re |= appendSpan(pContent, el, es);
        }
          // 其他的统统无视
        else {
            // re |= dispatchInlineChildren(pContent, el, es);
        }

        return re;
    }

    private void dispatchBlock(List<Object> partItems, CheapNode node) {
        // System.out.printf("dispatchBlock: %s\n", node.toBrief());
        if (node.isText()) {
            this.joinText(partItems, (CheapText) node);
            return;
        }

        if (!node.isElement())
            return;

        CheapElement el = (CheapElement) node;
        // if (!el.hasChildren())
        // return;

        // 处理 DIV
        if (el.isStdTagName("DIV")) {
            joinDiv(partItems, el);
        }
        // 处理标题
        else if (el.isStdTagAs("^(H[1-6])$")) {
            joinHeading(partItems, el);
        }
        // 处理段落
        else if (el.isStdTagName("P")) {
            joinP(partItems, el);
        }
        // 处理表格
        else if (el.isStdTagName("TABLE")) {
            joinTable(partItems, el);
        }
        // 处理无序列表
        else if (el.isStdTagName("UL")) {
            joinUl(partItems, el);
        }
        // 处理有序列表
        else if (el.isStdTagName("OL")) {
            joinOl(partItems, el);
        }
        // 处理引用
        else if (el.isStdTagName("BLOCKQUOTE")) {
            joinBlockquote(partItems, el);
        }
    }

    private void setPText(P p, String txt) {
        R r = factory.createR();
        Text t = factory.createText();
        t.setValue(txt);
        r.getContent().add(t);
        p.getContent().add(r);
    }

    private void joinRunText(StringBuilder sb, R r) {
        List<Object> content = r.getContent();
        for (Object o : content) {
            if (o instanceof JAXBElement<?>) {
                JAXBElement<?> jax = (JAXBElement<?>) o;
                Object ot = jax.getValue();
                if (null != ot && (ot instanceof Text)) {
                    Text t = (Text) ot;
                    sb.append(t.getValue());
                }
            }
        }
    }

    private String getPText(P p) {
        StringBuilder sb = new StringBuilder();
        List<Object> content = p.getContent();
        for (Object o : content) {
            if (o instanceof R) {
                joinRunText(sb, (R) o);
            }
        }
        return sb.toString();
    }

    private void handleHeader(HeaderPart headerPart) throws Exception {
        List<Object> headList = headerPart.getContent();
        String styleId = this.pageStyleMapping.get("#header");
        for (Object headP : headList) {
            if (headP instanceof P) {
                P p = (P) headP;
                String str = this.getPText(p);
                String s2 = Tmpl.exec(str, this.varsData);
                p.getContent().clear();
                this.setPStyle(p, styleId);
                this.setPText(p, s2);
            }
        }
    }

    public WordprocessingMLPackage render() throws Exception {
        // 渲染文档头（页眉和页脚）
        // TODO ...
        List<SectionWrapper> sections = wp.getDocumentModel().getSections();
        if (null != sections && !sections.isEmpty()) {
            for (int i = 0; i < sections.size(); i++) {
                HeaderFooterPolicy hfp = sections.get(i).getHeaderFooterPolicy();
                // 首页不同的页眉的header
                HeaderPart firstHeader = hfp.getFirstHeader();
                if (firstHeader != null) {
                    handleHeader(firstHeader);
                }
                // 普通情况的页眉header
                HeaderPart headerPart = hfp.getDefaultHeader();
                if (null != headerPart)
                    handleHeader(headerPart);
            }
        }

        // 渲染文档体
        MainDocumentPart part = wp.getMainDocumentPart();
        List<Object> partItems = part.getContent();
        for (CheapNode node : doc.body().getChildren()) {
            this.dispatchBlock(partItems, node);
        }

        // 最后保存
        return wp;
    }

    public WordprocessingMLPackage getWpPackage() {
        return wp;
    }

}
