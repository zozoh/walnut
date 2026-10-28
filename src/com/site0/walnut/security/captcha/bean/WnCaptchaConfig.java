package com.site0.walnut.security.captcha.bean;

/**
 * <code>~/.captcha/{name}.json</code> 的服务配置。
 *
 * 字段语义是抽象尺度，具体多大误差算通过、背景图怎么用，
 * 由验证码实现类自行理解（当前默认实现为 tianai）。
 */
public class WnCaptchaConfig {

    /**
     * 验证码类型，取值由实现定义（tianai：SLIDER / ROTATE / CONCAT /
     * WORD_IMAGE_CLICK / POW）
     */
    private String type = "SLIDER";

    /**
     * 答案缓存过期时间（毫秒），到点作废需重新获取
     */
    private long expire = 10000;

    /**
     * 校验容错档位：1-10 的整数，数值越大越宽松（越容易通过）。
     * 具体实现负责把档位换算成底层库的容错值
     */
    private int tolerant = 5;

    /**
     * 滑块干扰：生成迷惑性缺口
     */
    private boolean obfuscate = false;

    /**
     * 背景图目录：对象存储里的一个图片目录，里面放一组图片，
     * 每次生成验证码时随机取一张做背景（建议 600x360）。
     * 留空表示用实现自带的默认背景图
     */
    private String bgImgDir;

    /**
     * 背景图目录列表的缓存毫秒数（WnSimpleCache），0 表示完全不缓存
     */
    private long bgImgCache = 600000;

    /**
     * 点选验证码的字体（对象存储里的字体文件路径）；
     * 留空表示本验证码不支持文字点选类型
     */
    private String fontPath;

    /**
     * 临时票据的过期时间（毫秒）
     */
    private long ticketExpire = 600000;

    /**
     * 答案与票据的存储配置
     */
    private WnCaptchaStoreConfig store = new WnCaptchaStoreConfig();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public int getTolerant() {
        return tolerant;
    }

    public void setTolerant(int tolerant) {
        this.tolerant = tolerant;
    }

    public boolean isObfuscate() {
        return obfuscate;
    }

    public void setObfuscate(boolean obfuscate) {
        this.obfuscate = obfuscate;
    }

    public String getBgImgDir() {
        return bgImgDir;
    }

    public void setBgImgDir(String bgImgDir) {
        this.bgImgDir = bgImgDir;
    }

    public long getBgImgCache() {
        return bgImgCache;
    }

    public void setBgImgCache(long bgImgCache) {
        this.bgImgCache = bgImgCache;
    }

    public String getFontPath() {
        return fontPath;
    }

    public void setFontPath(String fontPath) {
        this.fontPath = fontPath;
    }

    public long getTicketExpire() {
        return ticketExpire;
    }

    public void setTicketExpire(long ticketExpire) {
        this.ticketExpire = ticketExpire;
    }

    public WnCaptchaStoreConfig getStore() {
        return store;
    }

    public void setStore(WnCaptchaStoreConfig store) {
        this.store = store;
    }

}
