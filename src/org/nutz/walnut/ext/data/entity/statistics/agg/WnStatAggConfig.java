package org.nutz.walnut.ext.data.entity.statistics.agg;

import org.nutz.lang.Strings;
import org.nutz.walnut.ext.data.entity.statistics.WnStatConfig;

public class WnStatAggConfig extends WnStatConfig {

    private String targetDao;

    private String targetTableName;

    private String targetTimeBy;

    private Object sumBy;

    private String markBy;

    private String markRemain;

    public String getTargetDao() {
        return targetDao;
    }

    public void setTargetDao(String targetDao) {
        this.targetDao = targetDao;
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    public void setTargetTableName(String targetTableName) {
        this.targetTableName = targetTableName;
    }

    public String getTargetTimeBy(String dft) {
        return Strings.sBlank(targetTimeBy, dft);
    }

    public String getTargetTimeBy() {
        return targetTimeBy;
    }

    public void setTargetTimeBy(String targetTimeBy) {
        this.targetTimeBy = targetTimeBy;
    }

    public AggSumBy getAggSumBy() {
        return new AggSumBy(sumBy);
    }

    public Object getSumBy() {
        return sumBy;
    }

    public void setSumBy(Object sumBy) {
        this.sumBy = sumBy;
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

    public String getMarkRemain(String dft) {
        return Strings.sBlank(markRemain, dft);
    }

    public String getMarkRemain() {
        return markRemain;
    }

    public void setMarkRemain(String markRemain) {
        this.markRemain = markRemain;
    }

}
