package org.nutz.walnut.ext.data.sqlx.loader;

public class SqlTmplEntry {

    private String key;

    private SqlType type;

    private String[] defaultPick;

    private String[] defaultOmit;

    private Boolean defaultIgnoreNil;

    private String content;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public SqlType getType() {
        return type;
    }

    public void setType(SqlType type) {
        this.type = type;
    }

    public String[] getDefaultPick() {
        return defaultPick;
    }

    public void setDefaultPick(String[] defaultPick) {
        this.defaultPick = defaultPick;
    }

    public String[] getDefaultOmit() {
        return defaultOmit;
    }

    public void setDefaultOmit(String[] defaultOmit) {
        this.defaultOmit = defaultOmit;
    }

    public Boolean getDefaultIgnoreNil() {
        return defaultIgnoreNil;
    }

    public void setDefaultIgnoreNil(Boolean defaultIgnoreNil) {
        this.defaultIgnoreNil = defaultIgnoreNil;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
