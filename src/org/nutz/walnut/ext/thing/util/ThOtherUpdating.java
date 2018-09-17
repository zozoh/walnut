package org.nutz.walnut.ext.thing.util;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.lang.Lang;
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
        Pattern p = Pattern.compile("^@([^:]+)(:(int|float|boolean|string))?$");
        for (Map.Entry<String, Object> en : tmpl.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            if (null == val)
                continue;
            String str = val.toString();
            Object v2 = null;

            // 直接填值
            Matcher m = p.matcher(str);
            if (m.find()) {
                String k2 = m.group(1);
                v2 = context.get(k2);
                // 转换值
                String valType = m.group(3);
                if (null != v2 && !Strings.isBlank(valType)) {
                    // int
                    if ("int".equals(valType)) {
                        v2 = Integer.parseInt(v2.toString());
                    }
                    // float
                    else if ("float".equals(valType)) {
                        v2 = Float.parseFloat(v2.toString());
                    }
                    // boolean
                    else if ("boolean".equals(valType)) {
                        v2 = Castors.me().castTo(v2, Boolean.class);
                    }
                    // string
                    else if ("int".equals(valType)) {
                        v2 = v2.toString();
                    }
                    // 靠，不可能
                    else {
                        throw Lang.impossible();
                    }
                }
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
