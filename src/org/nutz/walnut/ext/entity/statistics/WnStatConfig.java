package org.nutz.walnut.ext.entity.statistics;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

public class WnStatConfig {

    private String srcDao;

    private String srcTableName;

    private NutMap query;

    private String srcTimeBy;

    private String groupBy;

    private String dateFormat;

    private NutMap mapping;

    private String timeUnit;

    public String getSrcDao() {
        return srcDao;
    }

    public void setSrcDao(String srcDao) {
        this.srcDao = srcDao;
    }

    public String getSrcTableName() {
        return srcTableName;
    }

    public void setSrcTableName(String srcTableName) {
        this.srcTableName = srcTableName;
    }

    public boolean hasQuery() {
        return null != query && query.size() > 0;
    }

    public NutMap getQuery() {
        return query;
    }

    public void setQuery(NutMap query) {
        this.query = query;
    }

    public String getSrcTimeBy(String dft) {
        return Strings.sBlank(srcTimeBy, dft);
    }

    public String getSrcTimeBy() {
        return srcTimeBy;
    }

    public void setSrcTimeBy(String dateTimeBy) {
        this.srcTimeBy = dateTimeBy;
    }

    public String getDateFormat(String dft) {
        return Strings.sBlank(dateFormat, dft);
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public NutMap getMapping() {
        return mapping;
    }

    public void setMapping(NutMap mapping) {
        this.mapping = mapping;
    }

    public String getTimeUnit(String dft) {
        return Strings.sBlank(timeUnit, dft);
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String markUnit) {
        this.timeUnit = markUnit;
    }

}
