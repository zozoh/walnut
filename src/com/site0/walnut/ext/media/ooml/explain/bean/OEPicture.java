package com.site0.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.Files;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.dom.bean.CheapResource;
import com.site0.walnut.ooml.OomlContentTypes;
import com.site0.walnut.ooml.OomlEntry;
import com.site0.walnut.ooml.OomlRel;
import com.site0.walnut.ooml.OomlRels;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

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
        String enPath = Files.getParent(entry.getPath());
        String imgRph = Wn.appendPath(enPath, imgPh);

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

        // 6. 搞定
        return drawing;

    }

}
