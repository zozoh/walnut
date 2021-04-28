package org.nutz.walnut.ext.sys.noti;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.sys.noti.impl.WnSmsNotiHandler;
import org.nutz.walnut.ext.sys.noti.impl.WnWeixinNotiHandler;
import org.nutz.walnut.ext.sys.noti.impl.WnXXPushNotiHandler;

public final class WnNotis {

    /**
     * -1 : 错误的消息内容，不能被处理
     */
    public static final int TP_INVALID = -1;

    /**
     * 0 : 失败
     */
    public static final int TP_FAIL = 0;

    /**
     * 1 : 还未处理
     */
    public static final int TP_WAITING = 1;

    /**
     * 10 : 完成，完成后，会设置 expi ，以便系统统一删除
     */
    public static final int TP_DONE = 10;

    private static Map<String, WnNotiHandler> notis;

    static {
        notis = new HashMap<String, WnNotiHandler>();
        notis.put("weixin", new WnWeixinNotiHandler());
        notis.put("sms", new WnSmsNotiHandler());
        notis.put("xxpush", new WnXXPushNotiHandler());
    }

    public static WnNotiHandler checkHandler(String notiType) {
        // 寻找处理器
        WnNotiHandler noti = notis.get(notiType);

        // 没有合法处理器
        if (null == noti)
            throw Er.create("e.cmd.noti.unknownType", notiType);

        return noti;
    }

    // 静态帮助函数集合，不能被实例化
    private WnNotis() {}
}
