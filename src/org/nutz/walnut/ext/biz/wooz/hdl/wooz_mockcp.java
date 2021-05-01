package org.nutz.walnut.ext.biz.wooz.hdl;

import org.nutz.walnut.ext.biz.wooz.util.WoozCpMocking;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("^(clean_all_records)$")
public class wooz_mockcp implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 得到关键参数
        int speed = hc.params.getInt("speed", 7000);
        String compId = hc.params.val_check(0);

        // 得到赛事对象，仙踪域的赛事，需要转换为主办方赛事
        WoozCpMocking mock = new WoozCpMocking(sys.out, sys.io, compId);
        mock.setSpeed(speed);
        mock.setCleanAllRecords(hc.params.is("clean_all_records"));
        mock.setPlayerLimit(hc.params.getInt("pylimit", 10));
        mock.setCpdName(hc.params.get("dev"));

        // 执行
        mock.doMock();

    }

}
