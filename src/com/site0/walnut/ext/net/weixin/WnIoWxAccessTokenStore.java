package com.site0.walnut.ext.net.weixin;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.util.Wn;
import org.nutz.weixin.at.WxAccessToken;
import org.nutz.weixin.spi.WxAccessTokenStore;

/**
 * 用 Walnut 的 ZIo 存放微信公众号的 token
 * <p>
 * 一般存放在 <code>~/.weixin/token</code>对象中，对象有两个属性
 * <ul>
 * <li>wx_access_token 对应 token
 * <li>wx_access_token_expires 对应 access_token_expires
 * </ul>
 * 当然，本类的构造函数没有做这个限定，它允许你传入一个 Obj 作为主目录，<br>
 * 然后它会操作目录下的 "token" 这个文件对象
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnIoWxAccessTokenStore implements WxAccessTokenStore {

    private WnIo io;
    private WnObj home;

    public WnIoWxAccessTokenStore(WnIo io, WnObj home) {
        this.io = io;
        this.home = home;
    }

    @Override
    public WxAccessToken get() {
        WnObj oAt = io.fetch(home, "access_token");
        if (null != oAt) {
            WxAccessToken wat = new WxAccessToken();
            wat.setToken(oAt.getString("wx_at_token"));
            wat.setExpires(oAt.getInt("wx_at_expires"));
            return wat;
        }
        return null;
    }

    @Override
    public void save(String token, int expires, long lastCacheTimeMillis) {
        WnObj oAt = io.createIfNoExists(home, "access_token", WnRace.FILE);
        oAt.setv("wx_at_token", token);
        oAt.setv("wx_at_expires", Wn.now() / 1000 + expires);
        io.appendMeta(oAt, "^wx_at_.*$");
    }

}
