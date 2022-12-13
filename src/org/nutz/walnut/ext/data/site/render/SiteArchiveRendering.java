package org.nutz.walnut.ext.data.site.render;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class SiteArchiveRendering {

    private Object ing;

    private WnObj oArHome;

    private NutMap filter;

    private WnMatch canRecur;

    SiteArchiveRendering(SiteRendering ing, SiteRenderArchive ar) {
        this.ing = ing;
        this.oArHome = ing.checkObj(ar.getHomePath());
        this.filter = ar.getFilter();
        this.canRecur = AutoMatch.parse(ar.getRecur());
    }

    void renderArchives() {
        // 防守
        if (!this.oArHome.isDIR()) {
            throw Er.create("e.site.render.ArHomeNotDir", oArHome);
        }
        this.renderChildren(oArHome);
    }
    
    void renderChildren(WnObj oPAr) {
        if(null == oPAr) {
            return;
        }
        WnQuery q = Wn.Q.pid(oPAr);
        q.setAll(filter);
        
    }

    private WnQuery genQuery(WnObj oP) {
        WnQuery q = Wn.Q.pid(oP);
        q.setAll(filter);
        q.sort(Wlang.map("nm", 1));
        q.limit(2000);
        return q;
    }
}
