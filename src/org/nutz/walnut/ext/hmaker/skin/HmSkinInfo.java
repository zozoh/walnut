package org.nutz.walnut.ext.hmaker.skin;

import java.util.Map;

public class HmSkinInfo {

    public String name;

    public Map<String, HmSkinInfo[]> com;

    public Map<String, String> template;

    public String getSkin(String templateName) {
        if (null != template)
            return template.get(templateName);
        return null;
    }

}
