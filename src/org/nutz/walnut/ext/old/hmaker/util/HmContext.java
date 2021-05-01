package org.nutz.walnut.ext.old.hmaker.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.lang.Files;
import org.nutz.lang.Nums;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.old.hmaker.skin.HmSkinInfo;
import org.nutz.walnut.ext.old.hmaker.template.HmTemplate;
import org.nutz.walnut.ext.old.hmaker.template.HmTemplateInfo;

public class HmContext {

    /**
     * 执行转换的域名
     */
    public String domainName;

    /**
     * IO 接口
     */
    public WnIo io;

    /**
     * 数据接口的主目录 ~/.regapi，!!! 这个必须由创建来赋值
     */
    public WnObj oApiHome;

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
    public WnObj oSkinHome;
    public WnObj oSkinCss;
    public WnObj oSkinJs;
    public HmSkinInfo skinInfo;

    /**
     * 记录所有的控件使用的模板，最后 copy 到 template 目录
     */
    public Map<String, HmTemplate> templates;

    /**
     * 缓存所有的 dynamic 控件使用的 API 对象
     */
    private Map<String, WnObj> APIs;

    /**
     * 站点除了转换还要 copy 的资源
     */
    public Set<WnObj> resources;

    /**
     * true 为严格模式，这种情况下，所有的转换处理都需要尽量不容忍任何潜在的错误
     */
    public boolean strict;

    /**
     * 转换前需要预先分析，看看哪些页面是动态的，哪些是静态的
     */
    public Map<String, String> pageOutputNames;

    /**
     * 一个由 getProcessCount 函数维护的已完成的文件个数计数
     * <p>
     * 因此为了维持引用，所以采用一个元素的数组
     */
    private int[] processCount;

    public HmContext(WnIo io, String domainName) {
        this.domainName = domainName;
        this.io = io;
        this.templates = new HashMap<>();
        this.APIs = new HashMap<>();
        this.resources = new HashSet<>();
        this.pageOutputNames = new HashMap<>();
        this.processCount = Nums.array(0);
        this.__lib_codes = new HashMap<>();
    }

    protected HmContext(HmContext hpc) {
        this.domainName = hpc.domainName;
        this.io = hpc.io;
        this.resources = hpc.resources;
        this.oHome = hpc.oHome;
        this.oDest = hpc.oDest;
        this.oApiHome = hpc.oApiHome;
        this.oConfHome = hpc.oConfHome;
        this.oSkinHome = hpc.oSkinHome;
        this.oSkinCss = hpc.oSkinCss;
        this.oSkinJs = hpc.oSkinJs;
        this.skinInfo = hpc.skinInfo;
        this.templates = hpc.templates;
        this.APIs = hpc.APIs;
        this.strict = hpc.strict;
        this.pageOutputNames = hpc.pageOutputNames;
        this.processCount = hpc.processCount;
        this.__conf = hpc.__conf;
        this.__lib_codes = hpc.__lib_codes;
    }

    /**
     * @return 一个本站所有页面的元数据集合，键为对于站点的相对路径
     */
    public Map<String, NutBean> genSiteMap() {
        HashMap<String, NutBean> map = new HashMap<>();

        // 首先遍历本站所有的页面
        List<WnObj> children = io.getChildren(oHome, null);
        for (WnObj child : children) {
            this.__join_site_map(map, child);
        }

        return map;
    }

    private void __join_site_map(Map<String, NutBean> map, WnObj oPage) {
        // 目录: 递归
        if (oPage.isDIR()) {
            List<WnObj> children = io.getChildren(oPage, null);
            for (WnObj child : children) {
                this.__join_site_map(map, child);
            }
        }
        // 文件的话，看看是否有后缀，如果没有后缀的话，就加入
        else if (oPage.isMime("text/html")) {
            if (Strings.isBlank(Files.getSuffixName(oPage.name()))) {
                // 得到相对路径
                String rph = this.getRelativePath(oPage);
                NutBean bean = oPage.pickBy("^(nm|title|hm_.+)$");

                // 计入
                map.put(rph, bean);
            }
        }
    }

    /**
     * @param doCount
     *            是否增加计数
     * @return 返回已经处理的文件个数
     */
    public int getProcessCount(boolean doCount) {
        if (doCount) {
            this.processCount[0] = Math.min(this.processCount[0] + 1, __process_sum());
        }
        return this.processCount[0];
    }

