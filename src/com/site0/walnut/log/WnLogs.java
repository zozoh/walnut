package com.site0.walnut.log;

/**
 * Walnut 日志工厂（RM-NUTZ-11）。
 * <p>
 * 入口与现有 {@code com.site0.walnut.util.Wlog} 对齐（MAIN/APP/CMD/IO/AUTH/BOX/
 * HOOK/SQL/AC/BG_*），RM-NUTZ-12 起全库改用本工厂；{@code util.Wlog} 保留为薄适配，
 * 待后续统一。
 * <p>
 * 实现直接用 log4j2；不依赖 {@code org.nutz.log}。
 */
public final class WnLogs {

    public static final int LEVEL_FATAL = 50;

    public static final int LEVEL_ERROR = 40;

    public static final int LEVEL_WARN = 30;

    public static final int LEVEL_INFO = 20;

    public static final int LEVEL_DEBUG = 10;

    public static final int LEVEL_TRACE = 0;

    private WnLogs() {}

    /**
     * 解析日志级别字符串（对齐 Nutz {@code AbstractLog.level}）：
     * F/fatal、E/error、W/warn、I/info、D/debug、T/trace；非法且 strict=false 时返回 INFO。
     */
    public static int level(String str) {
        return level(str, false);
    }

    public static int level(String str, boolean strict) {
        if (null != str) {
            if ("F".equals(str) || "fatal".equals(str))
                return LEVEL_FATAL;
            if ("E".equals(str) || "error".equals(str))
                return LEVEL_ERROR;
            if ("W".equals(str) || "warn".equals(str))
                return LEVEL_WARN;
            if ("I".equals(str) || "info".equals(str))
                return LEVEL_INFO;
            if ("D".equals(str) || "debug".equals(str))
                return LEVEL_DEBUG;
            if ("T".equals(str) || "trace".equals(str))
                return LEVEL_TRACE;
        }
        if (strict)
            throw new IllegalArgumentException("invalid log level: " + str);
        return LEVEL_INFO;
    }

    /** 按名称取日志器。 */
    public static WnLog get(String name) {
        return new WnLog4j(null == name || name.isEmpty() ? "MAIN" : name);
    }

    /** 默认日志器（MAIN）。 */
    public static WnLog get() {
        return getMain();
    }

    public static WnLog getMain() {
        return get("MAIN");
    }

    public static WnLog getApp() {
        return get("APP");
    }

    public static WnLog getCmd() {
        return get("CMD");
    }

    public static WnLog getIO() {
        return get("IO");
    }

    public static WnLog getAuth() {
        return get("AUTH");
    }

    public static WnLog getBox() {
        return get("BOX");
    }

    public static WnLog getHook() {
        return get("HOOK");
    }

    public static WnLog getSQL() {
        return get("SQL");
    }

    public static WnLog getAC() {
        return get("AC");
    }

    public static WnLog getTest() {
        return get("TEST");
    }

    public static WnLog getBgCleanner() {
        return get("BG_CLEANER");
    }

    public static WnLog getBgTask() {
        return get("BG_TASK");
    }

    public static WnLog getBgSchedule() {
        return get("BG_SCHEDULE");
    }

    public static WnLog getBgCron() {
        return get("BG_CRON");
    }

    // ----------------------------------------------------------------------
    // 与现有 util.Wlog 同名的大写入口（RM-NUTZ-12 机械替换用）
    // ----------------------------------------------------------------------

    public static WnLog getMAIN() {
        return getMain();
    }

    public static WnLog getAPP() {
        return getApp();
    }

    public static WnLog getCMD() {
        return getCmd();
    }

    public static WnLog getAUTH() {
        return get("AUTH");
    }

    public static WnLog getBOX() {
        return get("BOX");
    }

    public static WnLog getHOOK() {
        return get("HOOK");
    }

    public static WnLog getTEST() {
        return get("TEST");
    }

    public static WnLog getBG_CLEARNER() {
        return get("BG_CLEANER");
    }

    public static WnLog getBG_TASK() {
        return get("BG_TASK");
    }

    public static WnLog getBG_SCHEDULE() {
        return get("BG_SCHEDULE");
    }

    public static WnLog getBG_CRON() {
        return get("BG_CRON");
    }

}
