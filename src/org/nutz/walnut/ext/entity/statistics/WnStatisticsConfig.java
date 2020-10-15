package org.nutz.walnut.ext.entity.statistics;

public class WnStatisticsConfig {

    private String srcDao;

    private String srcTableName;

    private String targetDao;

    private String targetTableName;

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

}
