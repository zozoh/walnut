package org.nutz.walnut.ext.mediax.apis;

import org.nutz.walnut.ext.mediax.bean.MxAccount;
import org.nutz.walnut.ext.mediax.bean.MxTicket;

/**
 * 所有不需要登录票据操作的 MediaXAPI 的父类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class NoTicketMediaXAPI extends AbstractMediaXAPI {

    public NoTicketMediaXAPI(MxAccount account) {
        super(account);
    }

    @Override
    protected MxTicket _gen_ticket(MxAccount ac) {
        return null;
    }

}
