package com.site0.walnut.ext.media.ooml.explain;

import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Encoding;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.dom.CheapNode;
import com.site0.walnut.ext.media.ooml.explain.bean.OECopyNode;
import com.site0.walnut.ext.media.ooml.explain.bean.OECopyText;
import com.site0.walnut.ext.media.ooml.explain.bean.OEDeepCopyNode;
import com.site0.walnut.ext.media.ooml.explain.bean.OEHyper;
import com.site0.walnut.ext.media.ooml.explain.bean.OENode;
import com.site0.walnut.ext.media.ooml.explain.bean.OEPicture;
import com.site0.walnut.ext.media.ooml.explain.bean.OEPlaceholder;
import com.site0.walnut.ooml.OomlEntry;
import com.site0.walnut.ooml.OomlPackage;
import com.site0.walnut.ooml.OomlRel;
import com.site0.walnut.ooml.OomlRels;
import com.site0.walnut.util.Ws;

public class WrStack {

    private OomlPackage ooml;

    private OomlEntry entry;

    private StringBuilder sb;

    private LinkedList<StackItem> items;

    private OEHyper hyper;

    public WrStack(OomlPackage ooml, OomlEntry entry) {
        this.ooml = ooml;
        this.entry = entry;
        this.clear();
    }

    static Pattern P_V = Pattern.compile("\\$\\{([^?}]+)(\\?([^}]*))?\\}");

    private OECopyNode tryDrawing(CheapElement r) {
        CheapElement drawing = r.getFirstChildElement("w:drawing");
        if (null == drawing) {
            return null;
        }
        // 找到 Link
        CheapElement hlink = drawing.findElement(el -> {
            return el.isTagName("a:hlinkClick");
        });
        if (null == hlink) {
            return null;
        }
        // 找到链接目标
        String rId = hlink.attr("r:id");
        OomlRels rels = ooml.loadRelationships(entry);
        OomlRel rel = rels.get(rId);
        if (null == rel) {
            return null;
        }
        String target = rel.getTarget();
        if (null == target || !target.startsWith("=")) {
            return null;
        }
        String varName = target.substring(1).trim();

        OEPicture pic = new OEPicture();
        pic.setVarName(varName);
        pic.setRefer(drawing);

        // 生成返回
        OECopyNode cr = new OECopyNode();
        cr.setRefer(r);

        CheapElement rPr = r.getFirstChildElement("w:rPr");
        if (null != rPr) {
            OECopyNode crPr = new OECopyNode();
            crPr.setRefer(rPr);
            cr.addChild(crPr);
        }
        cr.addChild(pic);

        return cr;
    }

    private OEHyper tryHyper2(OENode pNode, CheapElement r) {
        if (r.isTagName("w:hyperlink")) {
            // 找到链接目标
            String rId = r.attr("r:id");
            OomlRels rels = ooml.loadRelationships(entry);
            OomlRel rel = rels.get(rId);
            if (null == rel) {
                return null;
            }
            String target = rel.getTarget();
            if (null == target || !target.startsWith("=")) {
                return null;
            }
            String altText = target.substring(1).trim();
            String tooltip = r.attr("w:tooltip");
            tooltip = Ws.decodeHtmlEntities(tooltip);
            OEHyper hy = new OEHyper();
            hy.setMatchInput(tooltip);
            hy.setAltText(altText);

            // 继续寻找要显示的文字
            List<CheapElement> els = r.getChildElements(child -> child.isTagName("w:r"));
            for (CheapElement el : els) {
                hy.addMoreRun(el);
            }
            return hy;
        }
        return null;
    }

    private boolean tryHyper(OENode pNode, CheapElement r) {
        CheapElement fldChar = r.getFirstChildElement("w:fldChar");
        String fcType = null;
        if (null != fldChar) {
            fcType = fldChar.attr("w:fldCharType");
        }
        // 如果已经在超链里，等到结束
        if (null != hyper) {
            // 结束
            if ("end".equals(fcType)) {
                pNode.addChild(hyper);
                this.hyper = null;
                return true;
            }
            // 超链
            CheapElement instrText = r.getFirstChildElement("w:instrText");
            if (null != instrText) {
                String text = instrText.getText();
                text = Ws.decodeHtmlEntities(text);
                text = URLDecoder.decode(text, Encoding.CHARSET_UTF8);

                // 尝试赋值
                if (hyper.setMatchAndAltText(text)) {
                    return true;
                }

                // 回退为普通超链
                items.add(gen_item(hyper.getRefer()));
                if (hyper.hasMoreRuns()) {
                    for (CheapElement mr : hyper.getMoreRuns()) {
                        items.add(gen_item(mr));
                    }
                }
                items.add(gen_item(r));
                this.hyper = null;
                return true;
            }
            // 分割以及其他普通run
            hyper.addMoreRun(r);
            return true;
        }
        // 开始一个超链
        else if ("begin".equals(fcType)) {
            // 清理之前
            this.joinAllAndClear(pNode);
            // 建立一个对象，同时也收集项目
            // 以便普通超链接回退
            this.hyper = new OEHyper();
            this.hyper.setRefer(r);
            return true;
        }
        // 其他，不接受
        return false;
    }

