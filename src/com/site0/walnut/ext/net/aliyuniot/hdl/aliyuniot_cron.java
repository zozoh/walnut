package com.site0.walnut.ext.net.aliyuniot.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import org.nutz.web.Webs.Err;

@JvmHdlParamArgs(value="cqn", regex="^dry$")
public class aliyuniot_cron extends aliyuniot_shadow {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String imei = hc.params.val_check(0);
        String cron = hc.params.val_check(1);
        if (Strings.isBlank(imei) || Strings.isBlank(cron)) {
            throw Err.create("e.cmd.npower_cron.miss_args");
        }
        // 校验cron是否合法,模拟lua代码的解析过程
        if (cron.contains(" ") || cron.contains("\t") || cron.contains("\n")) {
            throw Err.create("e.cmd.npower_cron.invaild_cron_with_blank");
        }
        // on:000000-010000,off:010001-050000,on:050001-235959
        // 0点到1点开启, 1点零1秒到5点关闭,5点零1秒到23点59分59秒开启
        for (String tmp : cron.split(",")) {
            // 不允许空字符串
            if (Strings.isBlank(tmp)) {
                throw Err.create("e.cmd.npower_cron.invaild_cron_with_blank");
            }
            // 使用冒号分隔
            String[] tmp2 = tmp.split("\\:");
            if (tmp2.length != 2) {
                throw Err.create("e.cmd.npower_cron.invaild_cron_with_blank");
            }
            if (!("on".equals(tmp2[0]) || "off".equals(tmp2[0]))) {
                throw Err.create("e.cmd.npower_cron.only_on_off");
            }
            // 第二部分是时间定义,肯定是 6+1+6=13个字符
            if (tmp2[1].length() != 13) {
                throw Err.create("e.cmd.npower_cron.invaild_time");
            }
            // 接下来,解析是否为数字
            tmp2 = tmp2[1].split("\\-");
            Integer.parseInt(tmp2[0], 10);
            Integer.parseInt(tmp2[1], 10);
            // 应该就好了
        }
        // 看来合法了
        hc.params.setv("u", Json.toJson(new NutMap("Cron", cron).setv("OpMode", "cron")));
        super.invoke(sys, hc);
    }

}
