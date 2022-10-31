package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.bean.CheapResource;
import org.nutz.walnut.ooml.OomlEntry;
import org.nutz.walnut.ooml.OomlRel;
import org.nutz.walnut.ooml.OomlRels;
import org.nutz.walnut.util.Ws;

public class OEPicture extends OEVarItem {

    public OEPicture() {
        this.type = OENodeType.PICTURE;
    }

    @Override
    public CheapElement renderTo(CheapElement pEl, NutBean vars) {
        CheapElement drawing = refer.clone();
        pEl.append(drawing);

        String objPath = vars.getString(varName);
        if (Ws.isBlank(objPath)) {
            return drawing;
        }

        CheapResource img = loader.loadByPath(objPath);
        if (null == img) {
            return drawing;
        }

        // 找到 rId
        CheapElement aBlip = drawing.findElement(el -> {
            return el.isTagName("a:blip");
        });
        if (null == aBlip) {
            return drawing;
        }
        String rId = aBlip.attr("r:embed");
        if (Ws.isBlank(rId)) {
            return drawing;
        }

        // 3. 根据 rId 在 document.xml.rels 文件中找到 <Relationship>，
        // 并得到对应的图片路径 media/image1.png
        OomlRels rels = ooml.loadRelationships(entry);
        OomlRel rel = rels.get(rId);
        String imgPh = rel.getTarget();
        String imgRph = rels.getUniqPath(imgPh);

        // 4. 找到这个图片所在的条目
        OomlEntry enImg = ooml.getEntry(imgRph);

        // 5. 直接向其写入嵌入图片
        enImg.setContent(img.getContent());

        // 6. 搞定
        return drawing;

    }

}
