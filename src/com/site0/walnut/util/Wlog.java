package com.site0.walnut.util;

import org.nutz.log.Log;
import org.nutz.log.Logs;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.util.log.WnLogWrapper;

public class Wlog {

    public static Log getIO() {
        return new WnLogWrapper(Logs.getLog("IO"));
    }

    public static Log getAUTH() {
        return new WnLogWrapper(Logs.getLog("AUTH"));
    }

    public static Log getBOX() {
        return new WnLogWrapper(Logs.getLog("BOX"));
    }

    public static Log getHOOK() {
        return new WnLogWrapper(Logs.getLog("HOOK"));
    }

    public static Log getCMD() {
        return new WnLogWrapper(Logs.getLog("CMD"));
    }

    public static Log getAPP() {
        return new WnLogWrapper(Logs.getLog("APP"));
    }

    public static Log getAC() {
        return new WnLogWrapper(Logs.getLog("AC"));
    }

    public static Log getBG_CLEARNER() {
        return new WnLogWrapper(Logs.getLog("BG_CLEANER"));
    }

    public static Log getBG_TASK() {
        return new WnLogWrapper(Logs.getLog("BG_TASK"));
    }

    public static Log getBG_SCHEDULE() {
        return new WnLogWrapper(Logs.getLog("BG_SCHEDULE"));
    }

    public static Log getBG_CRON() {
        return new WnLogWrapper(Logs.getLog("BG_CRON"));
    }

    // public static Log getEXT() {
    // return new WnLogWrapper(Logs.getLog("EXT"));
    // }

    public static Log getMAIN() {
        return new WnLogWrapper(Logs.getLog("MAIN"));
    }

    public static String msgf(String fmt, Object... args) {
        String s = String.format(fmt, args);
        return msg(s);
    }

    public static String msg(String str) {
        WnContext wc = Wn.WC();
        WnAuthSession se = wc.getSession();
        String tkt;
        String unm;
        if (null != se) {
            tkt = se.getTicket();
            unm = se.getMyName();
        } else {
            tkt = wc.getTicket();
            unm = wc.getMyName();
        }
        return String.format("%s:%s> %s", unm, tkt, str);
    }

}
