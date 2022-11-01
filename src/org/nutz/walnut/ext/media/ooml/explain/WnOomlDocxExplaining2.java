package org.nutz.walnut.ext.media.ooml.explain;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Encoding;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.cheap.api.CheapResourceLoader;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.ext.media.ooml.api.OomlExplaining;
import org.nutz.walnut.ext.media.ooml.explain.bean.OEBranch;
import org.nutz.walnut.ext.media.ooml.explain.bean.OECondition;
import org.nutz.walnut.ext.media.ooml.explain.bean.OECopyNode;
import org.nutz.walnut.ext.media.ooml.explain.bean.OEDeepCopyNode;
import org.nutz.walnut.ext.media.ooml.explain.bean.OEItem;
import org.nutz.walnut.ext.media.ooml.explain.bean.OELoop;
import org.nutz.walnut.ext.media.ooml.explain.bean.OENode;
import org.nutz.walnut.ext.media.ooml.explain.bean.OENodeType;
import org.nutz.walnut.ooml.OomlEntry;
import org.nutz.walnut.ooml.OomlPackage;
import org.nutz.walnut.ooml.Oomls;
import org.nutz.walnut.util.Ws;

public class WnOomlDocxExplaining2 implements OomlExplaining {

    private OomlPackage ooml;

    private CheapResourceLoader loader;

    private OomlEntry entry; // 解析时，实体

    private CheapElement currentEL; // 解析时，当前的 el

    private OENode pNode;

    public WnOomlDocxExplaining2(OomlPackage ooml, CheapResourceLoader loader) {
        this.ooml = ooml;
        this.loader = loader;
    }

    /**
     * 假设 pNode 与 currentEL 成对指向刚刚被解析的节点。
     * <p>
     * 本函数将寻找并设定下一个要设定的元素。并准备好正确的 pNode.
     * <p>
     * 调用 parseCurrent 后，又会回到对齐状态。
     * 
     * @param canEnterNode
     *            是否会主动尝试递归子节点
     * 
     * @return true 可以继续解析， false 表示没有可以解析的节点了
     */
    private boolean prepareParseNext(boolean canEnterNode) {
        // 防守
        if (null == currentEL || null == pNode) {
            return false;
        }
        CheapElement el;
        // 获取当前节点下一个节点
        if (canEnterNode) {
            el = currentEL.getFirstChildElement();
            // 进入
            if (null != el) {
                this.currentEL = el;
                return true;
            }
        }
        // 下一个
        el = currentEL.getNextElement();
        if (null != el) {
            // 防守
            if (!pNode.hasParent()) {
                return false;
            }
            this.currentEL = el;
            if (!pNode.isVirtualNode()) {
                this.pNode = pNode.getParent();
            }
            return true;
        }
        // 退回一级，并下一个
        do {
            currentEL = currentEL.parentElement();
            pNode = pNode.getParent();
            if (null == currentEL || null == pNode) {
                return false;
            }
            // 下一个
            el = currentEL.getNextElement();
        } while (null == el);
        // 防守
        if (!pNode.hasParent()) {
            return false;
        }
        this.currentEL = el;
        if (!pNode.isVirtualNode()) {
            this.pNode = pNode.getParent();
        }
        return true;
    }

    private static final String R3 = "#\\{"
                                     + "\\s*(loop|if|else-if|else|end)\\s*"
                                     + "(\\s*@\\s*(.*))?"
                                     + "\\s*\\}";
    private static final Pattern P3 = Pattern.compile(R3);

    static class ParseResult {
        OENode next;
        boolean doneForCurrent;

        ParseResult(OENode next, boolean doneForCurrent) {
            this.next = next;
            this.doneForCurrent = doneForCurrent;
        }
    }

    private ParseResult parseCurrent(OENode pNode, CheapElement el) {
        boolean isP = el.isTagName("w:p");
        boolean isTr = el.isTagName("w:tr");

        // 判断 <w:p> | <w:tr> 是否为循环或者分支的开头
        // 如果是，则开启一个对应的渲染节点，并递归
        if (isP || isTr) {
            String str = Ws.trim(el.getText());
            // 0/21 Regin:0/21
            // 0:[ 0, 21) `#{loop @ it,I : list}`
            // 1:[ 2, 6) `loop`
            // 2:[ 7, 20) `@ it,I : list`
            // 3:[ 9, 20) `it,I : list`
            Matcher m = P3.matcher(str);
            if (m.find()) {
                String type = m.group(1);
                String more = m.group(3);
                // 开启循环
                if ("loop".equals(type)) {
                    return __parse_as_loop(pNode, el, more);
                }
                // 开启分支
                else if ("if".equals(type)) {
                    return __parse_as_if(pNode, el, more);
                }
                // 加入分支
                else if ("else-if".equals(type)) {
                    return __parse_as_else_if(pNode, el, more);
                }
                // 加入默认分支
                else if ("else".equals(type)) {
                    return __parse_as_else(pNode, el);
                }
                // 结束循环或者分支
                else if ("end".equals(type)) {
                    return __parse_as_end(pNode);
                }
            }
        }

        // 如果是一个 <w:p> 则开启分析栈
        if (isP) {
            return __parse_as_p(pNode, el);
        }
        // 其他的，无脑 copy
        return __parse_as_copy(pNode, el);
    }

    private ParseResult __parse_as_end(OENode pNode) {
        // 寻找到最近的分支或循环
        OENode p = pNode;
        while (null != p) {
            if (p.isType(OENodeType.BRANCH)) {
                break;
            }
            if (p.isType(OENodeType.LOOP)) {
                break;
            }
            p = p.getParent();
        }

        return new ParseResult(p.getParent(), true);
    }

