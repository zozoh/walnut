package com.site0.walnut.ext.data.app.impl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.WnExecutable;
import com.site0.walnut.api.WnOutputable;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.app.bean.init.AppInitGroup;
import com.site0.walnut.ext.data.app.bean.init.AppInitItem;
import com.site0.walnut.ext.data.app.bean.init.AppInitItemType;

public class AppInitContext {

    private static final Map<AppInitItemType, AppInitProcessor> processors = new HashMap<>();

    static {
        processors.put(AppInitItemType.FILE, new AppInitFileProcessor());
        processors.put(AppInitItemType.DIR, new AppInitDirProcessor());
        processors.put(AppInitItemType.THING, new AppInitThingProcessor());
        processors.put(AppInitItemType.API, new AppInitApiProcessor());
        processors.put(AppInitItemType.ENV, new AppInitEnvProcessor());
        processors.put(AppInitItemType.HOME, new AppInitHomeProcessor());
        processors.put(AppInitItemType.INCLUDE, new AppInitIncludeProcessor());
    }

    public AppInitService init;

    public WnExecutable run;

    public WnIo io;

    public AppInitGroup group;

    public NutBean vars;

    public WnOutputable out;

    /**
     * 被初始化的目标目录
     */
    public WnObj oDist;

    /**
     * 加载初始化文件的原始目录，用来读取其他资源
     */
    public WnObj oHome;

    /**
     * oHome 目录下的初始化模板文件对象
     */
    public WnObj oInitFile;

    /**
     * 输出日志行的固定前缀，引入项目，增加固定前缀，比较容易看清楚
     */
    public String outPrefix;

    public void println(String str) {
        if (null != out) {
            if (null != outPrefix) {
                out.print(outPrefix);
            }
            out.println(str);
        }
    }

    public void printlnf(String fmt, Object... args) {
        if (null != out) {
            if (null != outPrefix) {
                out.print(outPrefix);
            }
            out.printlnf(fmt, args);
        }
    }

    public void printItem(AppInitItem item) {
        if (null != out) {
            if (null != outPrefix) {
                out.print(outPrefix);
            }
            out.println(item.toBrief());
        }
    }

    public AppInitItemContext createProcessing(AppInitItem item) {
        AppInitItemContext ing = new AppInitItemContext();
        ing.init = init;
        ing.run = run;
        ing.io = io;
        ing.out = out;
        ing.group = group;
        ing.vars = vars;
        ing.oHome = oHome;
        ing.oDist = oDist;
        ing.item = item;
        ing.outPrefix = outPrefix;
        return ing;
    }

    public AppInitContext clone() {
        AppInitContext ac = new AppInitContext();
        ac.init = init;
        ac.run = run;
        ac.io = io;
        ac.out = out;
        ac.group = group;
        ac.vars = new NutMap().mergeWith(this.vars);
        ac.oHome = oHome;
        ac.oDist = oDist;
        ac.outPrefix = outPrefix;
        return ac;
    }

    public AppInitProcessor getProcessor(AppInitItem item) {
        if (!item.hasType()) {
            throw Er.create("e.cmd.app.init.WithoutType", item.toString());
        }
        return processors.get(item.getType());
    }

}
