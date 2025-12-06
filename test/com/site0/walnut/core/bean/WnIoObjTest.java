package com.site0.walnut.core.bean;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

public class WnIoObjTest {
    
    @Test
    public void test_pvg2() {
        WnIoObj o = new WnIoObj();
        o.id(Wn.genId());
        o.put("pvg", Wlang.map("{'@x':'0700'}"));
        WnIoObj p = new WnIoObj();
        p.id(Wn.genId());
        p.put("pvg", Wlang.map("{'+B':'~0777','C':'0755'}"));
        o.setParent(p);
        WnIoObj p2 = new WnIoObj();
        p2.id(Wn.genId());
        p2.put("pvg", Wlang.map("{'@x':'!0666','C':7}"));
        p.setParent(p2);

        NutBean pvg = o.joinCustomizedPrivilege(null);
        NutMap map = Wlang.map("{'@x':438,'C':493}");
        assertEquals(map, pvg);
    }

    @Test
    public void test_pvg() {
        WnIoObj o = new WnIoObj();
        o.id(Wn.genId());
        o.put("pvg", Wlang.map("{'@x':'0700'}"));
        WnIoObj p = new WnIoObj();
        p.id(Wn.genId());
        p.put("pvg", Wlang.map("{'+B':'~0777','C':'0755'}"));
        o.setParent(p);

        NutBean pvg = o.joinCustomizedPrivilege(null);
        NutMap map = Wlang.map("{'@x':448,'C':493}");
        assertEquals(map, pvg);
    }

}
