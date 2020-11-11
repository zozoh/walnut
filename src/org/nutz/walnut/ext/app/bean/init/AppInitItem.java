package org.nutz.walnut.ext.app.bean.init;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public class AppInitItem {

    private AppInitItemType type;

    private NutMap properties;

    private String path;

    private String linkPath;

    private NutMap meta;

    private String content;

    private boolean contentAsTmpl;

    private boolean overrideContent;

    private String contentFilePath;

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        if (obj instanceof AppInitItem) {
            AppInitItem item = (AppInitItem) obj;
            // 类型
            if (!this.isSameType(item))
                return false;
            // 属性
            if (this.hasProperties()) {
                if (item.hasProperties() && !properties.equals(item.properties)) {
                    return false;
                }
            } else if (item.hasProperties()) {
                return false;
            }

            // 路径
            if (this.hasPath()) {
                if (item.hasPath() && !path.equals(item.path)) {
                    return false;
                }
            } else if (item.hasPath()) {
                return false;
            }

            // 链接路径
            if (this.hasLinkPath()) {
                if (item.hasLinkPath() && !linkPath.equals(item.linkPath)) {
                    return false;
                }
            } else if (item.hasLinkPath()) {
                return false;
            }

            // 元数据
            if (this.hasMeta()) {
                if (item.hasMeta() && !meta.equals(item.meta)) {
                    return false;
                }
            } else if (item.hasMeta()) {
                return false;
            }

            // 内容
            if (this.hasContent()) {
                if (item.hasContent() && !content.equals(item.content)) {
                    return false;
                }
            } else if (item.hasContent()) {
                return false;
            }

            // 标志位
            if (contentAsTmpl != item.contentAsTmpl)
                return false;

            if (overrideContent != item.overrideContent)
                return false;

            // 内容
            if (this.hasContentFilePath()) {
                if (item.hasContentFilePath() && !contentFilePath.equals(item.contentFilePath)) {
                    return false;
                }
            } else if (item.hasContentFilePath()) {
                return false;
            }

            // 嗯，一定是相同的
            return true;
        }

        return false;
    }

    @Override
    protected AppInitItem clone() {
        AppInitItem item = new AppInitItem();
        item.type = this.type;
        if (this.hasProperties())
            item.properties = this.properties;
        item.path = this.path;
        item.linkPath = this.linkPath;
        if (this.hasMeta())
            item.meta = this.meta.duplicate();
        item.content = this.content;
        item.contentAsTmpl = this.contentAsTmpl;
        item.overrideContent = this.overrideContent;
        item.contentFilePath = this.contentFilePath;
        return item;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("@");

        // 类型
        if (null == type) {
            sb.append('?');
        } else {
            sb.append(type.toString());
        }

        // 属性
        if (this.hasProperties()) {
            sb.append(Json.toJson(properties));
        }

        // 路径
        if (this.hasPath()) {
            sb.append(' ').append(path);
        }

        // 链接路径
        if (this.hasLinkPath()) {
            sb.append(" -> ").append(linkPath);
        }

        // 元数据
        if (this.hasMeta()) {
            sb.append('\n').append(Json.toJson(meta, JsonFormat.nice().setQuoteName(true)));
        }

        // 内容
        if (this.hasContent()) {
            sb.append(overrideContent ? '%' : '?');
            sb.append(contentAsTmpl ? "TMPL:" : "COPY:");
            sb.append('\n');
            sb.append(content);
            sb.append("%END%");
        }
        // 链接内容
        else if (this.hasContentFilePath()) {
            sb.append(overrideContent ? '%' : '?');
            sb.append(contentAsTmpl ? "TMPL>" : "COPY>");
            sb.append(' ');
            sb.append(contentFilePath);
        }

        return sb.toString();
    }

    public boolean isDIR() {
        return AppInitItemType.DIR == type;
    }

    public boolean isFILE() {
        return AppInitItemType.FILE == type;
    }

    public boolean isTHING() {
        return AppInitItemType.THING == type;
    }

    public boolean isAPI() {
        return AppInitItemType.API == type;
    }

    public boolean isENV() {
        return AppInitItemType.ENV == type;
    }

    public boolean isHOME() {
        return AppInitItemType.HOME == type;
    }

    public boolean hasType() {
        return null != type;
    }

    public boolean isSameType(AppInitItem item) {
        if (null == item)
            return false;
        if (!this.hasType())
            return !item.hasType();

        if (!item.hasType())
            return false;

        return type.equals(item.type);
    }

    public AppInitItemType getType() {
        return type;
    }

    public void setType(String type) {
        this.type = AppInitItemType.valueOf(type.toUpperCase());
    }

    public void setType(AppInitItemType type) {
        this.type = type;
    }

    public boolean hasProperties() {
        return null != properties && !properties.isEmpty();
    }

    public NutMap getProperties() {
        return properties;
    }

    public void setProperties(String json, NutBean vars) {
        if (null != vars) {
            json = Tmpl.exec(json, vars);
        }
        this.properties = Lang.map(json);
    }

    public void setProperties(NutMap properties) {
        this.properties = properties;
    }

    public boolean hasPath() {
        return !Strings.isBlank(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path, NutBean vars) {
        if (null != vars) {
            this.path = Tmpl.exec(path, vars);
        } else {
            this.path = path;
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean hasLinkPath() {
        return !Strings.isBlank(linkPath);
    }

    public String getLinkPath() {
        return linkPath;
    }

    public void setLinkPath(String linkPath, NutBean vars) {
        if (null != vars) {
            this.linkPath = Tmpl.exec(linkPath, vars);
        } else {
            this.linkPath = linkPath;
        }
    }

    public void setLinkPath(String linkPath) {
        this.linkPath = linkPath;
    }

    public boolean hasMeta() {
        return null != meta && !meta.isEmpty();
    }

    public NutMap getMeta() {
        return meta;
    }

    public void setMeta(String json, NutBean vars) {
        if (null != vars) {
            json = Tmpl.exec(json, vars);
        }
        this.meta = Lang.map(json);
    }

    public void setMeta(NutMap meta) {
        this.meta = meta;
    }

    public boolean hasContent() {
        return null != content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isContentAsTmpl() {
        return contentAsTmpl;
    }

    public void setContentAsTmpl(boolean contentAsTmpl) {
        this.contentAsTmpl = contentAsTmpl;
    }

    public boolean isOverrideContent() {
        return overrideContent;
    }

    public void setOverrideContent(boolean contentOverride) {
        this.overrideContent = contentOverride;
    }

    public boolean hasContentFilePath() {
        return !Strings.isBlank(contentFilePath);
    }

    public String getContentFilePath() {
        return contentFilePath;
    }

    public void setContentFilePath(String contentFilePath) {
        this.contentFilePath = contentFilePath;
    }

}
