package com.site0.walnut.ext.sys.i18n;

import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;

public class cmd_i18n extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 解析参数
        ZParams params = ZParams.parse(args, "cqn", "^(reload)$");

        // 获取服务类
        Wni18nService i18ns = this.ioc.get(Wni18nService.class);

        // 重新加载
        if (params.is("reload")) {
            i18ns.reload();
        }

        // 得到语言
        String lang = params.val(0, sys.getLang());

        // 输出键
        if (params.has("k")) {
            String key = params.get("k");
            String msg = i18ns.getMsg(lang, key);
            sys.out.println(msg);
        }
        // 输出集合
        else {
            Map<String, String> map = i18ns.getLang(lang);
            JsonFormat jfmt = Cmds.gen_json_format(params);
            sys.out.println(Json.toJson(map, jfmt));
        }
    }

}
