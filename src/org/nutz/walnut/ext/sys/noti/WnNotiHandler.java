package org.nutz.walnut.ext.sys.noti;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public interface WnNotiHandler {

    /**
     * 为新建消息生成元数据
     * 
     * @param sys
     *            系统上下文
     * @param params
     *            命令参数表
     * @return 新建消息的的特殊元数据
     */
    NutMap add(WnSystem sys, ZParams params);

    /**
     * 执行某个消息对象的发送，如果发送成功，返回 null，<br>
     * 如果返回非空字符串，则表示失败原因，调用者会重试
     * 
     * @param sys
     *            系统上下文
     * @param oN
     *            消息对象
     * @return 失败原因
     */
    String send(WnSystem sys, WnObj oN);

}
