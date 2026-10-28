package com.site0.walnut.security.captcha.impl.tianai;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.cache.simple.WnSimpleCache;
import com.site0.walnut.security.captcha.bean.WnCaptchaConfig;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

import cloud.tianai.captcha.resource.CrudResourceStore;
import cloud.tianai.captcha.resource.FontCache;
import cloud.tianai.captcha.resource.ImageCaptchaResourceManager;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.common.model.dto.ResourceMap;
import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;

/**
 * 基于 Walnut 对象存储（{@link WnIo}）的 tianai 资源存储器。
 *
 * <p>
 * 背景图（和自定义字体）从 {@link WnCaptchaConfig#getBgImgDir()} 指向的
 * 对象存储目录随机读取，资源类型为 <code>wnio</code>，由
 * {@link WnIoResourceProvider} 负责打开流；目录列表按
 * {@link WnCaptchaConfig#getBgImgCache()} 用 {@link WnSimpleCache} 缓存，
 * 0 表示完全不缓存。
 *
 * <p>
 * 模板图（滑块缺口/旋转轮廓）和内置字体走内置的
 * {@link LocalMemoryResourceStore}（classpath 资源），委托处理。
 */
public class WnIoCaptchaResourceStore implements CrudResourceStore {

    /**
     * 背景图文件的后缀白名单
     */
    private static final String[] BG_SUFFIXES = {"jpg", "jpeg", "png", "webp", "bmp", "gif"};

    private WnIo io;

    private NutBean vars;

    private WnCaptchaConfig conf;

    /**
     * 模板与内置字体的本地存储（委托对象）
     */
    private LocalMemoryResourceStore local = new LocalMemoryResourceStore();

    /**
     * 背景图目录列表的缓存，null 表示不缓存
     */
    private WnSimpleCache<List<Resource>> bgCache;

    public WnIoCaptchaResourceStore(WnIo io, NutBean vars, WnCaptchaConfig conf) {
        this.io = io;
        this.vars = vars;
        this.conf = conf;
        if (!Ws.isBlank(conf.getBgImgDir()) && conf.getBgImgCache() > 0) {
            this.bgCache = new WnSimpleCache<>(10, 100, (int) (conf.getBgImgCache() / 1000));
        }
    }

    @Override
    public void init(ImageCaptchaResourceManager resourceManager) {
        local.init(resourceManager);
    }

    @Override
    public List<Resource> randomGetResourceByTypeAndTag(String type, String tag, Integer quantity) {
        // 字体：配置了 fontPath 则从对象存储读，否则用内置字体
        if (FontCache.FONT_TYPE.equals(type)) {
            if (!Ws.isBlank(conf.getFontPath())) {
                List<Resource> list = new ArrayList<>(1);
                list.add(new Resource(WnIoResourceProvider.TYPE, conf.getFontPath()));
                return list;
            }
            return local.randomGetResourceByTypeAndTag(type, tag, quantity);
        }
        // 背景图：配置了 bgImgDir 则从对象存储随机取
        if (!Ws.isBlank(conf.getBgImgDir())) {
            List<Resource> bgs = loadBgImages();
            return randomPick(bgs, quantity);
        }
        return local.randomGetResourceByTypeAndTag(type, tag, quantity);
    }

    @Override
    public List<ResourceMap> randomGetTemplateByTypeAndTag(String type, String tag, Integer quantity) {
        return local.randomGetTemplateByTypeAndTag(type, tag, quantity);
    }

    /**
     * 读取背景图目录，包装成 wnio 资源列表
     */
    private List<Resource> loadBgImages() {
        // 缓存命中？
        if (null != bgCache) {
            List<Resource> cached = bgCache.get("bg");
            if (null != cached) {
                return cached;
            }
        }
        // 列目录
        WnObj oDir = Wn.checkObj(io, vars, conf.getBgImgDir());
        List<WnObj> children = io.getChildren(oDir, null);
        List<Resource> bgs = new ArrayList<>(children.size());
        for (WnObj child : children) {
            if (child.isFILE() && isBgImage(child.name())) {
                String ph = Wn.appendPath(conf.getBgImgDir(), child.name());
                bgs.add(new Resource(WnIoResourceProvider.TYPE, ph));
            }
        }
        // 目录里一张图都没有，配置错了
        if (bgs.isEmpty()) {
            throw Er.create("e.captcha.NoBgImage", conf.getBgImgDir());
        }
        // 写缓存
        if (null != bgCache) {
            bgCache.put("bg", bgs);
        }
        return bgs;
    }

    private boolean isBgImage(String name) {
        String suffix = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        for (String bgSuffix : BG_SUFFIXES) {
            if (bgSuffix.equals(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从列表里随机取 quantity 个（不足则全取）
     */
    private List<Resource> randomPick(List<Resource> bgs, Integer quantity) {
        int n = null == quantity ? 1 : quantity;
        if (n >= bgs.size()) {
            return new ArrayList<>(bgs);
        }
        List<Resource> re = new ArrayList<>(n);
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < n; i++) {
            re.add(bgs.get(random.nextInt(bgs.size())));
        }
        return re;
    }

    // ---------- 以下委托给本地存储（模板与内置资源） ----------

    @Override
    public void addResource(String type, Resource resource) {
        local.addResource(type, resource);
    }

    @Override
    public void addTemplate(String type, ResourceMap template) {
        local.addTemplate(type, template);
    }

    @Override
    public Resource deleteResource(String type, String id) {
        return local.deleteResource(type, id);
    }

    @Override
    public ResourceMap deleteTemplate(String type, String id) {
        return local.deleteTemplate(type, id);
    }

    @Override
    public List<Resource> listResourcesByTypeAndTag(String type, String tag) {
        return local.listResourcesByTypeAndTag(type, tag);
    }

    @Override
    public List<ResourceMap> listTemplatesByTypeAndTag(String type, String tag) {
        return local.listTemplatesByTypeAndTag(type, tag);
    }

    @Override
    public void clearAllTemplates() {
        local.clearAllTemplates();
    }

    @Override
    public void clearAllResources() {
        local.clearAllResources();
    }

}
