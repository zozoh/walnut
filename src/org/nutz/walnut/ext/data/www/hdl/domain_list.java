package org.nutz.walnut.ext.data.www.hdl;

import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class domain_list implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String cmdText = "obj /domain -match"
                         + " -t 'id,nm,tp,domain,site,expi_at,title'"
                         + " -bish -pager"
                         + " -sort nm:1"
                         + " -limit "
                         + hc.params.getInt("limit", 100)
                         + " -skip "
                         + hc.params.getInt("skip", 0);
        sys.exec(cmdText);
    }

}