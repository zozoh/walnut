package org.nutz.walnut.ext.www;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.util.Wn;

/**
 * 实现会话相关逻辑
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WWWPageAPI {

    /**
     * IO 接口
     */
    private WnIo io;

    /**
     * 当前用户操作的主目录，通常为 `/home/xxx`
     */
    private WnObj oHome;

    /**
     * 当前页面所在的 www 目录
     */
    private WnObj oWWW;

    /**
     * 网页渲染的上下文
     */
    private NutMap context;

    /**
     * 内部缓存一下会话主目录
     */
    private WnObj __o_session_home;

    /**
     * 创建时检查一下 oWWW 有没有指定 `hm_site_id`
     */
    private String __site_id;

    public WWWPageAPI(WnIo io, WnObj oHome, WnObj oWWW, NutMap context) {
        this.io = io;
        this.oHome = oHome;
        this.oWWW = oWWW;
        this.context = context;
        this.__site_id = oWWW.getString("hm_site_id");
        if (Strings.isBlank(this.__site_id)) {
            throw Er.create("e.www.page.api_without_siteId", oWWW);
        }
    }

    /**
     * 根据上下文中的 `cookies.www` 来检查一下会话是否登录 <br>
     * 如果登录，则会在上下文中增加 `me` 这个对象表示用户（当然会去掉密码和盐） <br>
     * 然后再检查用户是否有有效的 `phone` 字段
     * 
     * <h4>微信逻辑</h4> <br>
     * 如果请求虽然未登录，发现有微信公众号的返回码，且本网站也关联了一个微信公众号`hm_wxmp`。<br>
     * 那么会试图用微信公众号的OpenId来注册一个新用户，并生成 Session，再执行后面的逻辑<br>
     * 当然，通常没有手机号，也是要被踢掉的
     * 
     * @return true: 当前会话登录，并且用户有phone字段. false: 当前会话没有登录,或者没有 phone 字段
     */
    public boolean checkMyPhone() {
        // 确保可以正确的找到用户库
        WnObj oAcsSet = checkAccountSet();

        // 确保有会话
        NutBean se = getSession();
        if (null == se)
            return false;

        // 那么让我们看看我们可爱的用户
        String uid = se.getString("uid");
        WnThingService ths = new WnThingService(io, oAcsSet);
        WnObj oMe = ths.getThing(uid, false);

        // 用户不存在？被删了？
        if (null == oMe)
            return false;

        // 它有手机号吗？
        if (!oMe.has("phone")) {
            // 记录一下上下文，以便后面渲染重定向时，能知道要绑定手机，而不是“注册用户”
            context.put("me_without_phone", true);
            return false;
        }

        // 记录一下用户，嗯，就算全过了哦
        context.put("me", oMe.pickBy("!^(id|race|tp|mime|pid|d0|d1|c|m|g|md|ph|passwd|salt|_.*)$"));
        return true;
    }

    /**
     * 如果会话存在，更新其票据字段
     * 
     * @return 返回 Session 的路径 `siteId/Ticket`
     */
    public String updateSessionTicket() {
        NutBean se = this.getSession();
        if (null != se && se.has("nm")) {
            // 得到会话路径
            String seph = Wn.appendPath(this.__site_id, se.getString("nm"));
            // 得到会话的目录
            WnObj oSeHome = this.__session_home();
            // 得到会话对象
            WnObj oSe = io.fetch(oSeHome, seph);
            if (null != oSe) {
                String ticket = R.UU64();
                io.rename(oSe, ticket);
                se = oSe.pickBy("!^(id|race|tp|mime|pid|d0|d1|c|m|g|md|ph|_.*)$");
                context.put("session", se);

                // // 更新一下上下文中的 cookies 记录，以便页面渲染的后续指令能得到正确的会话信息
                // NutBean cookies = context.getAs("cookies", NutBean.class);
                // if (null != cookies) {
                // cookies.put("www", seph);
                // }
            }
            // 返回 Session 的路径
            return Wn.appendPath(this.__site_id, se.getString("nm"));
        }
        return null;
    }

    public NutBean getSession() {
        // 上下文里已经有会话了
        NutBean se = context.getAs("session", NutBean.class);
        if (null != se && se.size() > 0)
            return se;

        // 得到会话对象
        WnObj oSe = getSessionObj();

        if (null != oSe) {
            // OK，让我会话先放到上下文里
            se = oSe.pickBy("!^(id|race|tp|mime|pid|d0|d1|c|m|g|md|ph|_.*)$");
            context.put("session", se);
            return se;
        }

        return null;
    }

    public WnObj getSessionObj() {
        String seph = getSessionPath();
        // 非法的会话路径格式，必须 `站点ID/会话票据`
        if (Strings.isBlank(seph) || seph.indexOf('/') <= 0 || seph.endsWith("/"))
            return null;

        // 如果会话路径包括 ".." 或者 "id:"，好死不死，一定是一小撮别有用心的人在搞破坏，干掉丫的！
        if (seph.indexOf("..") >= 0 || seph.indexOf("id:") >= 0)
            return null;

        // 得到会话的目录
        WnObj oSeHome = this.__session_home();

        // 得到会话并返回
        return io.fetch(oSeHome, seph);
    }

    public String getSessionPath() {
        // 上下文里已经有会话了
        NutBean se = context.getAs("session", NutBean.class);
        if (null != se && se.has("nm")) {
            return Wn.appendPath(this.__site_id, se.getString("nm"));
        }
        // 那么，让我们来获得一下 cookies 对应的会话吧
        return (String) Mapl.cell(context, "cookies.www");
    }

    private WnObj __session_home() {
        if (null == this.__o_session_home) {
            this.__o_session_home = io.createIfNoExists(oHome, ".hmaker/session", WnRace.DIR);
        }
        if (null == this.__o_session_home)
            throw Er.create("e.www.page.session_home_fail");
        return this.__o_session_home;
    }

    public WnObj deleteMySession() {
        WnObj se = this.getSessionObj();
        if (null != se)
            io.delete(se);
        return se;
    }

    public WnObj checkAccountSet() {
        // 首先，www 目录必须是有用户库的，否则抛错，因为这肯定是开发者的锅
        if (null == oWWW || !oWWW.has("hm_account_set")) {
            throw Er.create("e.www.session.check.NoAccountSet", oWWW);
        }
        String acsId = oWWW.getString("hm_account_set");
        WnObj oAcsSet = io.checkById(acsId);
        if (null == oAcsSet || !oAcsSet.isType("thing_set")) {
            throw Er.create("e.www.session.check.AccountSetNoExists", acsId);
        }
        return oAcsSet;
    }

    public NutMap getContext() {
        return context;
    }

    public String getContextJsonString(boolean compact, boolean quoteName) {
        JsonFormat jfmt = JsonFormat.nice();
        jfmt.setCompact(compact).setQuoteName(quoteName);
        return Json.toJson(context, jfmt);
    }

    public String toJson() {
        return "<WWWSession Interface>";
    }
}
