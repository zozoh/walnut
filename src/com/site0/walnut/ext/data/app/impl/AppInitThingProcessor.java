package com.site0.walnut.ext.data.app.impl;

import java.util.List;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;

public class AppInitThingProcessor implements AppInitProcessor {

    @Override
    public void process(AppInitItemContext ing) {
        // 首先创建自己的目录
        WnObj oTs = ing.checkObj(WnRace.DIR);

        // 更新元数据
        NutMap meta = ing.genMeta(true, false);
        meta.put("tp", "thing_set");
        meta.putDefault("icon", "fas-cubes");
        meta.putDefault("th_auto_select", true);

        ing.io.appendMeta(oTs, meta);

        // 链接对应的文件
        if (ing.item.hasLinkPath()) {
            WnObj oDir = ing.io.check(ing.oHome, ing.item.getLinkPath());
            // 讲其内所有的文件，都链接到当前数据集下
            List<WnObj> oChildren = ing.io.getChildren(oDir, null);
            for (WnObj oChild : oChildren) {
                WnObj oF = null;
                //　File 
                if (oChild.isFILE()) {
                    ing.out.printlnf("  + %s -> %s", oChild.name(), oChild.path());
                    oF = ing.io.createIfNoExists(oTs, oChild.name(), WnRace.FILE);
                    oF.link(oChild.path());
                }
                // Dir
                else if(oChild.isDIR()) {
                    ing.out.printlnf("  + %s/ -> %s/", oChild.name(), oChild.path());
                    oF = ing.io.createIfNoExists(oTs, oChild.name(), WnRace.DIR);
                    oF.link(oChild.path());
                }
                if (null != oF) {
                    ing.io.set(oF, "^(ln)$");
                }
            }
        }

        // 执行初始化命令
        ing.run.exec("thing id:" + oTs.id() + " init;");
    }

}
