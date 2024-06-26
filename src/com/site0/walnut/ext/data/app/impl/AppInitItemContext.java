package com.site0.walnut.ext.data.app.impl;

import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.app.bean.init.AppInitItem;
import com.site0.walnut.util.Wn;

public class AppInitItemContext extends AppInitContext {

    public AppInitItem item;

    public WnObj checkObj(WnRace race) {
        return io.createIfNoExists(oDist, item.getPath(), race);
    }

    public NutMap genMeta() {
        return genMeta(true, true);
    }

    public NutMap genMeta(boolean includeProperties) {
        return genMeta(includeProperties, true);
    }

    public NutMap genMeta(boolean includeProperties, boolean includeLinkPath) {
        NutMap meta = new NutMap();
        if (includeProperties && item.hasProperties()) {
            meta.putAll(item.getProperties());
        }
        if (includeLinkPath && item.hasLinkPath()) {
            meta.put("ln", item.getLinkPath());
        }
        if (item.hasMeta()) {
            meta.putAll(item.getMeta());
        }
        return (NutMap) Wn.explainObj(vars, meta);
    }

    public void writeFile(WnObj obj) {
        if (!obj.isFILE()) {
            println("!!!!!!!!!!!!");
            println("! NOT FILE !");
            println("!!!!!!!!!!!!");
            return;
        }

        // 不是强制写
        if (!item.isOverrideContent() && !Wn.Io.isEmptySha1(obj.sha1())) {
            // println("~~~~~~~~~~~~~~~~");
            // println("~ NOT OVERRIDE ~");
            // println("~~~~~~~~~~~~~~~~");
            return;
        }

        // 解析模板
        if (item.isContentAsTmpl()) {
            // 得到内容
            String content = item.getContent();
            if (item.hasContentFilePath()) {
                WnObj o = io.check(oHome, item.getContentFilePath());
                content = io.readText(o);
                if (item.hasContentFileVars()) {
                    content = WnTmpl.exec(content, item.getContentFileVars());
                }
            }
            content = WnTmpl.exec(content, vars);
            // 写入
            io.writeText(obj, content);
            // println("================");
            // println(content);
            // println("================");
        }
        // 直接 COPY 内容
        else if (item.hasContentFilePath()) {
            // 内容也需要转换
            if (item.hasContentFileVars()) {
                WnObj o = io.check(oHome, item.getContentFilePath());
                String content = io.readText(o);
                content = WnTmpl.exec(content, item.getContentFileVars());
                io.writeText(obj, content);
            }
            // 内容也可以是二进制文件
            else {
                WnObj o = io.check(oHome, item.getContentFilePath());
                io.copyData(o, obj);
            }
        }
        // 直接写入内容
        else if (item.hasContent()) {
            String content = item.getContent();
            io.writeText(obj, content);
            // println("================");
            // println(content);
            // println("================");
        }
        // 啥都没必要做
        // else {
        // println("................");
        // println(". NO CONTENT .");
        // println("................");
        // }

    }

}
