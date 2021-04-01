package org.nutz.walnut.cheap.dom.docx;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.PPrBase.NumPr.NumId;
import org.docx4j.wml.PPrBase.PStyle;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.nutz.walnut.api.err.Er;
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

    /**
     * 元素（特别是<code>H1~6</cod>），应该应对到哪个 w:style 呢？<br>
     * 元素（特别是<code>DIV</cod>）采用了特别的 className，应该应对到哪个 w:style 呢？
     * 文档头部元数据（特别是<code>META</code> 制作的页头/脚应该应对哪个 w:style 呢？
     * <p>
     * 这里是一个映射表:
     * 
     * <pre>
     * {
     *    "${tagName}" : "${styleId}",
     *    ".${className}" : "${styleId}",
     *    "@${metaName}" : "${styleId}",
     * }
     * 譬如
     * {
     *    "H1" : "10",
     *    "H2" : "20",
     *    ".my-title" : "ab",
     *    "@doc-code" : "a0",
     *    "@doc-name" : "a1",
     * }
     * </pre>
     */
    private Map<String, String> styleMapping;

    private ObjectFactory factory;

    private Map<String, CheapResource> resources;

    /* 采用奇数 */
    private int _seq_id1;
    /* 采用偶数 */
    private int _seq_id2;

    public CheapDocxRendering(CheapDocument doc,
                              WordprocessingMLPackage wp,
                              Map<String, String> styleMapping,
                              CheapResourceLoader loader) {
        this.doc = doc;
        this.wp = wp;
        this.styleMapping = null == styleMapping ? new HashMap<>() : styleMapping;
        this.loader = loader;
        this.factory = new ObjectFactory();

        this.resources = new HashMap<>();
        this._seq_id1 = 1;
        this._seq_id2 = 2;
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
        String styleId = styleMapping.get("BLOCKQUOTE");
        P p = factory.createP();
        if (null != styleId) {
            setPStyle(p, styleId);
        }
        if (appendBlockChildren(p, el)) {
            partItems.add(p);
        }
    }

    private void setPStyle(P p, String styleId) {
        PPr pPr = factory.createPPr();
        PStyle pStyle = factory.createPPrBasePStyle();
        pStyle.setVal(styleId);
        pPr.setPStyle(pStyle);
        p.setPPr(pPr);
    }

    private void joinHeading(List<Object> partItems, CheapElement el) {
        String styleId = styleMapping.get(el.getStdTagName());
        P p = factory.createP();
        if (null != styleId) {
            setPStyle(p, styleId);
        }
        if (appendBlockChildren(p, el)) {
            partItems.add(p);
        }
    }

    private void joinP(List<Object> partItems, CheapElement el) {
        P p = factory.createP();
        if (appendBlockChildren(p, el)) {
            partItems.add(p);
        }
    }

    private int listLvl = 0;

    private int listNumId = 0;

    private void joinLi(List<Object> partItems, CheapElement el) {
        // 搞自己
        P p = factory.createP();

        // 增加自己的属性
        PPr ppr = factory.createPPr();
        p.setPPr(ppr);

        // Create and add <w:numPr>
        NumPr numPr = factory.createPPrBaseNumPr();
        ppr.setNumPr(numPr);

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
        for (CheapNode child : el.getChildren()) {
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

        // 依次搞单元格内容
        List<Object> tdContent = td.getContent();

        for (CheapNode node : el.getChildren()) {
            this.dispatchBlock(tdContent, node);
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
            if (child.isTagName("TD") || child.isTagName("TH")) {
                joinTableCell(tr, child);
            }
        }
        table.getContent().add(tr);
    }

    private void joinTable(List<Object> partItems, CheapElement el) {
        Tbl table = factory.createTbl();
        for (CheapElement child : el.getChildElements()) {
            // THEAD
            // TBODY
            if (child.isTagName("THEAD") || child.isTagName("TBODY")) {
                for (CheapElement c2 : child.getChildElements()) {
                    if (c2.isTagName("TR")) {
                        joinTableRow(table, c2);
                    }
                }
            }
            // TR
            else if (child.isTagName("TR")) {
                joinTableRow(table, child);
            }
        }
        partItems.add(table);
    }

    private void joinDiv(List<Object> partItems, CheapElement el) {
        // 如果找到了样式映射，就不是简单的一层包裹咯
        String styleId = null;
        if (el.hasClassName()) {
            // 整体映射
            styleId = this.styleMapping.get(el.getClassName());
            // 某个类选择器
            if (null == styleId) {
                for (String cn : el.getClassList()) {
                    styleId = styleMapping.get(cn);
                    if (null != styleId)
                        break;
                }
            }
        }

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
        List<Object> pContent = p.getContent();
        boolean re = false;
        for (CheapNode node : el.getChildren()) {
            // 文本节点
            if (node.isText()) {
                re |= appendText(pContent, (CheapText) node, null);
            }
            // 行内元素
            else if (node.isElement()) {
                re |= appendInline(pContent, (CheapElement) node, null);
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
        t.setValue(txt.getText());
        r.getContent().add(t);
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

        // 处理图像
        if (el.isTagName("IMG")) {
            re |= appendImage(pContent, el);
        }
        // 处理加粗
        else if (el.isTagName("STRONG") || el.isTagName("B")) {
            re |= appendBold(pContent, el, es);
        }
        // 处理斜体
        else if (el.isTagName("EM") || el.isTagName("I")) {
            re |= appendItalic(pContent, el, es);
        }
        // 处理下划线
        else if (el.isTagName("U")) {
            re |= appendUnderline(pContent, el, es);
        }
        // 其他的递归处理文字
        else {
            re |= dispatchInlineChildren(pContent, el, es);
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
        if (!el.hasChildren())
            return;

        // 处理 DIV
        if (el.isTagName("DIV")) {
            joinDiv(partItems, el);
        }
        // 处理标题
        else if (el.isTagAs("^(H[1-6])$")) {
            joinHeading(partItems, el);
        }
        // 处理段落
        else if (el.isTagName("P")) {
            joinP(partItems, el);
        }
        // 处理表格
        else if (el.isTagName("TABLE")) {
            joinTable(partItems, el);
        }
        // 处理无序列表
        else if (el.isTagName("UL")) {
            joinUl(partItems, el);
        }
        // 处理有序列表
        else if (el.isTagName("OL")) {
            joinOl(partItems, el);
        }
        // 处理引用
        else if (el.isTagName("BLOCKQUOTE")) {
            joinBlockquote(partItems, el);
        }
    }

    public WordprocessingMLPackage render() throws Exception {
        // 渲染文档头（页眉和页脚）
        // TODO ...

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
