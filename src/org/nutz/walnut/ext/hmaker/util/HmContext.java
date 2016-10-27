package org.nutz.walnut.ext.hmaker.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.hmaker.skin.HmSkinInfo;
import org.nutz.walnut.ext.hmaker.template.HmTemplate;
import org.nutz.walnut.ext.hmaker.template.HmTemplateInfo;

public class HmContext {

    public WnIo io;

    /**
     * hmaker 的配置目录，!!! 这个必须由创建来赋值
     */
    public WnObj oConfHome;

    /**
     * 站点主目录
     */
    public WnObj oHome;

    /**
     * 输出目标
     */
    public WnObj oDest;

    /**
     * 站点皮肤
     */
    public WnObj oSkinCss;
    public WnObj oSkinJs;
    public HmSkinInfo skinInfo;

    /**
     * 记录所有的控件使用的模板，最后 copy 到 template 目录
     */
    public Map<String, HmTemplate> templates;

    /**
     * 站点除了转换还要 copy 的资源
     */
    public Set<WnObj> resources;

    // true 为严格模式，这种情况下，所有的转换处理都需要尽量不容忍任何潜在的错误
    public boolean strict;

    public HmContext(WnIo io) {
        this.io = io;
        this.templates = new HashMap<String, HmTemplate>();
        this.resources = new HashSet<WnObj>();
    }

    public HmContext(HmContext hpc) {
        this.io = hpc.io;
        this.resources = hpc.resources;
        this.oHome = hpc.oHome;
        this.oDest = hpc.oDest;
        this.oConfHome = hpc.oConfHome;
        this.oSkinCss = hpc.oSkinCss;
        this.oSkinJs = hpc.oSkinJs;
        this.skinInfo = hpc.skinInfo;
        this.templates = hpc.templates;
    }

    public HmTemplate getTemplate(String templateName) {
        HmTemplate tmpl = this.templates.get(templateName);
        if (null == tmpl) {
            tmpl = new HmTemplate();
            WnObj oTmplHome = io.check(oConfHome, "template/" + templateName);
            tmpl.oJs = io.check(oTmplHome, "jquery.fn.js");
            WnObj oInfo = io.check(oTmplHome, "template.info.json");
            tmpl.info = io.readJson(oInfo, HmTemplateInfo.class);
            this.templates.put(templateName, tmpl);
        }
        return tmpl;
    }

    public boolean hasSkin() {
        return null != skinInfo && null != oSkinCss;
    }

    public String getRelativePath(WnObj o) {
        return Disks.getRelativePath(oHome.path(), o.path());
    }

    public String getRelativePath(WnObj oBase, WnObj o) {
        String phBase = this.getTargetRelativePath(oBase);
        String phObj = this.getTargetRelativePath(o);

        return Disks.getRelativePath(phBase, phObj);
    }

    public String getTargetRelativePath(WnObj o) {
        // 得到资源的相对路径
        String rph = this.getRelativePath(o);

        // 如果对象不在 oHome 内，那么则会被 copy 一个统一的位置
        if (rph.startsWith("../")) {
            return "_copy/" + o.name();
        }

        // 嗯，返回吧
        return rph;
    }

    public WnObj createTarget(WnObj o) {
        // 得到资源的相对路径
        String rph = this.getTargetRelativePath(o);

        // 在目标处创建
        return createTarget(rph, o.race());

    }

    public WnObj createTarget(String rph, WnRace race) {
        return io.createIfNoExists(this.oDest, rph, race);

    }
}
