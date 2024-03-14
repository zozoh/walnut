package com.site0.walnut.ext.media.ooml.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Encoding;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.cheap.api.CheapResourceLoader;
import com.site0.walnut.cheap.dom.CheapDocument;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.dom.CheapNode;
import com.site0.walnut.cheap.dom.bean.CheapResource;
import com.site0.walnut.ext.media.ooml.api.OomlExplaining;
import com.site0.walnut.ooml.OomlContentTypes;
import com.site0.walnut.ooml.OomlEntry;
import com.site0.walnut.ooml.OomlPackage;
import com.site0.walnut.ooml.OomlRel;
import com.site0.walnut.ooml.OomlRels;
import com.site0.walnut.ooml.Oomls;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.each.WnEachIteratee;

public class WnOomlDocxExplaining implements OomlExplaining {

    private OomlPackage ooml;

    private CheapResourceLoader loader;

    public WnOomlDocxExplaining(OomlPackage ooml, CheapResourceLoader loader) {
        this.ooml = ooml;
        this.loader = loader;
    }

    private boolean tryAsShape(CheapElement el, NutBean vars, OomlEntry en) {
        if (!el.isStdTagName("V:SHAPE")) {
            return false;
        }
        // 1. 判断是否有属性 alt="=变量名"
        String alt = Ws.trim(el.attr("alt"));
        if (null == alt || !alt.startsWith("=")) {
            return false;
        }
        // 加载资源，如果确保资源在系统中存在
        String varName = alt.substring(1).trim();
        String objPath = vars.getString(varName);
        if (null == objPath) {
            return false;
        }
        CheapResource img = this.loader.loadByPath(objPath);
        if (null == img) {
            return false;
        }

        // 2. 找到子节点 `<v:imagedata>` 得到 `r:id` 属性
        CheapElement vid = el.getFirstChildElement("v:imagedata");
        if (null == vid) {
            return false;
        }
        String rId = vid.attr("r:id");
        if (null == rId) {
            return false;
        }

        // 3. 根据 rId 在 document.xml.rels 文件中找到 <Relationship>，
        // 并得到对应的图片路径 media/image1.png
        OomlRels rels = ooml.loadRelationships(en);
        OomlRel rel = rels.get(rId);
        String imgPh = rel.getTarget();
        String imgRph = rels.getUniqPath(imgPh);

        // 4. 找到这个图片所在的条目
        OomlEntry enImg = ooml.getEntry(imgRph);

        // 5. 直接向其写入嵌入图片
        enImg.setContent(img.getContent());

        // 6. 如果图片的扩展名与嵌入图片不一致
        String imgType = img.getSuffixName();
        if ("jpg".equals(imgType)) {
            imgType = "jpeg";
        }
        if (!rel.isTargetType(imgType)) {
            // 7. 将其后缀名进行修改
            enImg.renameSuffix(imgType);
            rel.renameSuffix(imgType);

            // 8. 确保 [Content_Types].xml 文件中声明了这个扩展名所对应的 MIME 类型
            OomlContentTypes oct = ooml.loadContentTypes();
            if (!oct.getDefaults().has(imgType)) {
                String mime = loader.getMime(imgType);
                oct.getDefaults().put(imgType, mime);
            }
        }

        // 修改图片的 title
        CheapElement elImgData = el.getFirstChildElement("v:imagedata");
        if (null != elImgData) {
            String title = Ws.sBlank(img.getAlt(), img.getName());
            if (!Ws.isBlank(title)) {
                elImgData.attr("o:title", title);
            }
        }
        return true;
    }

    /**
     * <pre>
     * 0: "${变量名#loop-begin:it}"
     * 1: "变量名"
     * 2: "#loop-begin"
     * 3: "loop-begin"
     * 4: "begin"
     * 5: ":it"
     * 6: "it"
     * </pre>
     */
    private final Pattern PP = Pattern.compile("^[$][{]([^}#:]*)(#(loop-(begin|end)))(:([^}]+))?[}]$");

    private OomlWPlaceholder getLoopPlaceholder(CheapElement el) {
        String str = Ws.trim(el.getText());
        if (null == str) {
            return null;
        }
        OomlWPlaceholder ph = null;
        Matcher m = PP.matcher(str);
        if (m.find()) {
            String varName = m.group(1);
            String typeName = m.group(3);
            String itemName = m.group(6);
            ph = new OomlWPlaceholder();
            ph.setName(varName);
            ph.setType(typeName);
            ph.setItemName(itemName);
        }
        return ph;
    }

