package com.site0.walnut.util;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;

/**
 * 通常一个登录用户标识可以是
 * <ul>
 * <li>name : 登录名
 * <li>phone : 手机号
 * <li>email : 邮箱
 * </ul>
 * 这个类就是用来做具体分析判断的
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnLoginObj {

    /**
     * 原始登录字符串
     */
    private String value;

    /**
     * 登录的类型
     */
    private WnLoginType type;

    public WnLoginObj(String val) {
        if (null == val)
            throw Er.create("e.login.by.null");

        // 首先整理一下字符串，去掉所有的空格
        String str = val.replaceAll("[ \t\r\n]", "");

        if (str.length() == 0)
            throw Er.create("e.login.by.blank");

        // 手机
        if (str.matches("^\\+?[ \\d-]{8,13}$")) {
            this.type = WnLoginType.PHONE;
        }
        // 邮箱
        else if (str.matches("^[0-9a-zA-Z_.-]+@[0-9a-zA-Z_.-]+.[0-9a-zA-Z_.-]+$")) {
            this.type = WnLoginType.EMAIL;
        }
        // 登录名
        else if (isValidUserName(str)) {
            this.type = WnLoginType.NAME;
        }
        // 错误的登录字符串
        else {
            throw Er.create("e.login.is.invalid", str);
        }

        // 记录原始字符串
        this.value = str;
    }

    public static boolean isValidUserName(String nm) {
        return null != nm && nm.matches("^[0-9a-zA-Z_-]{2,}$");
    }

    public NutMap toMap() {
        // 准备查询条件
        NutMap map = new NutMap();
        // 登录名
        if (this.isByName()) {
            map.put("nm", this.value);
        }
        // 手机
        else if (this.isByPhone()) {
            map.put("phone", this.value);
        }
        // 邮箱
        else if (this.isByEmail()) {
            map.put("email", this.value);
        }
        // 不可能
        else {
            throw Lang.impossible();
        }
        return map;
    }

    public String getValue() {
        return value;
    }

    public WnLoginType getType() {
        return type;
    }

    public boolean isByName() {
        return WnLoginType.NAME == this.type;
    }

    public boolean isByPhone() {
        return WnLoginType.PHONE == this.type;
    }

    public boolean isByEmail() {
        return WnLoginType.EMAIL == this.type;
    }
}
