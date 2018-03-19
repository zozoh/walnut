package org.nutz.walnut.ext.mediax.apis;

import java.util.Date;
import java.util.List;

import org.nutz.walnut.ext.mediax.MediaXAPI;
import org.nutz.walnut.ext.mediax.bean.MxAccount;
import org.nutz.walnut.ext.mediax.bean.MxCrawl;
import org.nutz.walnut.ext.mediax.bean.MxReCrawl;
import org.nutz.walnut.ext.mediax.bean.MxTicket;

/**
 * 所有媒体接口都要继承的父类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class AbstractMediaXAPI implements MediaXAPI {

    /**
     * 登录用的信息
     */
    private MxAccount account;

    /**
     * 缓存票据对象
     */
    private MxTicket ticket;

    public AbstractMediaXAPI(MxAccount account) {
        this.account = account;
    }

    @Override
    public MxAccount getAccount() {
        return account;
    }

    @Override
    public List<MxReCrawl> crawl(String uri, int limit) {
        return this.crawl(new MxCrawl().uri(uri).limit(limit));
    }

    @Override
    public List<MxReCrawl> crawl(String uri, Date last) {
        return this.crawl(new MxCrawl().uri(uri).lastDate(last));
    }

    @Override
    public List<MxReCrawl> crawl(String uri, Date last, int limit) {
        return this.crawl(new MxCrawl().uri(uri).lastDate(last).limit(limit));
    }

    /**
     * 子类执行操作前，通常要调用一下这个函数。这个函数保证了你有对应平台登录需要的票据
     * <p>
     * 对于 Email 等，可能这个函数是没用的
     * 
     * @return 连接信息
     */
    protected MxTicket checkTicket() {
        if (null == ticket) {
            ticket = this._gen_ticket(account);
        }
        return ticket;
    }

    /**
     * 清除票据后，再调用 checkTicket 则会真的调用 _gen_ticket
     */
    protected void clearTicket() {
        this.ticket = null;
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
