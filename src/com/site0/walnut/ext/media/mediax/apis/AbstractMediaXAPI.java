package com.site0.walnut.ext.media.mediax.apis;

import java.io.File;
import java.util.Arrays;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.media.mediax.MediaXAPI;
import com.site0.walnut.ext.media.mediax.bean.MxAccount;
import com.site0.walnut.ext.media.mediax.bean.MxTicket;
import com.site0.walnut.ext.media.mediax.util.MxURITarget;

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

    /**
     * 缓存目标
     */
    private MxURITarget target;

    public AbstractMediaXAPI(MxAccount account) {
        this.account = account;
        // 看看是否提供了 target
        String ph = this.getClass().getPackage().getName().replace('.', '/');
        File f = Files.findFile(ph + "/targets.js");
        if (null != f) {
            this.target = Json.fromJsonFile(MxURITarget.class, f);
        }
    }

    @Override
    public String dumpTarget(String actionName) {
        if (null == target) {
            throw Er.create("e.mediax.explain.targetWithoutDefine");
        }
        return target.dump(actionName, false);
    }

    @Override
    public String explain(String actionName, String[] path) {
        if (null == target) {
            throw Er.create("e.mediax.explain.targetWithoutDefine");
        }
        if (null == path || path.length == 0) {
            throw Er.create("e.mediax.explain.nopath");
        }
        // 第一个元素为键，譬如 "最新备案/京/3"
        String key = path[0];
        String[] args = Arrays.copyOfRange(path, 1, path.length);
        // 得到返回结果
        return target.getPath(actionName, key, args);
    }

    @Override
    public MxAccount getAccount() {
        return account;
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
