package org.nutz.walnut.ext.data.app.impl;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;

public class AppInitIncludeProcessor implements AppInitProcessor {

    private static final String HR0 = Strings.dup('>', 50);
    private static final String HR1 = Strings.dup('<', 50);

    @Override
    public void process(AppInitItemContext ing) {
        // 获取引入的文件
        WnObj oInclude = ing.io.check(ing.oHome, ing.item.getPath());
        String input = ing.io.readText(oInclude);

        // 准备一个新的上下文变量
        AppInitContext ac = ing.clone();
        ac.outPrefix = ">> ";

        // 准备上下文，并融合新的上下文变量
        NutMap vars = ing.genMeta(true);
        ac.vars.mergeWith(vars);

        // 解析
        ac.group = ing.init.parse(input, ac.vars);

        // 执行
        ing.println(HR0);
        ing.init.process(ac);
        ing.println(HR1);

    }
}
