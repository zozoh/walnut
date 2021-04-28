package org.nutz.walnut.ext.data.thing.hdl;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.WnThingService;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(check)$")
public class thing_get implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 分析参数
        String thId = hc.params.val_check(0);
        boolean isFull = hc.params.is("full");
        String sort = hc.params.get("sort");
        String sortKey = null;
        boolean isAsc = true;
        if (!Strings.isBlank(sort) && !"true".equals(sort)) {
            int pos = sort.indexOf(':');
            if (pos > 0) {
                sortKey = sort.substring(0, pos);
                isAsc = !sort.substring(pos + 1).equals("desc");
            }
            // 否则用默认
            else {
                sortKey = sort;
                isAsc = true;
            }
        }

        // 如果还需要查询关联对象的内容指纹
        String sha1 = hc.params.getString("sha1");
        String[] sha1Fields = null;
        if (!Strings.isBlank(sha1)) {
            sha1Fields = Strings.splitIgnoreBlank(sha1);
        }

        // 准备服务类
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        WnThingService wts = new WnThingService(sys, oTs);

        // 获取对象
        WnObj oT = null;

        // 调用接口·严格模式
        if (hc.params.is("check")) {
            oT = wts.checkThing(thId, isFull, sortKey, isAsc);
        }
        // 调用接口·普通模式
        else {
            oT = wts.getThing(thId, isFull, sortKey, isAsc);
        }

        // 要计算 sha1 扩展的 fields
        if (null != oT && null != sha1Fields && sha1Fields.length > 0) {
            for (String key : sha1Fields) {
                String val = oT.getString(key);
                if (!Strings.isBlank(val)) {
                    WnObj o = sys.io.fetch(oT, val);
                    if (null != o) {
                        oT.put(key + "_obj",
                               o.pickBy("^(id|nm|title|sha1|len|mime|tp|width|height)$"));
                    }
                }
            }
        }

        // 得到返回
        hc.output = oT;
    }

}
