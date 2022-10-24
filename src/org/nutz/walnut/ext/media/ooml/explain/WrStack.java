package org.nutz.walnut.ext.media.ooml.explain;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapNode;
import org.nutz.walnut.ext.media.ooml.explain.bean.OECopyNode;
import org.nutz.walnut.ext.media.ooml.explain.bean.OECopyText;
import org.nutz.walnut.ext.media.ooml.explain.bean.OENode;
import org.nutz.walnut.ext.media.ooml.explain.bean.OEPicture;
import org.nutz.walnut.ext.media.ooml.explain.bean.OEPlaceholder;
import org.nutz.walnut.ooml.OomlEntry;
import org.nutz.walnut.ooml.OomlPackage;
import org.nutz.walnut.ooml.OomlRel;
import org.nutz.walnut.ooml.OomlRels;
import org.nutz.walnut.util.Ws;

public class WrStack {

    private OomlPackage ooml;

    private OomlEntry entry;

    private StringBuilder sb;

    private LinkedList<StackItem> items;

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

    public void push(OENode pNode, CheapElement r) {
        // 图像or超链导致清栈

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
        Matcher m = P_V.matcher(sb);
        if (m.find()) {
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
    CheapElement joinToAndRemove(OENode pNode, int pS, int pE) {
        FindRe frS = findPos(pS);
        FindRe frE = findPos(pE);

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
        if (it.str.length() > frE.offset) {
            it.str = it.str.substring(frE.offset);
            items.addFirst(it);
        }

        return re;
    }

    void joinAllAndClear(OENode pNode) {
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

        void genNodeForElement(OENode pNode, CheapNode node) {
            if (node.isElement()) {
                CheapElement el = (CheapElement) node;
                OECopyNode t = new OECopyNode();
                t.setRefer(el);
                pNode.addChild(t);
                if (el.isTagName("w:t")) {
                    OECopyText ct = new OECopyText();
                    ct.setText(str);
                    t.addChild(ct);
                }
                // 其他节点
                else {
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
