package org.nutz.walnut.ext.media.dom.util;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

public class CheapDomReplaceWnObjId {

    private CheapDocument doc;

    private WnIo io;

    private NutBean vars;

    private Map<String, WnObj> cachePathObj;

    private static String REG = "^(wn-obj-)([0-9a-z]+)$";
    private static Pattern _P = Pattern.compile(REG);

    public boolean doReaplace(Map<String, String> idPaths) {
        // 寻找到对应元素
        List<CheapElement> els = doc.findElements(el -> {
            return el.hasAttr("wn-obj-id");
        });

        // 依次处理
        boolean re = false;
        for (CheapElement el : els) {
            // 整理所有 wn-obj- 开头的属性
            NutBean obj = el.getAttrObj(name -> {
                Matcher m = _P.matcher(name);
                if (m.find()) {
                    return m.group(2);
                }
                return null;
            });

            // 根据 ID 得到路径
            String oid = obj.getString("id");
            if (Ws.isBlank(oid))
                continue;

            String path = idPaths.get(oid);
            if (Ws.isBlank(path))
                continue;

            // 进行映射: 先看看缓存
            WnObj oTa = this.cachePathObj.get(path);

            // 那就真的读取咯
            if (null == oTa) {
                String aph = Wn.normalizeFullPath(path, vars);
                oTa = io.fetch(null, aph);
                if (null == oTa) {
                    continue;
                }
                // 记入缓存
                this.cachePathObj.put(path, oTa);
            }

            // 换一批属性
            NutMap attrs = new NutMap();
            for (Map.Entry<String, Object> en : obj.entrySet()) {
                String key = en.getKey();
                Object val = oTa.get(key);
                if (null == val) {
                    val = en.getValue();
                }
                attrs.put(key, val);
            }

            // 设置元素
            el.setAttrs(attrs, "wn-obj-");
            re |= true;

            // 对于图片，那么需要重新设置一下 ID
            if (el.isStdTagName("IMG") && el.hasAttr("src")) {
                String src = "/o/content?str=id:" + oTa.id();
                el.attr("src", src);
            }
        }

        return re;
    }

    public String getDocMarkup() {
        return doc.toMarkup();
    }

    public CheapDocument getDoc() {
        return doc;
    }

    public void setDoc(CheapDocument doc) {
        this.doc = doc;
    }

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public NutBean getVars() {
        return vars;
    }

    public void setVars(NutBean vars) {
        this.vars = vars;
    }

    public Map<String, WnObj> getCachePathObj() {
        return cachePathObj;
    }

    public void setCachePathObj(Map<String, WnObj> cachePathObj) {
        this.cachePathObj = cachePathObj;
    }

}