    public void push(OENode pNode, CheapElement r) {
        // 图像导致清栈
        OECopyNode drawing = tryDrawing(r);
        if (null != drawing) {
            this.joinAllAndClear(pNode);
            pNode.addChild(drawing);
            return;
        }
        // 超链导致清栈
        OEHyper hy = tryHyper2(pNode, r);
        if (null != hy) {
            this.joinAllAndClear(pNode);
            pNode.addChild(hy);
            return;
        }
        // 超链导致清栈
        if (tryHyper(pNode, r)) {
            return;
        }

        // 其普通元素压栈后看占位符
        StackItem it = gen_item(r);

        // 拿到字符偏移并记入
        sb.append(it.str);
        items.addLast(it);

        // 看看是否有发现，如果发现了匹配，则尝试清空栈后，
        // 增加一个占位变量节点。
        // 1/12 Regin:0/13
        // 0:[ 1, 12) `${name?dft}`
        // 1:[ 3, 7) `name`
        // 2:[ 7, 11) `?dft`
        // 3:[ 8, 11) `dft`
        while (true) {
            Matcher m = P_V.matcher(sb);
            if (!m.find()) {
                break;
            }
            int pS = m.start();
            int pE = m.end();

            // 记入之前节点
            CheapElement rPr = this.joinToAndRemove(pNode, pS, pE);

            // 自己是一个占位
            OEPlaceholder ph = new OEPlaceholder();
            ph.setRefer(rPr);
            ph.setVarName(Ws.trim(m.group(1)));
            ph.setDftValue(Ws.trim(m.group(3)));
            pNode.addChild(ph);
        }
    }

    /**
     * @param pNode
     * @param pS
     *            占位符开始
     * @param pE
     *            占位符结束
     */
    private CheapElement joinToAndRemove(OENode pNode, int pS, int pE) {
        FindRe frS = findPos(pS);
        FindRe frE = findPos(pE - 1);

        int I = 0;
        CheapElement re;

        // 到占位符 $ 之前，都要复制
        for (; I < frS.index; I++) {
            StackItem it = items.removeFirst();
            it.joinToNode(pNode);
        }

        // $ 最后一项目是否还有静态字符
        StackItem it = items.peekFirst();
        if (frS.offset > 0) {
            String s0 = it.str.substring(0, frS.offset);
            String s1 = it.str.substring(frS.offset);
            it.str = s0;
            it.joinToNode(pNode);
            // 切换剩余, 这样当前栈顶，第一个字符就是 $
            it.str = s1;
        }
        re = it.r.getFirstChildElement("w:rPr");

        // 到 } 之前全部删除
        for (; I < frE.index; I++) {
            items.removeFirst();
        }

        // } 前最后一项是否还余留静态字符
        it = items.removeFirst();
        int eOff = frE.offset + 1;
        if (it.str.length() > eOff) {
            it.str = it.str.substring(eOff);
            items.addFirst(it);
        }

        // 同步一下字符缓冲
        this.resetStrBuf();

        return re;
    }

    void resetStrBuf() {
        this.sb = new StringBuilder();
        for (StackItem it : items) {
            this.sb.append(it.str);
        }
    }

    public void joinAllAndClear(OENode pNode) {
        for (StackItem it : items) {
            it.joinToNode(pNode);
        }
        this.clear();
    }

    void clear() {
        this.items = new LinkedList<>();
        this.sb = new StringBuilder();
    }

    static class FindRe {
        int index;

        /**
         * 最后一项匹配的字符偏移。 因此，index 之前的项字长，再加上这个偏移， 就是字符在 sb 中的位置
         */
        int offset;
    }

    /**
     * @param n
     *            字符缓冲中的字符数
     * @return {index:所在项下标, offset:所在项偏移}
     */
    FindRe findPos(int n) {
        int off = 0;
        int index = -1;
        for (StackItem it : items) {
            index++;
            int end = off + it.str.length();
            if (off <= n && end > n) {
                FindRe re = new FindRe();
                re.index = index;
                re.offset = n - off;
                return re;
            }
            off = end;
        }
        return null;
    }

    private StackItem gen_item(CheapElement el) {
        StackItem it = new StackItem();
        it.r = el;
        CheapElement t = el.getFirstChildElement("w:t");
        if (null == t) {
            it.str = "";
        } else {
            it.str = t.getText();
        }
        return it;
    }

    private static class StackItem {
        CheapElement r;
        String str;

        public String toString() {
            return String.format("【%s】 %s", str, r.toBrief());
        }

        void genNodeForElement(OENode pNode, CheapNode node) {
            if (node.isElement()) {
                CheapElement el = (CheapElement) node;
                // 属性复制
                if (el.isTagAs("^(w:rPr)$")) {
                    OEDeepCopyNode cp = OEDeepCopyNode.create(el);
                    pNode.addChild(cp);
                    return;
                }
                //
                // 其他节点递归复制
                //
                OECopyNode t = new OECopyNode();
                t.setRefer(el);
                pNode.addChild(t);
                if (el.isTagName("w:t")) {
                    OECopyText ct = new OECopyText();
                    ct.setText(str);
                    t.addChild(ct);
                }
                // 其他节点
                else if (el.hasChildren()) {
                    for (CheapNode child : el.getChildren()) {
                        genNodeForElement(t, child);
                    }
                }
            }
            // 文本
            else if (node.isText()) {
                String txt = node.getText();
                OECopyText ct = new OECopyText();
                ct.setText(txt);
                pNode.addChild(ct);
            }
        }

        void joinToNode(OENode pNode) {
            this.genNodeForElement(pNode, r);
        }

    }

}
