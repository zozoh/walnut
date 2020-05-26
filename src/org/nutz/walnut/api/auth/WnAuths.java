package org.nutz.walnut.api.auth;

import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.lang.util.Regex;

public abstract class WnAuths {

    /**
     * 延迟 10 秒退出登录，以便有机会切换会话
     */
    public static final long LOGOUT_DELAY = 10000L;

    /**
     * Account Bean Merge Mode
     * 
     * @author zozoh(zozohtnt@gmail.com)
     * @see WnAccount#mergeToBean(org.nutz.lang.util.NutBean, ABMM)
     */
    public static class ABMM {
        /**
         * 仅处理 LOGIN 字段 (包括ID)
         */
        public static final int LOGIN = 1;

        public static boolean asLOGIN(int mode) {
            return (mode & LOGIN) > 0;
        }

        /**
         * 仅处理 INFO 字段
         */
        public static final int INFO = 1 << 1;

        public static boolean asINFO(int mode) {
            return (mode & INFO) > 0;
        }

        /**
         * 仅处理 密码和盐字段
         */
        public static final int PASSWD = 1 << 2;

        public static boolean asPASSWD(int mode) {
            return (mode & PASSWD) > 0;
        }

        /**
         * 仅处理 OAuth2字段
         */
        public static final int OAUTH2 = 1 << 3;

        public static boolean asOAUTH2(int mode) {
            return (mode & OAUTH2) > 0;
        }

        /**
         * 仅处理 微信公众号字段
         */
        public static final int WXOPEN = 1 << 4;

        public static boolean asWXOPEN(int mode) {
            return (mode & WXOPEN) > 0;
        }

        /**
         * 仅处理 META 字段
         */
        public static final int META = 1 << 5;

        public static boolean asMETA(int mode) {
            return (mode & META) > 0;
        }

        /**
         * 强制设置 "HOME"
         */
        public static final int HOME = 1 << 6;

        public static boolean asHOME(int mode) {
            return (mode & HOME) > 0;
        }

        /**
         * 全部字段，除了 "HOME"
         */
        public static final int ALL_INFO = LOGIN | INFO | PASSWD | OAUTH2 | WXOPEN | META;
        /**
         * 全部字段，并强制设置 "HOME"
         */
        public static final int ALL = ALL_INFO | HOME;

    }

    public static boolean isValidAccountName(String nm) {
        return null != nm && nm.matches("^[0-9a-zA-Z_]{2,}$");
    }

    public static boolean isValidMetaName(String name) {
        if (Strings.isBlank(name))
            return false;
        String regex = "^(nm|ph|race|tp|mime|pid|len|sha1"
                       + "|ct|lm|login"
                       + "|c|m|g|md"
                       + "|thumb|phone|email|sex"
                       + "|oauth_.+"
                       + "|wx_.+"
                       + "|th_.+"
                       + "|_.+"
                       + "|nickname"
                       + "|d0|d1"
                       + "|passwd|salt)$";
        Pattern p = Regex.getPattern(regex);
        if (p.matcher(name).find())
            return false;
        return true;
    }

}
