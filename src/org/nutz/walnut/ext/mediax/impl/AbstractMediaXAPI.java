package org.nutz.walnut.ext.mediax.impl;

import org.nutz.lang.Lang;
import org.nutz.walnut.ext.mediax.MediaXAPI;
import org.nutz.walnut.ext.mediax.bean.MxAccount;
import org.nutz.walnut.ext.mediax.bean.MxTicket;

/**
 * 所有媒体接口都要继承的父类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class AbstractMediaXAPI implements MediaXAPI {

    /**
     * 子类执行操作前，通常要调用一下这个函数。这个函数保证了你有对应平台登录需要的票据
     * <p>
     * 对于 Email 等，可能这个函数是没用的
     * 
     * @return 连接信息
     */
    protected MxTicket checkTicket() {
        throw Lang.noImplement();
    }

    /**
     * 子类实现对应的媒体平台登录功能
     * 
     * @param ac
     *            账号信息
     * 
     * @return 根据账号信息，生成一个登录用的Ticket
     */
    protected abstract MxTicket _gen_ticket(MxAccount ac);

}
