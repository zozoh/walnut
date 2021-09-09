package org.nutz.walnut.ext.media.ooml.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.api.CheapResourceLoader;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.bean.CheapResource;
import org.nutz.walnut.ext.media.ooml.api.OomlExplaining;
import org.nutz.walnut.ooml.OomlEntry;
import org.nutz.walnut.ooml.OomlPackage;
import org.nutz.walnut.ooml.OomlRelationship;
import org.nutz.walnut.ooml.OomlRels;
import org.nutz.walnut.ooml.Oomls;
import org.nutz.walnut.util.Ws;

public class WnOomlDocxExplaining implements OomlExplaining {

    private OomlPackage ooml;

    private CheapResourceLoader loader;

    public WnOomlDocxExplaining(OomlPackage ooml, CheapResourceLoader loader) {
        this.ooml = ooml;
        this.loader = loader;
    }

    private void tryAsShape(CheapElement el, NutBean vars, OomlEntry en) {
        if (el.isStdTagName("V:SHAPE")) {
            // 1. 判断是否有属性 alt="=变量名"
            String alt = Ws.trim(el.attr("alt"));
            if (null != alt && alt.startsWith("=")) {
                // 加载资源，如果确保资源在系统中存在
                String varName = alt.substring(1).trim();
                String objPath = vars.getString(varName);
                if (null == objPath) {
                    return;
                }
                CheapResource img = this.loader.loadByPath(objPath);
                if (null == img) {
                    return;
                }

                // 2. 找到子节点 `<v:imagedata>` 得到 `r:id` 属性
                CheapElement vid = el.getFirstChildElement("v:imagedata");
                if (null == vid) {
                    return;
                }
                String rId = vid.attr("r:id");
                if (null == rId) {
                    return;
                }

                // 3. 根据 rId 在 document.xml.rels 文件中找到 <Relationship>，
                // 并得到对应的图片路径 media/image1.png
                OomlRels rels = ooml.loadRelationships(en);
                OomlRelationship rel = rels.get(rId);
                String imgPh = rel.getTarget();
                String imgRph = rels.getUniqPath(imgPh);

                // 4. 找到这个图片所在的条目
                OomlEntry enImg = ooml.getEntry(imgRph);

                // 5. 直接向其写入嵌入图片
                enImg.setContent(img.getContent());

                // 6. 如果图片的扩展名与嵌入图片不一致
                String imgType = img.getSuffixName();
                if (!rel.isTargetType(imgType)) {
                    // 7. 将其后缀名进行修改
                    enImg.renameSuffix(imgType);
                    rel.renameSuffix(imgType);

                    // 8. 确保 [Content_Types].xml 文件中声明了这个扩展名所对应的 MIME 类型
                }

                // 修改图片的 title
                CheapElement elImgData = el.getFirstChildElement("v:imagedata");
                if (null != elImgData) {
                    String title = Ws.sBlank(img.getAlt(), img.getName());
                    if (!Ws.isBlank(title)) {
                        elImgData.attr("o:title", title);
                    }
                }
            }
        }
    }

    /**
     * <pre>
     * 0: "${变量名#tr-begin:it}"
     * 1: "变量名"
     * 2: "#tr-begin"
     * 3: "tr-begin"
     * 4: ":it"
     * 5: "it"
     * </pre>
     */
    private final Pattern PP = Pattern.compile("[$][{]([^}#:]+)(#([^:]+))(:([^}]+))[}]");

    private OomlWPlaceholder getPlaceholder(CheapElement el) {
        String str = Ws.trim(el.getText());
        if (null == str) {
            return null;
        }
        OomlWPlaceholder ph = null;
        Matcher m = PP.matcher(str);
        if (m.find()) {
            String varName = m.group(1);
            String typeName = m.group(3);
            String itemName = m.group(5);
            ph = new OomlWPlaceholder();
            ph.setName(varName);
            ph.setType(typeName);
            ph.setItemName(itemName);
        }
        return ph;
    }

    private void tryAsP(CheapElement el, NutBean vars, OomlEntry en) {
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
        // <w:p> 节点: 替换普通占位符
        tryAsP(el, vars, en);

        // <v:shape> 节点: 替换占位图片
        tryAsShape(el, vars, en);

        // 默认就是进行递归
        if (el.hasChildren()) {
            CheapElement child = el.getFirstChildElement();
            while (null != child) {
                // 是否是循环段落的开始呢？
                // 如果是，就收集一个后续的段落列表
                // 返回占位符，以便调用者开始收集段落

                this.processElement(child, vars, en);
                child = child.getNextElement();
            }
        }
    }

    private void processXML(OomlEntry en, NutBean vars) {
        // 加载实体对应的 XML
        CheapDocument doc = Oomls.parseEntryAsXml(en);

        // 从根节点开始处理
        this.processElement(doc.root(), vars, en);
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
