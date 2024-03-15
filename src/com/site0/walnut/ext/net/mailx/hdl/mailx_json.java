package com.site0.walnut.ext.net.mailx.hdl;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class mailx_json extends MailxFilter {

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        // 准备映射
        String mapping = params.getString("mapping");
        NutMap transMap = null;
        if (!Strings.isBlank(mapping)) {
            transMap = Wlang.map(mapping);
        }

        // 读取文件
        for (String rph : params.vals) {
            WnObj o = Wn.checkObj(sys, rph);
            String json = sys.io.readText(o);
            NutMap map = Json.fromJson(NutMap.class, json);
            NutBean bean;
            if (null == transMap) {
                bean = map;
            }
            // 否则转换
            else {
                bean = (NutBean) Wn.explainObj(map, transMap);
            }

            // 计入变量
            fc.vars.putAll(bean);
        }
    }

}
