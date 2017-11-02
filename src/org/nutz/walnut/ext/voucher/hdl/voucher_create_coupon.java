package org.nutz.walnut.ext.voucher.hdl;

import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 为促销活动创建具体的优惠卷
 * @author wendal
 *
 */
public class voucher_create_coupon implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String voucher_name = hc.params.check("name");
        String myName = sys.me.name();
        sys.nosecurity(()->{
            WnObj wobj = sys.io.createIfNoExists(null, "/sys/voucher/"+ myName + "/" + voucher_name, WnRace.DIR);
            int voucher_count = wobj.getInt("voucher_totalNum");
            long count = sys.io.count(new WnQuery().setv("pid", wobj.id()));
            if (voucher_count > count) {
                NutMap metas = new NutMap();
                for (String key : wobj.keySet()) {
                    if (key.startsWith("voucher_"))
                        metas.put(key, wobj.get(key));
                }
                metas.setv("voucher_payId", "");
                for (int i = 0; i < voucher_count - count; i++) {
                    WnObj t = sys.io.create(wobj, R.UU32(), WnRace.FILE);
                    sys.io.appendMeta(t, metas);
                }
            }
        });
    }

}
