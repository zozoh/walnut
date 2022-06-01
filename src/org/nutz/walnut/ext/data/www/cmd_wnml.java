package org.nutz.walnut.ext.data.www;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_wnml extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 生成上下文
        ZParams params = ZParams.parse(args, null);

        // 得到输入的模板文件
        String str = params.val_check(0);
        WnObj o = Wn.checkObj(sys, str);
        String input = sys.io.readText(o);

        // 初始化上下文
        NutMap context;

        // 指定了一个上下文的 JSON 字符串，合并
        if (params.has("c")) {
            String json = params.check("c");
            // 仅仅是声明，那么就从标准输入读一下
            if ("true".equals(json)) {
                json = sys.in.readAll();
            }
            // 不是 JSON 的格式，那么试图读读文件对象
            else if (!Strings.isQuoteBy(json, '{', '}')) {
                WnObj oJson = Wn.checkObj(sys.io, json);
                json = sys.io.readText(oJson);
            }
            // 解析并放入上下文
            context = Lang.map(json);
        }
        // 建立一个空白的上下文
        else {
            context = new NutMap();
        }

        // 设置上下文
        context.putDefault("grp", sys.getMyGroup());
        context.putDefault("fnm", o.name());
        context.putDefault("rs", "/gu/rs");
        context.putDefault("CURRENT_PATH", o.path());
        String dirPath = o.parent().path();
        context.putDefault("CURRENT_DIR", dirPath);
        context.putDefault("SITE_HOME", params.get("home", dirPath));

        // 准备服务类
        WnmlService ws = new WnmlService();

        // 准备运行时对象
        WnmlRuntime wrt = new JvmWnmlRuntime(sys);

        // 执行
        String re = ws.invoke(wrt, context, input);
        sys.out.println(re);

    }

}
