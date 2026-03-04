package com.site0.walnut.ext.data.sqlx.hislog;

public class HisConfigTrimData {

    /**
     * 需要裁剪的历史记录字段名称
     */
    private String name;

    /**
     * 字段最大尺寸(Bytes)
     * 
     * 譬如对于 SQL Text 字段，通常为 65535。
     * 
     * 处理程序，会跟据字段所占的字节大小进行裁剪，而不是根据字符的长度
     */
    private int maxSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

}
