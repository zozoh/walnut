package org.nutz.walnut.ext.data.thing.util;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.bean.WnBeanMapping;

public class ThQuery {

    public String qStr;

    public WnPager wp;

    //public String[] tss;
    
    public WnBeanMapping mapping;
    
    public WnTmpl mappingPath;
    
    public boolean mappingOnly;

    public String[] sha1Fields;

    public NutMap sort;

    public boolean needContent;

    public boolean autoObj;

    public ThQuery() {}

    public ThQuery(String json) {
        this.qStr = json;
    }

    public ThQuery(NutMap map) {
        this.qStr = Json.toJson(map);
    }

    public ThQuery(String key, Object val) {
        this(Lang.map(key, val));
    }

}
