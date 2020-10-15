package org.nutz.walnut.ext.entity.statistics;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

public class WnStatAggreConfig extends WnStatisticsConfig {

    private NutMap query;

    private String dateTimeBy;

    private String dateFormat;

    private String groupBy;

    private NutMap mapping;

    private String markBy;

    private String markUnit;

    private String markRemain;

    public NutMap getQuery() {
        return query;
    }

    public void setQuery(NutMap query) {
        this.query = query;
    }

    public String getDateTimeBy() {
        return dateTimeBy;
    }

    public void setDateTimeBy(String dateTimeBy) {
        this.dateTimeBy = dateTimeBy;
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

    public boolean hasMarkBy() {
        return !Strings.isBlank(this.markBy);
    }

    public String getMarkBy() {
        return markBy;
    }

    public void setMarkBy(String markBy) {
        this.markBy = markBy;
    }

    public String getMarkUnit() {
        return markUnit;
    }

    public void setMarkUnit(String markUnit) {
        this.markUnit = markUnit;
    }

    public String getMarkRemain() {
        return markRemain;
    }

    public void setMarkRemain(String markRemain) {
        this.markRemain = markRemain;
    }

}
