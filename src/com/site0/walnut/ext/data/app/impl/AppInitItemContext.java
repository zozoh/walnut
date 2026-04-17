package com.site0.walnut.ext.data.app.impl;

import com.site0.walnut.util.tmpl.WnTmpl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.app.bean.init.AppInitItem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.explain.WnExplain;
import com.site0.walnut.util.explain.WnExplains;

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

        // 没有内容
        if (!item.hasContentType()) {
            return;
        }

        // 不是强制写
        if (!item.isOverrideContent() && !Wn.Io.isEmptySha1(obj.sha1())) {
            // println("~~~~~~~~~~~~~~~~");
            // println("~ NOT OVERRIDE ~");
            // println("~~~~~~~~~~~~~~~~");
            return;
        }

        // -----------------------------------
        // 复制的话需要特殊处理一下，因为可能会复制二进制文件
        // -----------------------------------
        if (item.isContentAsCopy()) {
            // 内容也可以是二进制文件
            if (item.hasContentFilePath()) {
                WnObj o = io.check(oHome, item.getContentFilePath());
                io.copyData(o, obj);
            }
            // 直接复制
            else {
                String content = item.getContent();
                io.writeText(obj, content);
            }
            return;
        }

        // 获取文件内容
        String content = item.getContent();
        if (item.hasContentFilePath()) {
            WnObj o = io.check(oHome, item.getContentFilePath());
            content = io.readText(o);
        }

        // -----------------------------------
        // 解析模板
        // -----------------------------------
        if (item.isContentAsTmpl()) {
            // 准备渲染上下文
            NutMap rc = prepare_render_context(this.vars);
            // 渲染内容
            content = WnTmpl.exec(content, rc);
            // 写入
            io.writeText(obj, content);
            // println("================");
            // println(content);
            // println("================");
        }
        // -----------------------------------
        // 解释为 JSON 内容
        // -----------------------------------
        else if (item.isContentAsExplain()) {
            // 准备渲染上下文
            NutMap rc = prepare_render_context(this.vars);

            // 解析内容
            Object input = Json.fromJson(content);
            WnExplain wx = WnExplains.parse(input);

            // 渲染内容
            Object output = wx.explain(rc);
            String json = Json.toJson(output, JsonFormat.nice());

            // 写入
            io.writeText(obj, json);
        }
        // -----------------------------------
        // 非法写入模式
        // -----------------------------------
        else {
            throw Er.create("e.app.init.InvalidFileContentType",
                            item.getContentType());
        }
        // 啥都没必要做
        // else {
        // println("................");
        // println(". NO CONTENT .");
        // println("................");
        // }

    }

    private NutMap prepare_render_context(NutBean vars) {
        NutMap rc = new NutMap();
        if (null != vars && !vars.isEmpty()) {
            rc.putAll(vars);
        }
        if (item.hasContentFileVars()) {
            rc.putAll(item.getContentFileVars());
        }
        return rc;
    }

}
