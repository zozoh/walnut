package org.nutz.walnut.api.auth;

public abstract class WnAuths {

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
         * 仅处理 META 字段（包括强制设置 "HOME"）
         */
        public static final int META = 1 << 5;

        public static boolean asMETA(int mode) {
            return (mode & META) > 0;
        }

        /**
         * 全部字段，并强制设置 "HOME"
         */
        public static final int ALL = LOGIN | INFO | PASSWD | OAUTH2 | WXOPEN | META;

    }

    public static boolean isValidAccountName(String nm) {
        return null != nm && nm.matches("^[0-9a-zA-Z_]{2,}$");
    }

}
