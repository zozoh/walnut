package com.site0.walnut.core.mapping.support;

import com.site0.walnut.util.Ws;

public class SqlIoOptions {

    private boolean useTimestamp = false;

    private boolean useD0D1 = false;

    private boolean useCreator = false;

    private boolean useMender = false;

    private boolean useGroup = false;

    private boolean useMode = false;

    private boolean useParentId = false;

    public SqlIoOptions(String str) {
        String[] ss = Ws.splitIgnoreBlank(str);
        if (null != ss) {
            for (String s : ss) {
                // AMS;
                if ("AMS".equals(s)) {
                    this.useTimestamp = true;
                }
                // d0d1;
                else if ("d0d1".equals(s)) {
                    this.useD0D1 = true;
                }
                // cmg
                else if (s.matches("^[cmg]+$")) {
                    this.useCreator = s.contains("c");
                    this.useMender = s.contains("m");
                    this.useGroup = s.contains("g");
                }
                // mode
                else if ("md".equals(s)) {
                    this.useMode = true;
                }
                // pid
                else if ("pid".equals(s)) {
                    this.useParentId = true;
                }
            }
        }
    }

    public boolean isUseTimestamp() {
        return useTimestamp;
    }

    public boolean isUseD0D1() {
        return useD0D1;
    }

    public boolean isUseCreator() {
        return useCreator;
    }

    public boolean isUseMender() {
        return useMender;
    }

    public boolean isUseGroup() {
        return useGroup;
    }

    public boolean isUseMode() {
        return useMode;
    }

    public boolean isUseParentId() {
        return useParentId;
    }

}