    private int __process_sum() {
        return this.templates.size() + this.resources.size() + this.pageOutputNames.size();
    }

    public String getProcessInfo(boolean doCount) {
        int sum = __process_sum();
        return String.format("%%[%3d/%s]", this.getProcessCount(doCount), sum);
    }

    public void markPrcessDone() {
        this.processCount[0] = __process_sum();
    }

    public String getProcessInfoAndDoCount() {
        return getProcessInfo(true);
    }

    /**
     * 遍历整个站点全部的页面，判断这些页面到底是动态还是静态的
     */
    public void preparePages() {
        // 开始遍历
        io.walk(oHome, new Callback<WnObj>() {
            public void invoke(WnObj o) {
                // 得到相对路径
                String rph = getRelativePath(o);

                // 如果需要转换，转换出来都是 html 文件，只不过动态的在真正转换的时候会打上 "as_wnml:true" 的元数据
                if (Hms.isNeedTranslate(o)) {
                    pageOutputNames.put(rph, o.name() + ".html");
                }
            }
        }, WalkMode.LEAF_ONLY);
    }

    public WnObj getApiObj(String api) {
        WnObj oApi = APIs.get(api);
        if (null == oApi) {
            if (null != oApiHome) {
                if (api.startsWith("/"))
                    api = api.substring(1);
                oApi = io.check(oApiHome, api);
                APIs.put(api, oApi);
            }
        }
        return oApi;
    }

    public HmTemplate getTemplate(String templateName) {
        HmTemplate tmpl = this.templates.get(templateName);
        if (null == tmpl) {
            tmpl = new HmTemplate();
            WnObj oTmplHome = io.check(oConfHome, "template/" + templateName);

            // jQuery 插件
            tmpl.oJs = io.check(oTmplHome, "jquery.fn.js");

            // 解析模板信息
            WnObj oInfo = io.check(oTmplHome, "template.info.json");
            tmpl.info = io.readJson(oInfo, HmTemplateInfo.class);
            tmpl.info.evalOptions();

            // 得到模板的服务器模板
            String dfnm = tmpl.info.getDomFileName("dom.wnml");
            WnObj oDomNm = io.fetch(oTmplHome, dfnm);
            if (null != oDomNm) {
                tmpl.dom = Tmpl.parse(io.readText(oDomNm));
            }

            // 计入
            this.templates.put(templateName, tmpl);
        }
        return tmpl;
    }

    /**
     * 缓存组件的代码
     */
    private Map<String, String> __lib_codes;

    /**
     * 获取组件内容（懒加载）
     * 
     * @param libName
     *            组件名称
     * @return 组件内容代码
     */
    public String getLibCode(String libName) {
        String re = __lib_codes.get(libName);
        if (null == re) {
            WnObj oLib = io.check(oHome, "lib/" + libName);
            re = io.readText(oLib);
            __lib_codes.put(libName, re);
        }
        return re;
    }

    private NutMap __conf;

    /**
     * 读取配置目录下的配置文件 "~/.hmaker/hmaker.conf"
     * 
     * @return 配置内容
     */
    public NutMap getConf() {
        if (null == __conf) {
            WnObj oConf = io.fetch(oConfHome, "hmaker.conf");
            if (null == oConf) {
                __conf = new NutMap();
            } else {
                __conf = io.readJson(oConf, NutMap.class);
            }
        }
        return __conf;
    }

    public boolean hasSkin() {
        return null != oSkinHome && null != skinInfo && null != oSkinCss;
    }

    public String getRelativePath(WnObj o) {
        String phHome = oHome.getRegularPath();
        String phObj = o.getRegularPath();
        return Disks.getRelativePath(phHome, phObj);
    }

    public String getRelativeDestPath(WnObj oTa) {
        String phDest = oDest.getRegularPath();
        String phTa = oTa.getRegularPath();
        return Disks.getRelativePath(phDest, phTa);
    }

    public String getRelativePath(WnObj oBase, WnObj o) {
        String phBase = this.getTargetRelativePath(oBase);
        String phObj = this.getTargetRelativePath(o);

        return Disks.getRelativePath(phBase, phObj, o.isDIR() ? "" : o.name());
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
