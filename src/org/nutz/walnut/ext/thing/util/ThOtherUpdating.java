package org.nutz.walnut.ext.thing.util;

import java.util.List;
import java.util.Map;

import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.WnThingService;

public class ThOtherUpdating {

    /**
     * 准备要更新的服务类
     */
    public WnThingService service;

    /**
     * 准备要更新的其他记录
     */
    public List<WnObj> list;

    /**
     * 要修改的元数据
     */
    public NutMap meta;

    public void doUpdate() {
        if (null == list || null == meta || null == service)
            return;
        if (list.isEmpty() || meta.isEmpty())
            return;
        for (WnObj ot : this.list) {
            this.service.updateThing(ot.id(), this.meta);
        }
    }

    /**
     * @param meta
     *            要填充的元数据
     * @param tmpl
     *            填充模板，格式类似
     * 
     *            <pre>
     * {
         "dev_tp" : "@g1",
         "spl_nm" : "@g2",
         "spl_md" : "@g3",
         "spl_nb" : "@g4:int"
       }
     *            </pre>
     * 
     * @param context
     */
    public void fillMeta(NutMap meta, NutMap tmpl, NutBean context) {
        for (Map.Entry<String, Object> en : tmpl.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            if (null == val)
                continue;
            String str = val.toString();
            Object v2 = null;
            // 直接填值
            if (str.startsWith("@")) {
                String k2 = Strings.trim(str.substring(1));
                v2 = context.get(k2);
            }
            // 否则当做模板
            else {
                v2 = Tmpl.exec(str, context);
            }
            // 计入
            if (v2 != null) {
                meta.put(key, v2);
            }
        }
    }

}
