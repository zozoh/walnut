package org.nutz.walnut.ext.titanium.creation;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnRace;

public class TiTypeInfo {

    private String name;

    private WnRace race;

    private String icon;

    private String thumb;

    private String mime;

    private String text;

    private String brief;

    private String help;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WnRace getRace() {
        return race;
    }

    public void setRace(WnRace race) {
        this.race = race;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public boolean hasMime() {
        return !Strings.isBlank(mime);
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public boolean isHelpReferToFile() {
        return null != help && (help.startsWith("./") || help.startsWith("/"));
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

}
