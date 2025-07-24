package org.nutz.web.ajax;

import org.nutz.lang.util.NutMap;
import org.nutz.web.WebException;

import com.site0.walnut.api.err.Er;

public abstract class Ajax {

    public static AjaxReturn ok() {
        AjaxReturn re = new AjaxReturn();
        re.ok = true;
        return re;
    }

    public static AjaxReturn fail() {
        AjaxReturn re = new AjaxReturn();
        re.ok = false;
        return re;
    }

    public static AjaxReturn fail(Throwable e) {
        WebException err = Er.wrap(e);
        AjaxReturn re = new AjaxReturn();
        re.ok = false;
        re.errCode = err.getKey();
        re.data = err.getReason();
        return re;
    }

    public static AjaxReturn expired() {
        AjaxReturn re = new AjaxReturn();
        re.ok = false;
        re.msg = "ajax.expired";
        return re;
    }

    /**
     * @return 获得一个map，用来存放返回的结果。
     */
    public static NutMap one() {
        return new NutMap();
    }

}
