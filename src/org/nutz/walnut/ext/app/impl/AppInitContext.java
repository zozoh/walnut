package org.nutz.walnut.ext.app.impl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.app.bean.init.AppInitGroup;
import org.nutz.walnut.ext.app.bean.init.AppInitItem;
import org.nutz.walnut.ext.app.bean.init.AppInitItemType;

public class AppInitContext {

    private static final Map<AppInitItemType, AppInitProcessor> processors = new HashMap<>();

    static {
        processors.put(AppInitItemType.FILE, new AppInitFileProcessor());
        processors.put(AppInitItemType.DIR, new AppInitDirProcessor());
        processors.put(AppInitItemType.THING, new AppInitThingProcessor());
        processors.put(AppInitItemType.API, new AppInitApiProcessor());
        processors.put(AppInitItemType.ENV, new AppInitEnvProcessor());
        processors.put(AppInitItemType.HOME, new AppInitHomeProcessor());
    }

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

    public void println(String str) {
        if (null != out) {
            out.println(str);
        }
    }

    public void printlnf(String fmt, Object... args) {
        if (null != out) {
            out.printlnf(fmt, args);
        }
    }

    public void printItem(AppInitItem item) {
        if (null != out) {
            out.println(item.toBrief());
        }
    }

    public AppInitItemContext createProcessing(AppInitItem item) {
        AppInitItemContext ing = new AppInitItemContext();
        ing.run = run;
        ing.io = io;
        ing.out = out;
        ing.group = group;
        ing.vars = vars;
        ing.oHome = oHome;
        ing.oDist = oDist;
        ing.item = item;
        return ing;
    }

    public AppInitProcessor getProcessor(AppInitItem item) {
        if (!item.hasType()) {
            throw Lang.makeThrow("AppInitItem without type", item.toString());
        }
        return processors.get(item.getType());
    }

}
