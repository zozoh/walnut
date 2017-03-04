package org.nutz.walnut.ext.hmaker.skin;

import java.util.Map;

import org.nutz.lang.Strings;

public class HmSkinInfo {

    public String name;

    public Map<String, HmSkinInfoCom[]> com;

    public Map<String, String> template;

    public String[] js;

    public HmSkinInfoCom[] area;

    public HmSkinInfoCom[] menuItem;

    public HmSkinInfoCom getSkinForCom(String ctype, String skin) {
        if (!Strings.isBlank(skin)) {
            HmSkinInfoCom[] list = com.get(ctype);
            if (null != list && list.length > 0) {
                for (HmSkinInfoCom info : list) {
                    if (info.is(skin)) {
                        return info;
                    }
                }
            }
        }
        return null;
    }

    public String getSkinForTemplate(String templateName) {
        if (null != template)
            return template.get(templateName);
        return null;
    }

}
