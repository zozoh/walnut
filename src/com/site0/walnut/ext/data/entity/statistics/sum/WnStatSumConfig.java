package com.site0.walnut.ext.data.entity.statistics.sum;

import org.nutz.lang.Strings;
import com.site0.walnut.ext.data.entity.statistics.WnStatConfig;

public class WnStatSumConfig extends WnStatConfig {

    private String sumBy;

    private String nameBy;

    private String valueBy;

    private String cacheDir;

    private String cacheName;

    private String cacheDateFormat;

    public String getSumBy(String dft) {
        return Strings.sBlank(sumBy, dft);
    }

    public String getSumBy() {
        return sumBy;
    }

    public void setSumBy(String valueBy) {
        this.sumBy = valueBy;
    }

    public String getNameBy(String dft) {
        return Strings.sBlank(nameBy, dft);
    }

    public String getNameBy() {
        return nameBy;
    }

    public void setNameBy(String nameBy) {
        this.nameBy = nameBy;
    }

    public String getValueBy(String dft) {
        return Strings.sBlank(valueBy, dft);
    }

    public String getValueBy() {
        return valueBy;
    }

    public void setValueBy(String valueBy) {
        this.valueBy = valueBy;
    }

    public boolean hasCacheDir() {
        return !Strings.isBlank(cacheDir);
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    public String getCacheName(String dft) {
        return Strings.sBlank(cacheName, dft);
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public String getCacheDateFormat(String dft) {
        return Strings.sBlank(cacheDateFormat, dft);
    }

    public String getCacheDateFormat() {
        return cacheDateFormat;
    }

    public void setCacheDateFormat(String cacheDateFormat) {
        this.cacheDateFormat = cacheDateFormat;
    }

}