    class TryLoopRe {
        boolean isLoop;
        CheapNode last;
    }

    private TryLoopRe tryLoop(CheapElement el, NutBean vars, OomlEntry en) {
        TryLoopRe re = new TryLoopRe();
        // 防守
        if (!el.isStdTagAs("^W:(P|TR)$")) {
            return re;
        }
        // 尝试得到占位符，看看是不是循环的开始
        OomlWPlaceholder mkS = this.getLoopPlaceholder(el);
        if (null == mkS || !mkS.isLoopBegin()) {
            return re;
        }

        // 找到对应的变量
        String varName = mkS.getName();
        String itName = mkS.getItemName();
        Object vals = vars.get(varName);
        if (null == vals) {
            return re;
        }
        int count = Wlang.count(vals);
        List<Object> items = new ArrayList<>(count);
        Wlang.each(vals, new WnEachIteratee<Object>() {
            public void invoke(int index, Object ele, Object src) {
                items.add(ele);
            }
        });

        // 如果是循环的开始，那么尝试收集后续段，直到循环结束
        re.isLoop = true;
        List<CheapNode> list = new LinkedList<>();
        re.last = el.getNextSibling();
        while (null != re.last) {
            // 判断是否为结束
            if (re.last.isElement()) {
                CheapElement elNext = (CheapElement) re.last;
                OomlWPlaceholder mkE = this.getLoopPlaceholder(elNext);
                if (null != mkE && mkE.isLoopEnd()) {
                    break;
                }
            }
            // 收集
            list.add(re.last);
            // 指向下一个
            re.last = re.last.getNextSibling();
        }

        // 循环替换模板段落，并逐个插入到开始节点前面
        boolean hasDefItemName = !Ws.isBlank(itName);
        NutMap vars2 = new NutMap();
        vars2.attach(vars);
        for (Object it : items) {
            // 设置上下文
            if (hasDefItemName) {
                vars2.put(itName, it);
            }
            // 循环模板
            for (CheapNode li : list) {
                // 复制节点，并插入到开始节点前面
                CheapNode li2 = li.clone();
                // 如果是元素节点，则执行替换操作
                if (li2.isElement()) {
                    this.processSelf((CheapElement) li2, vars2, en);
                }
                el.insertPrev(li2);
            }
        }

        // 删除中间的模板节点
        el.remove();
        if (!list.isEmpty()) {
            for (CheapNode li : list) {
                li.remove();
            }
        }
        if (null != re.last) {
            re.last.remove();
        }

        // 搞定
        return re;
    }

    private void applyPlaceholder(CheapElement el, NutBean vars, OomlEntry en) {
        // 默认，处理普通占位符
        if (el.isStdTagName("W:P")) {
            OomlWRunList rl = new OomlWRunList();
            rl.load(el);
            if (rl.prepare() > 0) {
                rl.explain(vars);
            }
        }
    }

    private void processElement(CheapElement el, NutBean vars, OomlEntry en) {
        // 防守
        if (!el.hasChildren()) {
            return;
        }

        // 依次处理子节点
        CheapElement child = el.getFirstChildElement();
        while (null != child) {
            // 是否是循环段落的开始呢？
            // 如果是，就收集一个后续的段落列表
            // 返回占位符，以便调用者开始收集段落
            TryLoopRe re = this.tryLoop(child, vars, en);
            if (re.isLoop) {
                // 到头了
                if (null == re.last) {
                    break;
                }
                // 继续后续段落列表
                child = re.last.getNextElement();
                continue;
            }

            // 处理这个子节点
            processSelf(child, vars, en);

            // 下一个子节点
            child = child.getNextElement();
        }
    }

    private void processSelf(CheapElement child, NutBean vars, OomlEntry en) {
        // <v:shape> 节点: 替换占位图片
        if (!tryAsShape(child, vars, en)) {
            // 如果不是，则尝试替换普通占位符节点:
            applyPlaceholder(child, vars, en);

            // 递归子节点
            this.processElement(child, vars, en);
        }
    }

    private void processXML(OomlEntry en, NutBean vars) {
        // 加载实体对应的 XML
        CheapDocument doc = Oomls.parseEntryAsXml(en);

        // 从根节点开始处理
        this.processElement(doc.root(), vars, en);

        // 保存实体内容
        String xml = doc.toMarkup();
        byte[] buf = xml.getBytes(Encoding.CHARSET_UTF8);
        en.setContent(buf);
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
