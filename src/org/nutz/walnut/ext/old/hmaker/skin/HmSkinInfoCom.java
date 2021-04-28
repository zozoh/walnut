package org.nutz.walnut.ext.old.hmaker.skin;

import java.util.List;

import org.nutz.lang.util.NutMap;

public class HmSkinInfoCom {

    public String text;

    public String selector;

    public NutMap attributes;

    public List<Object> blockFields;

    public boolean is(String skin) {
        if (null != selector && null != skin)
            return selector.equals(skin);
        return false;
    }

}
