package com.site0.walnut.ext.data.thing.util;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.util.WnPager;
import com.site0.walnut.util.bean.WnBeanMapping;
import com.site0.walnut.util.obj.WnObjJoinFields;

public class ThQuery {

    public String qStr;

    public WnPager wp;

    public WnObjJoinFields joinFields;

    // public String[] tss;

    public WnBeanMapping mapping;

    public WnTmpl mappingPath;

    public WnTmpl mappingPathFallback;

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
        this(Wlang.map(key, val));
    }

}
