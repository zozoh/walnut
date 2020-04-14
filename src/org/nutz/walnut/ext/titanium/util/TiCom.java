package org.nutz.walnut.ext.titanium.util;

import org.nutz.lang.util.NutMap;

public class TiCom {

    private String path;

    private String name;

    private String icon;

    private String title;

    private boolean manual;

    private boolean tutorial;

    private boolean video;

    private String[] screen;

    private String editComType;

    private NutMap editComConf;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public boolean isTutorial() {
        return tutorial;
    }

    public void setTutorial(boolean tutorial) {
        this.tutorial = tutorial;
    }

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public String[] getScreen() {
        return screen;
    }

    public void setScreen(String[] screen) {
        this.screen = screen;
    }

    public String getEditComType() {
        return editComType;
    }

    public void setEditComType(String editComType) {
        this.editComType = editComType;
    }

    public NutMap getEditComConf() {
        return editComConf;
    }

    public void setEditComConf(NutMap editComConf) {
        this.editComConf = editComConf;
    }

}
