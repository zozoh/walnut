package org.nutz.walnut.ext.data.site.render;

import org.nutz.lang.util.NutMap;

/**
 * 指定了一个站点渲染时的配置信息
 * 
 * 参见 <code>cmd_site_render.md</code>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class SitePageRenderConfig {

    /**
     * 站点的原始路径，将根据这个路径下的内容进行站点发布
     */
    private String home;

    /**
     * 默认输出目录
     */
    private String target;

    /**
     * 全局上下文变量
     */
    private NutMap vars;

    /**
     * 渲染哪些归档
     */
    private SiteRenderArchive[] archives;

    /**
     * 还需要额外渲染哪些静态网页
     */
    private String[] pages;

    /**
     * 需要复制哪些静态资源
     */
    private String[] copyFiles;

    /**
     * 渲染的语言，如果不声明，则虚渲染时将没有 ${lang}<br>
     * 多个语言将导致一个归档杯渲染多次
     */
    private String[] langs;

    /**
     * 输出的的html路径模板
     */
    private String path;

    /**
     * 采用哪个模板输出
     */
    private String html;

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public boolean hasVars() {
        return null != vars && !vars.isEmpty();
    }

    public NutMap getVars() {
        return vars;
    }

    public void setVars(NutMap vars) {
        this.vars = vars;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean hasArchives() {
        return null != this.archives && this.archives.length > 0;
    }

    public SiteRenderArchive[] getArchives() {
        return archives;
    }

    public void setArchives(SiteRenderArchive[] archives) {
        this.archives = archives;
    }

    public String[] getPages() {
        return pages;
    }

    public void setPages(String[] pages) {
        this.pages = pages;
    }

    public boolean hasCopyFiles() {
        return null != copyFiles && copyFiles.length > 0;
    }

    public String[] getCopyFiles() {
        return copyFiles;
    }

    public void setCopyFiles(String[] copyFiles) {
        this.copyFiles = copyFiles;
    }

    public String[] getLangs() {
        return langs;
    }

    public void setLangs(String[] langs) {
        this.langs = langs;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

}
