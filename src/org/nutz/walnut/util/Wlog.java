package org.nutz.walnut.util;

import org.nutz.log.Log;
import org.nutz.log.Logs;

public class Wlog {

    public static Log getIO() {
        return Logs.getLog("IO");
    }
    
    public static Log getAUTH() {
        return Logs.getLog("AUTH");
    }
    
    public static Log getBOX() {
        return Logs.getLog("BOX");
    }
    
    public static Log getHOOK() {
        return Logs.getLog("HOOK");
    }

    public static Log getCMD() {
        return Logs.getLog("CMD");
    }
    
    public static Log getAPP() {
        return Logs.getLog("APP");
    }

    public static Log getAC() {
        return Logs.getLog("AC");
    }

    public static Log getBG_CLEARNER() {
        return Logs.getLog("BG_CLEANER");
    }
    
    public static Log getBG_TASK() {
        return Logs.getLog("BG_TASK");
    }
    
    public static Log getBG_SCHEDULE() {
        return Logs.getLog("BG_SCHEDULE");
    }
    
    public static Log getBG_CRON() {
        return Logs.getLog("BG_CRON");
    }

    public static Log getEXT() {
        return Logs.getLog("EXT");
    }

    public static Log getMAIN() {
        return Logs.getLog("MAIN");
    }

}
