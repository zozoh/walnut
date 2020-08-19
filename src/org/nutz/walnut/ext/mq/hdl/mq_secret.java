package org.nutz.walnut.ext.mq.hdl;

import org.nutz.lang.random.R;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("Q")
public class mq_secret implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        int len = hc.params.getInt("len", 50);

        // 生成密钥
        String skey = R.sg(len).next();

        // 创建密钥文件
        String aph = Wn.normalizeFullPath("~/.mq/secret", sys);
        WnObj oSecret = sys.io.createIfNoExists(null, aph, WnRace.FILE);
        sys.io.writeText(oSecret, skey);

        // 输出
        if (!hc.params.is("Q")) {
            sys.out.println(skey);
        }
    }

}
