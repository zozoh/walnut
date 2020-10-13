package org.nutz.walnut.ext.entity.history.fake;

import java.util.Random;

import org.nutz.lang.tmpl.Tmpl;

public class HistoryFakeField {

    private String schema;

    private String gen;

    private String key;

    private String value;

    private String[] cans;

    private Tmpl genTmpl;

    public boolean hasSchema() {
        return null != schema;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getGen() {
        return gen;
    }

    public void setGen(String gen) {
        this.gen = gen;
        this.genTmpl = Tmpl.parse(gen);
    }

    public boolean hasGenTmpl() {
        return null != this.genTmpl;
    }

    public Tmpl getGenTmpl() {
        return genTmpl;
    }

    public boolean hasKey() {
        return null != key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean hasValue() {
        return null != value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean hasCans() {
        return null != cans && cans.length > 0;
    }

    public Object[] getCans() {
        return cans;
    }

    public void setCans(String[] cans) {
        this.cans = cans;
    }

    private static Random RAD = new Random(System.currentTimeMillis());
    
    public String getOneCan() {
        if (!this.hasCans()) {
            return null;
        }
        int index = RAD.nextInt(this.cans.length);
        return this.cans[index];
    }

}
