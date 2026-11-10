package com.site0.walnut.util;

import com.site0.walnut.log.WnLog;
import com.site0.walnut.log.WnLogs;

import com.site0.walnut.login.session.WnSession;

public class Wlog {

    public static WnLog getIO() {
        return WnLogs.getIO();
    }

    public static WnLog getAUTH() {
        return WnLogs.getAUTH();
    }

    public static WnLog getBOX() {
        return WnLogs.getBOX();
    }

    public static WnLog getHOOK() {
        return WnLogs.getHOOK();
    }

    public static WnLog getCMD() {
        return WnLogs.getCMD();
    }
    
    public static WnLog getSQL() {
        return WnLogs.getSQL();
    }

    public static WnLog getAPP() {
        return WnLogs.getAPP();
    }

    public static WnLog getAC() {
        return WnLogs.getAC();
    }

    public static WnLog getBG_CLEARNER() {
        return WnLogs.getBG_CLEARNER();
    }

    public static WnLog getBG_TASK() {
        return WnLogs.getBG_TASK();
    }

    public static WnLog getBG_SCHEDULE() {
        return WnLogs.getBG_SCHEDULE();
    }

    public static WnLog getBG_CRON() {
        return WnLogs.getBG_CRON();
    }

    // public static WnLog getEXT() {
    // return WnLogs.getExt();
    // }

    public static WnLog getMAIN() {
        return WnLogs.getMAIN();
    }
    
    public static WnLog getTEST() {
        return WnLogs.getTEST();
    }

    public static String msgf(String fmt, Object... args) {
        String s = String.format(fmt, args);
        return msg(s);
    }

    public static String msg(String str) {
        WnContext wc = Wn.WC();
        WnSession se = wc.getSession();
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
