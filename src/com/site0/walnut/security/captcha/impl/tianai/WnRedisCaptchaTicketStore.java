package com.site0.walnut.security.captcha.impl.tianai;

import com.site0.walnut.ext.sys.redis.Wedis;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.security.captcha.WnCaptchaTicketStore;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

import redis.clients.jedis.params.SetParams;

/**
 * 基于 Redis（{@link Wedis}）的临时票据存取实现。
 *
 * <p>
 * 票据在业务提交时才被消费，那次请求可能落到集群里另一个节点，
 * 所以这里必须用多节点共享的存储。
 */
public class WnRedisCaptchaTicketStore implements WnCaptchaTicketStore {

    private WedisConfig conf;

    private String prefix;

    private long ticketExpire;

    public WnRedisCaptchaTicketStore(WedisConfig conf, String prefix, long ticketExpire) {
        this.conf = conf;
        this.prefix = prefix;
        this.ticketExpire = ticketExpire;
    }

    private String _TICKET_KEY(String ticket) {
        return this.prefix + "tkt:" + ticket;
    }

    @Override
    public String createTicket(String account, long expire) {
        String ticket = Wn.genId();
        String key = _TICKET_KEY(ticket);
        long du = expire > 0 ? expire : this.ticketExpire;
        Wedis.run(conf, jed -> {
            SetParams params = new SetParams();
            params.px(du);
            jed.set(key, account, params);
        });
        return ticket;
    }

    @Override
    public boolean checkTicket(String ticket, String account) {
        if (Ws.isBlank(ticket) || Ws.isBlank(account)) {
            return false;
        }
        String key = _TICKET_KEY(ticket);
        String json = Wedis.runGet(conf, jed -> {
            return jed.get(key);
        });
        // 票据不存在或已过期
        if (Ws.isBlank(json)) {
            return false;
        }
        // 账号不匹配（重放别人的票据）
        if (!account.equals(json)) {
            return false;
        }
        // 验过即作废（一次性）
        Wedis.run(conf, jed -> {
            jed.del(key);
        });
        return true;
    }

}
