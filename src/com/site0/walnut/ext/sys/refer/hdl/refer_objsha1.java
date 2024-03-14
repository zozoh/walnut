package com.site0.walnut.ext.sys.refer.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoMapping;
import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.core.bm.localbm.LocalIoBM;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class refer_objsha1 implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析参数
        String ph = hc.params.val_check(0);
        long len = hc.params.val_check_long(1);
        String sha1 = hc.params.val_check(2);

        // 最后修改时间
        long lm = hc.params.val_long(3, Wn.now());

        // 获取对象
        WnObj o = Wn.checkObj(sys, ph);

        // 防守: 空
        if (Wn.Io.isEmptySha1(sha1)) {
            return;
        }

        // 获取当前对象的引用接口
        WnIoMapping im = sys.io.getMappingFactory().checkMapping(o);
        WnIoBM bm = im.getBucketManager();
        WnReferApi refers = null;
        if (bm instanceof LocalIoBM) {
            refers = ((LocalIoBM) bm).getReferApi();
        }

        // 防守：本对象区块无需引用计数
        if (null == refers) {
            return;
        }

        // 引用增加
        refers.add(sha1, o.id());

        // 修改对象
        o.sha1(sha1);
        o.lastModified(lm);
        o.len(len);

        sys.io.set(o, "^(sha1|len|lm)$");

    }

}
