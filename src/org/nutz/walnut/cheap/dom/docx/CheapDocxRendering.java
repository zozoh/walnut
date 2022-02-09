package org.nutz.walnut.cheap.dom.docx;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;

import org.apache.commons.imaging.ImageInfo;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTHeight;
import org.docx4j.wml.CTTblCellMar;
import org.docx4j.wml.CTTblLayoutType;
import org.docx4j.wml.CTVerticalJc;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.PPrBase.NumPr.NumId;
import org.docx4j.wml.PPrBase.PStyle;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.STHint;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.STTblLayoutType;
import org.docx4j.wml.STVerticalJc;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner.GridSpan;
import org.docx4j.wml.TcPrInner.VMerge;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.cheap.api.CheapResourceLoader;
import org.nutz.walnut.cheap.css.CheapSize;
import org.nutz.walnut.cheap.css.CheapStyle;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapNode;
import org.nutz.walnut.cheap.dom.CheapText;
import org.nutz.walnut.cheap.dom.bean.CheapResource;
import org.nutz.walnut.cheap.dom.docx.num.DocxAbstractNum;
import org.nutz.walnut.cheap.dom.docx.num.DocxNumbering;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wnum;
import org.nutz.walnut.util.Ws;

public class CheapDocxRendering {

    private CheapDocument doc;

    private WordprocessingMLPackage wp;

    /**
     * 用来收集整个文档的编号记录
     */
    private DocxNumbering numbering;

    /**
     * 记录当前的列表级别
     */
    private DocxAbstractNum _a_num;

    /**
     * 如何加载资源。
     * <p>
     * 譬如，一个 DOM 里面的<code>IMG</cod>引用了系统中的图，打成 zip 包时，需要读取这个图片的内容。
     */
    private CheapResourceLoader loader;

    private ObjectFactory factory;

    private NutBean varsData;

    private Map<String, CheapResource> resources;

    private int pageTableWidth;

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
        this.pageTableWidth = 8613;

        this.numbering = new DocxNumbering();
        this._a_num = new DocxAbstractNum();

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

            // 保证文件名是安全的
            fnm = fnm.replaceAll("&", "&amp;");
            fnm = fnm.replaceAll("<", "&lt;");
            fnm = fnm.replaceAll(">", "&gt;");

            // 保证 alt 是安全的
            if (null != alt) {
                alt = alt.replaceAll("&", "&amp;");
                alt = alt.replaceAll("<", "&lt;");
                alt = alt.replaceAll(">", "&gt;");
            }

            // 创建元素
            Inline inline = ip.createImageInline(fnm, alt, _seq_id1, _seq_id2, false);

            // 得到图像的 dpi
            ImageInfo imi = cr.getImageInfo();
            int dpiW = imi.getPhysicalWidthDpi();
            if (dpiW < 0) {
                dpiW = 96;
            }
            int dpiH = imi.getPhysicalHeightDpi();
            if (dpiH < 0) {
                dpiH = 96;
            }

            // 处理图像宽高...
            CTPositiveSize2D aExt = inline.getExtent();
            if (w > 0) {
                double dW = (double) w;
                long wEMU = (long) ((dW / dpiW) * 914400L);
                aExt.setCx(wEMU);
            }
            if (h > 0) {
                double dH = (double) h;
                long hEMU = (long) ((dH / dpiH) * 914400L);
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
            // CSS 的样式值，这里应该是 BOTH
            if ("JUSTIFY".equals(jcs)) {
                jcs = "BOTH";
            }
            try {
                jc.setVal(JcEnumeration.valueOf(jcs));
                pPr.setJc(jc);
                setPPr = true;
            }
            catch (Exception e) {
                // 忍耐这个错误
                throw Er.wrap(e);
            }
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
        if (inCell || "center".equals(align) || el.isAttr("doc-heading", "no-indent")) {
            indent = 0;
        }

        P p = factory.createP();
        setPStyle(p, styleId, align, indent);

        // 段落是一定要加入的，因为为了显示空行
        appendBlockChildren(p, el);
        partItems.add(p);
    }

    private int listLvl = 0;