    private ParseResult __parse_as_else(OENode pNode, CheapElement el) {
        // 寻找到最近的分支
        OENode p = pNode;
        while (null != p && !p.isType(OENodeType.BRANCH)) {
            p = p.getParent();
        }
        OEBranch br;

        // 并没有分支，创建一个
        if (null == p) {
            br = new OEBranch();
            pNode.addChild(br);
        } else {
            br = (OEBranch) p;
        }

        // 并加入
        OECondition cond = new OECondition();
        cond.setAsDefaultBranch();
        br.addBranch(cond);

        return new ParseResult(cond, true);
    }

    private ParseResult __parse_as_else_if(OENode pNode, CheapElement el, String more) {
        // 寻找到最近的分支
        OENode p = pNode;
        while (null != p && !p.isType(OENodeType.BRANCH)) {
            p = p.getParent();
        }

        // 并没有分支，创建一个
        if (null == p) {
            return __parse_as_if(pNode, el, more);
        }

        // 并加入
        OEBranch br = (OEBranch) p;
        OECondition cond = new OECondition();
        cond.setMatch(more);
        br.addBranch(cond);

        return new ParseResult(cond, true);
    }

    private ParseResult __parse_as_if(OENode pNode, CheapElement el, String more) {
        OEBranch br = new OEBranch();
        pNode.addChild(br);
        OECondition cond = new OECondition();
        cond.setMatch(more);
        br.addBranch(cond);

        return new ParseResult(cond, true);
    }

    private static final String R4 = "\\s*([^,]+)"
                                     + "(\\s*,\\s*([^:\t ]+))?"
                                     + "\\s*:"
                                     + "\\s*([^:\\t ]+)";
    private static final Pattern P4 = Pattern.compile(R4);

    private ParseResult __parse_as_loop(OENode pNode, CheapElement el, String more) {
        // 0/11 Regin:0/11
        // 0:[ 0, 11) `it,I : list`
        // 1:[ 0, 2) `it`
        // 2:[ 2, 4) `,I`
        // 3:[ 3, 4) `I`
        // 4:[ 7, 11) `list`
        Matcher m = P4.matcher(more);
        if (!m.find()) {
            throw Er.create("e.ooml.tmpl.InvalidLoop", more);
        }
        OELoop loop = new OELoop();
        loop.setVarName(m.group(1));
        loop.setKeyName(m.group(3));
        loop.setLoopWith(m.group(4));
        pNode.addChild(loop);

        return new ParseResult(loop, true);
    }

    private ParseResult __parse_as_p(OENode pNode, CheapElement el) {
        // 父节点
        OECopyNode cp = OECopyNode.create(el);
        pNode.addChild(cp);

        // 复制属性
        CheapElement pPr = el.getFirstChildElement("w:pPr");
        if (null != pPr) {
            OEDeepCopyNode cppr = OEDeepCopyNode.create(pPr);
            cp.addChild(cppr);
        }

        // 分析栈
        WrStack rStack = new WrStack(ooml, this.entry);

        // 循环处理
        List<CheapElement> runs = el.getChildElements(ch -> ch.isTagName("w:r"));
        for (CheapElement r : runs) {
            rStack.push(cp, r);
        }

        // 清理残余
        rStack.joinAllAndClear(cp);

        // 当前节点
        return new ParseResult(cp, true);
    }

    private ParseResult __parse_as_copy(OENode pNode, CheapElement el) {
        OECopyNode cp = OECopyNode.create(el);
        pNode.addChild(cp);
        return new ParseResult(cp, false);
    }

    private void processXML(OomlEntry en, NutBean vars) {
        // 加载实体对应的 XML
        CheapDocument doc = parseEntryDocument(en);

        // 解析
        OENode rootNode = prepareRenderNode(doc);

        // 渲染
        CheapDocument out = new CheapDocument(null);
        CheapElement outRoot = doc.root().cloneSelf();
        out.setRootElement(outRoot);
        for (OEItem it : rootNode.getChildren()) {
            it.renderTo(outRoot, vars);
        }

        // 保存实体内容
        String xml = out.toMarkup();
        byte[] buf = xml.getBytes(Encoding.CHARSET_UTF8);
        en.setContent(buf);
    }

    public CheapDocument parseEntryDocument(OomlEntry en) {
        this.entry = en;
        CheapDocument doc = Oomls.parseEntryAsXml(en);
        return doc;
    }

    public OENode prepareRenderNode(CheapDocument doc) {
        // 从根节点开始处理
        currentEL = doc.root();
        pNode = OECopyNode.create(currentEL);
        OENode rootNode = pNode;
        boolean canEnterNode = true;
        while (this.prepareParseNext(canEnterNode)) {
            ParseResult re = parseCurrent(this.pNode, this.currentEL);
            this.pNode = re.next;
            canEnterNode = !re.doneForCurrent;
        }

        // 必要的设置
        rootNode.setLoader(loader);
        rootNode.setOoml(ooml);
        rootNode.setEntry(entry);
        return rootNode;
    }

    @Override
    public void explain(NutBean vars) {
        // 处理主文档
        OomlEntry en = ooml.getEntry("word/document.xml");
        this.processXML(en, vars);

        // 处理页眉
        List<OomlEntry> list = ooml.findEntriesByPath("^word/header\\d+.xml$");
        for (OomlEntry li : list) {
            this.processXML(li, vars);
        }

        // 处理页脚
        list = ooml.findEntriesByPath("^word/footer\\d+.xml$");
        for (OomlEntry li : list) {
            this.processXML(li, vars);
        }

        // 将修改的缓存内容(rels/ contentTypes) 等，写入到实体集合里
        ooml.saveAllRelationshipsFromCache();
        ooml.saveContentTypes();
    }

}
