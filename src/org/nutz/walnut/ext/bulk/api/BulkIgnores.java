package org.nutz.walnut.ext.bulk.api;

public class BulkIgnores {

    private String[] ignorePaths;

    private boolean ignoreHiddden;

    private boolean ignoreExpi;

    private boolean ignoreThumbnail;

    private String ignoreMetaKeys;

    private String[] whiteTypes;

    private String[] blackTypes;

    private String[] whiteMimes;

    private String[] blackMimes;

    public String[] getIgnorePaths() {
        return ignorePaths;
    }

    public void setIgnorePaths(String[] ignorePaths) {
        this.ignorePaths = ignorePaths;
    }

    public boolean isIgnoreHiddden() {
        return ignoreHiddden;
    }

    public void setIgnoreHiddden(boolean ignoreHiddden) {
        this.ignoreHiddden = ignoreHiddden;
    }

    public boolean isIgnoreExpi() {
        return ignoreExpi;
    }

    public void setIgnoreExpi(boolean ignoreExpi) {
        this.ignoreExpi = ignoreExpi;
    }

    public boolean isIgnoreThumbnail() {
        return ignoreThumbnail;
    }

    public void setIgnoreThumbnail(boolean ignoreThumbnail) {
        this.ignoreThumbnail = ignoreThumbnail;
    }

    public String getIgnoreMetaKeys() {
        return ignoreMetaKeys;
    }

    public void setIgnoreMetaKeys(String ignoreMetaKeys) {
        this.ignoreMetaKeys = ignoreMetaKeys;
    }

    public String[] getWhiteTypes() {
        return whiteTypes;
    }

    public void setWhiteTypes(String[] whiteTypes) {
        this.whiteTypes = whiteTypes;
    }

    public String[] getBlackTypes() {
        return blackTypes;
    }

    public void setBlackTypes(String[] blackTypes) {
        this.blackTypes = blackTypes;
    }

    public String[] getWhiteMimes() {
        return whiteMimes;
    }

    public void setWhiteMimes(String[] whiteMimes) {
        this.whiteMimes = whiteMimes;
    }

    public String[] getBlackMimes() {
        return blackMimes;
    }

    public void setBlackMimes(String[] blackMimes) {
        this.blackMimes = blackMimes;
    }

}