    private int listNumId = 1;

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
        ilvlElement.setVal(BigInteger.valueOf(this.listLvl - 1));

        // The <w:numId> element
        NumId numIdElement = factory.createPPrBaseNumPrNumId();
        numPr.setNumId(numIdElement);
        numIdElement.setVal(BigInteger.valueOf(this.listNumId));

        // 设置自己的内容
        if (appendBlockChildren(p, el)) {
            partItems.add(p);
        }

        // 搞子节点：增加级别
        for (CheapElement child : el.getChildElements()) {
            this.dispatchBlock(partItems, child);
        }
    }

    private void joinOl(List<Object> partItems, CheapElement el) {
        // 有序列表一定是要开始一个重新编号的
        if (!this._a_num.isEmpty()) {
            this.numbering.addNum(this.listNumId, this._a_num);
            this._a_num.reset();
        }
        this.listNumId++;
        // 推入一定是成功的
        if (!this._a_num.tryPushLvlForOl(this.listLvl)) {
            throw Wlang.impossible();
        }
        // 记入列表级别成功，层级加1
        this.listLvl++;

        for (CheapElement li : el.getChildElements()) {
            joinLi(partItems, li);
        }

        // 退出列表，层级回退
        this.listLvl--;

        // 在文档处理结束时，会检查最后一个编号设定 ...
    }

    private void joinUl(List<Object> partItems, CheapElement el) {
        // 尝试加入一个列表级别，如果加入不成功，则表示有冲突，那么就要开始一个新列表
        if (!this._a_num.tryPushLvlForUl(this.listLvl)) {
            this.numbering.addNum(this.listNumId, this._a_num);
            this.listNumId++;
            // 重置当前列表，并尝试再推入
            this._a_num.reset();
            this._a_num.tryPushLvlForUl(this.listLvl);
        }
        // 记入列表级别成功，层级加1
        this.listLvl++;

        for (CheapElement li : el.getChildElements()) {
            joinLi(partItems, li);
        }

        // 退出列表，层级回退
        this.listLvl--;

        // 在文档处理结束时，会检查最后一个编号设定 ...
    }

    private void joinTableCell(Tr tr, CheapElement el) {
        Tc td = factory.createTc();
        TcPr tcPr = factory.createTcPr();
        td.setTcPr(tcPr);

        // TODO 设置 Table cell 的属性
        CheapStyle style = el.getStyleObj();
        String valign = el.attrString("valign", "center");
        valign = style.getString("vertical-align", valign);

        // 获取单元格段落样式:垂直居中
        // String styleId = this.getStyleId(el);
        String styleTagName = "TD";
        CheapElement elTable = el.getClosestByTagName("TABLE");
        if (null != elTable) {
            CheapSize bo = elTable.attrSize("border", "0px");
            int border = bo.getIntValue();
            if (border <= 0) {
                styleTagName = "P";
            }
        }
        String styleId = this.tagNameStyleMapping.get(styleTagName);

        if (!"top".equals(valign)) {
            if ("middle".equals(valign)) {
                valign = "center";
            }
            CTVerticalJc vjc = factory.createCTVerticalJc();
            try {
                vjc.setVal(STVerticalJc.fromValue(valign));
                tcPr.setVAlign(vjc);
            }
            catch (Exception e) {}
        }

        // 宽度
        int width = el.attrInt("cell-width", 0);
        if (width > 0) {
            tcPr.setTcW(this.createWidth(width));
        }

        // 水平单元格跨越
        int colspan = el.attrInt("colspan", 1);
        if (colspan > 1) {
            GridSpan gridSpan = new GridSpan();
            gridSpan.setVal(BigInteger.valueOf(colspan));
            tcPr.setGridSpan(gridSpan);
        }

        // 垂直的单元格（虚格）
        int rowspan = el.attrInt("rowspan", 1);
        if (rowspan > 1) {
            VMerge vm = new VMerge();
            vm.setVal("restart");
            tcPr.setVMerge(vm);
        }
        // 就不需要内容了
        boolean vMerge = el.attrBoolean("v-merge");
        if (vMerge) {
            VMerge vm = new VMerge();
            tcPr.setVMerge(vm);
            P p = factory.createP();
            td.getContent().add(p);
            tr.getContent().add(td);
            return;
        }

        // 获取单元格段落样式:水平居中
        String align = el.attr("align");
        align = style.getString("text-align", align);

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

    private void joinTableRow(Tbl table, CheapElement el, List<CheapElement[]> grid, int rowI) {
        Tr tr = factory.createTr();

        CheapStyle style = el.getStyleObj();
        CheapSize ho = style.getSize("height", "0");
        int height = ho.getIntValue();
        if (height > 0) {
            TrPr trPr = factory.createTrPr();
            tr.setTrPr(trPr);
            CTHeight h = new CTHeight();
            h.setVal(BigInteger.valueOf(height * 10));

            List<JAXBElement<?>> div = trPr.getCnfStyleOrDivIdOrGridBefore();
            JAXBElement<CTHeight> jax = factory.createCTTrPrBaseTrHeight(h);
            div.add(jax);
        }

        // 根据修正过 col-span/row-span 的格子，输出单元格
        CheapElement[] cells = grid.get(rowI);
        for (CheapElement child : cells) {
            // Colspan 会导致grid里有空的占位元素
            if (null == child) {
                continue;
            }
            if (child.isStdTagName("TD") || child.isStdTagName("TH")) {
                joinTableCell(tr, child);
            }
        }
        table.getContent().add(tr);
    }

    private int getTableColumnsCount(CheapElement eTab) {
        List<CheapElement> trs = eTab.findElements(el2 -> el2.isStdTagName("TR"));
        // 首先得到最大格子数量
        int maxCol = 0;
        for (CheapElement tr : trs) {
            List<CheapElement> tds = tr.getChildElements(el2 -> el2.isStdTagName("TD"));
            int span = 0;
            for (CheapElement td : tds) {
                span += td.attrInt("colspan", 1);
            }
            maxCol = Math.max(span, maxCol);
        }
        return maxCol;
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

        // 得到表格最大列
        int colN = this.getTableColumnsCount(el);

        // 防守一个危险的
        if (colN <= 0) {
            return;
        }

        // 评估每列的宽度
        int[] colsW = this.evalTableColWidths(el, colN, pageTableWidth);

        // 针对单元格设置 rowSpan(vMerge (restart))
        List<CheapElement[]> grid = this.evalRowSpanAsVMerge(el, colN, colsW);

        // 设置表格宽度
        tPr.setTblW(createWidth(pageTableWidth));
        CTTblLayoutType clt = new CTTblLayoutType();
        clt.setType(STTblLayoutType.FIXED);
        tPr.setTblLayout(clt);
        // 设置单元格宽度
        TblGrid tGrid = factory.createTblGrid();
        for (int i = 0; i < colsW.length; i++) {
            int w = colsW[i];
            TblGridCol col = factory.createTblGridCol();
            col.setW(BigInteger.valueOf(w));
            tGrid.getGridCol().add(col);
        }
        table.setTblGrid(tGrid);

        // 设置单元格
        int rowI = 0;
        for (CheapElement child : el.getChildElements()) {
            // THEAD
            // TBODY
            if (child.isStdTagName("THEAD") || child.isStdTagName("TBODY")) {
                for (CheapElement c2 : child.getChildElements()) {
                    if (c2.isStdTagName("TR")) {
                        joinTableRow(table, c2, grid, rowI++);
                    }
                }
            }
            // TR
            else if (child.isStdTagName("TR")) {
                joinTableRow(table, child, grid, rowI++);
            }
        }
        partItems.add(table);
    }

    private int[] evalTableColWidths(CheapElement el, int maxCol, int tableWidth) {
        List<CheapElement> cols = el.findElements(el2 -> el2.isStdTagName("COL"));

        // 没有的话，尝试找到列最多的那一行
        if (cols.isEmpty()) {
            List<CheapElement> trs = el.findElements(el2 -> el2.isStdTagName("TR"));
            for (CheapElement tr : trs) {
                List<CheapElement> cells = tr.getChildElements(el2 -> el2.isStdTagName("TD"));
                if (cells.size() > cols.size()) {
                    cols = cells;
                }
            }
        }

        // 没指定？
        if (cols.isEmpty()) {
            int[] ws = new int[maxCol];
            int cellWidth = pageTableWidth / maxCol;
            int remainWidth = pageTableWidth - (cellWidth * maxCol);
            for (int i = 0; i < maxCol; i++) {
                int w = cellWidth;
                if (i == (maxCol - 1)) {
                    w += remainWidth;
                }
                ws[i] = w;
            }
            return ws;
        }

        // 指定了表格列宽度
        int[] ws = new int[cols.size()];
        int i = 0;
        for (CheapElement col : cols) {
            CheapSize wz = col.getStyleObj().getSize("width", null);
            if (null == wz) {
                wz = col.attrSize("width", null);
            }
            // 没有的话，均分
            if (null == wz) {
                ws[i++] = this.pageTableWidth / ws.length;
            }
            // 有的话，计算一下
            else {
                String unit = wz.getUnit();
                double w = -1;
                if ("%".equals(unit)) {
                    w = this.pageTableWidth * wz.getValue() / 100.0;
                } else if ("rem".equals(unit)) {
                    w = 100 * wz.getValue() * 6;
                } else {
                    w = wz.getValue() * 6;
                }
                ws[i++] = (int) (w);
            }
        }

        // 最后根据这些宽度，计算一个比例，然后根据 tableWidth 重新调整列宽
        // 如果不这么做， word 老版本，或者 wps 会不兼容
        double cellSumWidth = (double) Wnum.sum(ws);
        if (cellSumWidth > 0) {
            double[] cellWs = new double[ws.length];
            for (int x = 0; x < ws.length; x++) {
                double cellW = (double) ws[x];
                cellWs[x] = cellW / cellSumWidth;
            }
            int[] reWs = new int[ws.length];
            double dTableWidth = (double) tableWidth;
            for (int x = 0; x < ws.length; x++) {
                double s = cellWs[x];
                reWs[x] = (int) (dTableWidth * s);
            }
            return reWs;
        }

        // 直接返回吧
        return ws;
    }

    private List<CheapElement[]> evalRowSpanAsVMerge(CheapElement el, int maxCol, int[] colsW) {
        // 找到所有的行
        List<CheapElement> trs = el.findElements(el2 -> el2.isStdTagName("TR"));

        // 首先搞一个空白的二维数组 [TD][TR]
        List<CheapElement[]> grid = new ArrayList<>(trs.size());
        int y = 0;
        for (y = 0; y < trs.size(); y++) {
            CheapElement[] cells = new CheapElement[maxCol];
            Arrays.fill(cells, null);
            grid.add(cells);
        }

        // 开始向数组填充单元格
        y = 0;
        for (CheapElement tr : trs) {
            List<CheapElement> tds = tr.getChildElements(el2 -> el2.isStdTagName("TD"));
            CheapElement[] cells = grid.get(y);
            int x = 0;
            for (CheapElement td : tds) {
                int colspan = td.attrInt("colspan", 1);
                // 得到单元格应该的宽度
                int cellW = 0;
                if (null != colsW) {
                    for (int m = 0; m < colspan; m++) {
                        cellW += colsW[m + x];
                    }
                }
                if (cellW > 0) {
                    td.attr("cell-width", cellW);
                }

                // 搞 rowSpan
                int rowspan = td.attrInt("rowspan", 1);
                // 如果当前格子已经有东东了，向后移动
                while (null != cells[x] && x < maxCol) {
                    td.insertPrev(cells[x]);
                    x++;
                }
                cells[x] = td;
                // 搞 rowspan： 下面一排的单元格，全部填上虚格子
                if (rowspan > 1) {
                    for (int i = 1; i < rowspan; i++) {
                        int rowI = y + i;
                        if (rowI < grid.size()) {
                            CheapElement[] cells2 = grid.get(y + i);
                            CheapElement td2 = doc.createElement("TD");
                            if (cellW > 0) {
                                td2.attr("cell-width", cellW);
                            }
                            td2.attr("v-merge", true);
                            cells2[x] = td2;
                        }
                    }
                }
                // 跳过 colspan
                x += colspan;
            }
            // 指向下一行
            y++;
        }

        return grid;
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
        // 特殊控件：分页符
        if (el.hasClass("ti-doc-page-breaker")) {
            joinPageBreaker(partItems);
            return;
        }

        // 如果找到了样式映射，就不是简单的一层包裹咯
        // 因为这个可能是定制的一段文字段落
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

    private void joinPageBreaker(List<Object> partItems) {
        P p = factory.createP();
        // 属性
        PPr pPr = factory.createPPr();
        pPr.setWidowControl(new BooleanDefaultTrue());

        PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
        spacing.setLine(BigInteger.valueOf(240L));
        spacing.setLineRule(STLineSpacingRule.AUTO);
        pPr.setSpacing(spacing);

        PPrBase.Ind ind = factory.createPPrBaseInd();
        ind.setFirstLine(BigInteger.ZERO);
        ind.setFirstLineChars(BigInteger.ZERO);
        pPr.setInd(ind);

        Jc jc = factory.createJc();
        jc.setVal(JcEnumeration.LEFT);
        pPr.setJc(jc);

        // 分页符
        R r = factory.createR();
        Br br = factory.createBr();
        br.setType(STBrType.PAGE);
        r.getContent().add(br);
        p.setPPr(pPr);
        p.getContent().add(r);

        // 计入
        partItems.add(p);
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
                    DocxElStyle es = new DocxElStyle();
                    re |= appendInline(pContent, (CheapElement) node, es);
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
                U u = factory.createU();
                u.setVal(UnderlineEnumeration.SINGLE);
                rPr.setU(u);
            }
            if (null != es.fontFamily) {
                RFonts rf = factory.createRFonts();
                rf.setAscii(es.fontFamily);
                rf.setEastAsia(es.fontFamily);
                rf.setHAnsi(es.fontFamily);
                rf.setHint(STHint.EAST_ASIA);
                rPr.setRFonts(rf);
            }
            int fsVal = es.getFontSizeValue();
            if (fsVal > 0) {
                HpsMeasure hm = factory.createHpsMeasure();
                hm.setVal(BigInteger.valueOf(fsVal));
                rPr.setSz(hm);
                HpsMeasure hmCs = factory.createHpsMeasure();
                hmCs.setVal(BigInteger.valueOf(fsVal));
                rPr.setSzCs(hmCs);
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
        es = null == es ? new DocxElStyle() : es.clone();
        es.bold = true;
        es.updateByElement(el);
        return dispatchInlineChildren(pContent, el, es);
    }

    private boolean appendItalic(List<Object> pContent, CheapElement el, DocxElStyle es) {
        es = null == es ? new DocxElStyle() : es.clone();
        es.italic = true;
        es.updateByElement(el);
        return dispatchInlineChildren(pContent, el, es);
    }

    private boolean appendUnderline(List<Object> pContent, CheapElement el, DocxElStyle es) {
        es = null == es ? new DocxElStyle() : es.clone();
        es.underline = true;
        es.updateByElement(el);
        return dispatchInlineChildren(pContent, el, es);
    }

    private boolean appendSpan(List<Object> pContent, CheapElement el, DocxElStyle es) {
        es = null == es ? new DocxElStyle() : es.clone();
        es.updateByElement(el);
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

        // 处理文档最后一个编号设定
        if (!this._a_num.isEmpty()) {
            this.numbering.addNum(listNumId, _a_num);
            _a_num.reset();
        }

        // 设置列表编号的配置
        if (!numbering.isEmpty()) {
            String markup = numbering.toMarkup();
            NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
            Numbering numbering = (Numbering) XmlUtils.unmarshalString(markup);
            ndp.setJaxbElement(numbering);
            wp.getMainDocumentPart().addTargetPart(ndp);
        }

        // 最后保存
        return wp;
    }

    public WordprocessingMLPackage getWpPackage() {
        return wp;
    }

}
