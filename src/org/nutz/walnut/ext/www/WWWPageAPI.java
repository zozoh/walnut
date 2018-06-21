package org.nutz.walnut.ext.www;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.ext.thing.util.ThQuery;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.util.Wn;

/**
 * 实现会话相关逻辑
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WWWPageAPI extends WWWAPI {

    // public static final String CK_ME_WITHOUT_PHONE = "me_without_phone";

    public static final String CK_SET_COOKIE = "%WWW::DO_SET_COOKIE";

    public static final String CK_SET_HTTP_HEADER = "%WWW::DO_SET_HEADER";

    /**
     * 当前页面所在的 www 目录
     */
    private WnObj oWWW;

    /**
     * 网页渲染的上下文
     */
    private NutMap context;

    /**
     * 创建时检查一下 oWWW 有没有指定 `hm_site_id`
     */
    private String siteId;

    private WnIoWeixinApi wxApi;

    private String wxmp;

    /* 下面的变量来自上下文，以便成员函数可以方便的访问 */
    private NutBean header;
    private NutBean params;

    public WWWPageAPI(WnIo io, WnObj oHome, long sessionDu, WnObj oWWW, NutMap context) {
        super(io, oHome, sessionDu);
        this.oWWW = oWWW;
        this.context = context;
        this.header = context.getAs("header", NutBean.class);
        this.params = context.getAs("params", NutBean.class);

        // 检查一下
        this.siteId = oWWW.getString("hm_site_id");
        if (Strings.isBlank(this.siteId)) {
            throw Er.create("e.www.page.api_without_siteId", oWWW);
        }

        // 试图生成微信接口
        wxmp = oWWW.getString("hm_wxmp");
        if (!Strings.isBlank(wxmp)) {
            WnObj oWxConf = io.fetch(oHome, ".weixin/" + wxmp + "/wxconf");
            if (null != oWxConf) {
                wxApi = new WnIoWeixinApi(io, oWxConf);
            }
        }
    }

    /**
     * @see #getMe(String)
     */
    public boolean checkMyPhone(String cookiePath) {
        // 得到当前会话的用户
        NutBean me = this.getMe(cookiePath);

        // 没有用户，返回吧
        if (null == me)
            return false;

        // 它有手机号吗？
        if (!me.has("phone")) {
            return false;
        }

        return true;
    }

    /**
     * @see #checkMyPhone(String)
     */
    public boolean checkMyPhone() {
        return this.checkMyPhone(null);
    }

    public NutBean getMe() {
        return this.getMe(null);
    }

    /**
     * 根据当前的会话，找到会话对应的用户，确保设置如下上下文:
     * <ul>
     * <li><code>session</code> : 这个是会话对象
     * <li><code>me</code> : 当前用户
     * <li><code>role</code> : 当前用户的角色（如果设置了的话）
     * </ul>
     * 
     * 如果会话中已经有 session，那么复用。否则根据上下文中的 `cookies.www` 来检查一下会话是否登录 <br>
     * 如果登录，则会在上下文中增加 `me` 这个对象表示用户（当然会去掉密码和盐） <br>
     * 然后再检查用户是否有有效的 `phone` 字段
     * 
     * <h4>微信逻辑</h4> <br>
     * 如果请求虽然未登录，发现有微信公众号的返回码，且本网站也关联了一个微信公众号`hm_wxmp`。<br>
     * 那么会试图用微信公众号的OpenId来注册一个新用户，并生成 Session，再执行后面的逻辑<br>
     * 当然，通常没有手机号，也是要被踢掉的
     * <p>
     * 如果你没有指定 cookiePath，则不会执行这个逻辑。因为创建了 Session 也没用，没法下发 Cookie 呀
     * 
     * 
     * <h4>一个专属上下文键值，指明需要下发 Cookie</h4>
     * 
     * 当本函数创建了一个 cookies，它并不负责下发，也没法下发的。<br>
     * 它只是把要下发的 cookies 放到上下文一个特殊的键里。常量<code>CK_SET_COOKIE</code>定义了这个键
     * 
     * 调用者，通常是 `WWWModule` 会负责这个 cookies 的下发。
     * 
     * @param cookiePath
     *            指定了一个路径，表示，如果当前操作创建了一个Cookie，那么这个 Cookie 的作用路径是什么
     *            <ul>
     *            <li>如果你的值是以 "/" 开头的，则相当于 <code>Path=@cookiePath; </code>
     *            <li><code>Path=${URI_BASE}/@cookiePath; </code>
     *            </ul>
     *            其中 <code>${URI_BASE}</code> 来自上下文。 如果为空，则表示不要自动创建会话
     * 
     * @return 当前会话对应的用户对象
     * 
     * @see #CK_SET_COOKIE
     */
    public NutBean getMe(String cookiePath) {
        // 已经有了
        NutBean me = context.getAs("me", NutBean.class);
        if (null != me)
            return me;

        // 替换一下
        if (null != cookiePath)
            cookiePath = Tmpl.exec(cookiePath, context);

        // 确保可以正确的找到用户库
        WnObj oAcsSet = getAccountSet();
        if (null == oAcsSet)
            return null;
        WnThingService accS = new WnThingService(io, oAcsSet);
        WnObj oMe = null;

        // 确保有会话
        WnObj oSe = this.getSessionObj();

        // 尝试用微信搞一下
        if (null == oSe) {
            if (!Strings.isBlank(cookiePath)) {
                oMe = __create_user_by_wxcode(accS);
                // 如果没有会话，说明刚才是通过微信 code 搞的，那么搞一个会话
                if (null != oMe) {
                    // 首先看看是否有会话
                    oSe = this.fetchSessionObj(siteId, oMe);

                    // 木有的话，创建一个
                    if (null == oSe)
                        oSe = this.createSessionObj(siteId, oMe);

                    // 设置一下上下文属性，以便调用者发送 cookie
                    String seph = siteId + "/" + oSe.name();
                    context.addv2(CK_SET_COOKIE, "www=deleted; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT");
                    context.addv2(CK_SET_COOKIE, "www="+seph + "; path=/; Max-Age=1800");
                }
            }
        }
        // 那么让我们看看我们可爱的用户
        else {
            String uid = oSe.getString("uid");
            oMe = accS.getThing(uid, false);

            // 如果用户被删了，那么用微信的 code 再看看
            if (null == oMe && !Strings.isBlank(cookiePath)) {
                oMe = __create_user_by_wxcode(accS);
                // 如果搞出一个用户，那么更新当前 Session 吧
                if (null != oMe) {
                    this.chownSession(oSe, oMe);
                }
            }
        }

        // 用户不存在？被删了？
        if (null == oMe || null == oSe)
            return null;

        // 保存会话到上下文
        this.setSessionObjToContext(oSe);

        // 如果用户没有角色，试图为其分配一个默认角色
        WnObj oRosSet = this.getRoleSet();
        WnObj oRole = null;
        if (null != oRosSet) {
            WnThingService roS = new WnThingService(io, oRosSet);
            // 没有角色，给搞一个
            if (!oMe.has("role")) {
                ThQuery tq = new ThQuery("isdft", true);
                oRole = roS.getOne(tq);
                if (null != oRole) {
                    oMe.put("role", oRole.name());
                    io.set(oMe, "^role$");
                }
            }
            // 如果有角色的话，试图获取
            else {
                ThQuery tq = new ThQuery("nm", oMe.getString("role"));
                oRole = roS.getOne(tq);
            }
        }

        // 记录一下用户和角色，嗯，就算全过了哦
        me = this.genUserMap(oMe);
        context.put("me", me);
        if (null != oRole) {
            context.put("role", this.genRoleMap(oRole));
        }

        // 搞定收工
        return me;
    }

    /**
     * 如果没有会话，分析一下上下文，如果请求里表明是来自微信的，那么就尝试创建用户
     * 
     * <pre>
    {
        "header": {
           ...
           "USER-AGENT": ".. MicroMessenger/6.6.6.1300(0x26060637) ..",
           ..
        },
        "params": {
           "code": "021r2QkP0Dq82c2O4BkP0x98lP0r2QkG",
           "state": ""
        },
        ..
     * </pre>
     */
    private WnObj __create_user_by_wxcode(WnThingService ths) {
        if (null == wxApi)
            return null;

        WnObj oMe = null;
        String userAgent = header.getString("USER-AGENT");
        boolean is_weixin = userAgent.indexOf("MicroMessenger/") > 0;
        String code = params.getString("code");
        if (is_weixin && !Strings.isBlank(code)) {
            // 试图获取用户 openid 信息
            String openid = wxApi.user_openid_by_code(code);
            if (!Strings.isBlank(openid)) {
                // 查一下是否存在这个用户
                String key = "wx_" + wxmp;
                ThQuery tq = new ThQuery(key, openid);
                oMe = ths.getOne(tq);

                // 嗯，已经有这个用户了
                if (null != oMe)
                    return oMe;

                // 如果用户不存在，那么试图获取他的信息，并创建一个
                NutMap re = wxApi.user_info(openid, null);
                /**
                 * 得到的返回信息格式为：
                 * 
                 * <pre>
                 {
                    subscribe: 1,
                    openid: "xxx",
                    nickname: "小白",
                    sex: 1,
                    language: "zh_CN",
                    city: "海淀",
                    province: "北京",
                    country: "中国",
                    headimgurl: "http://..",
                    subscribe_time: 1474388443,
                    remark: "",
                    groupid: 0,
                    tagid_list: [],
                    subscribe_scene: "ADD_SCENE_OTHERS",
                    qr_scene: 0,
                    qr_scene_str: ""
                 }
                 * </pre>
                 */
                // 创建用户
                String myName = re.getString("nickname", "anonymous");
                NutMap meta = re.pickBy("^(city|province|country|sex)$");
                meta.put(key, openid);
                meta.put("th_nm", myName);
                oMe = ths.createThing(meta);

                // 如果有头像的话，搞一下
                String headimgurl = re.getString("headimgurl");
                if (!Strings.isBlank(headimgurl)) {
                    WnObj oData = Things.dirTsData(io, oMe);
                    WnObj oThumb = io.createIfNoExists(oData, oMe.id() + "/thumb.jpg", WnRace.FILE);
                    // 读取 Image
                    try {
                        URL thumb_url = new URL(headimgurl);
                        BufferedImage im = Images.read(thumb_url);
                        io.writeImage(oThumb, im);
                        oMe.thumbnail("id:" + oThumb.id());
                        io.set(oMe, "^(thumb)$");
                    }
                    catch (MalformedURLException e) {
                        throw Er.wrap(e);
                    }
                }
            }
        }
        return oMe;
    }

    public boolean checkMe() {
        return this.checkMe(null);
    }

    public boolean checkMe(String cookiePath) {
        return this.getMe(cookiePath) != null;
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
            String seph = Wn.appendPath(this.siteId, se.getString("nm"));
            // 得到会话的目录
            WnObj oSeHome = this.getSessionHome();
            // 得到会话对象
            WnObj oSe = io.fetch(oSeHome, seph);
            if (null != oSe) {
                String ticket = R.UU64();
                io.rename(oSe, ticket);
                se = setSessionObjToContext(oSe);
            }
            // 返回 Session 的路径
            return Wn.appendPath(this.siteId, se.getString("nm"));
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
            return setSessionObjToContext(oSe);
        }

        return null;
    }

    public NutBean setSessionObjToContext(WnObj oSe) {
        if (null != oSe) {
            NutBean se = genSessionMap(oSe);
            context.put("session", se);
            context.put("SEPH", oSe.getString("sid") + "/" + oSe.name());
            return se;
        }
        return null;
    }

    public WnObj getSessionObj() {
        String seph = getSessionPath();

        // 检查会话路径格式
        if (!isValidSessionPath(seph))
            return null;

        // 得到会话的目录
        WnObj oSeHome = this.getSessionHome();

        // 得到会话并返回
        return io.fetch(oSeHome, seph);
    }

    public boolean isValidSessionPath(String seph) {
        // 非法的会话路径格式，必须 `站点ID/会话票据`
        if (Strings.isBlank(seph) || seph.indexOf('/') <= 0 || seph.endsWith("/"))
            return false;

        // 如果会话路径包括 ".." 或者 "id:"，好死不死，一定是一小撮别有用心的人在搞破坏，干掉丫的！
        if (seph.indexOf("..") >= 0 || seph.indexOf("id:") >= 0)
            return false;

        return true;
    }

    public String getSessionPath() {
        // 上下文里已经有会话了
        NutBean se = context.getAs("session", NutBean.class);
        if (null != se && se.has("nm")) {
            return Wn.appendPath(this.siteId, se.getString("nm"));
        }
        // 参数里指定了
        String seph = (String) Mapl.cell(context, "params.www");
        if (this.isValidSessionPath(seph)) {
            return seph;
        }
        // 那么，让我们来获得一下 cookies 对应的会话吧
        seph = (String) Mapl.cell(context, "cookies.www");
        if (this.isValidSessionPath(seph)) {
            return seph;
        }
        return null;
    }

    public WnObj deleteMySession() {
        WnObj se = this.getSessionObj();
        if (null != se)
            io.delete(se);
        return se;
    }

    public WnObj getAccountSet() {
        // 首先，www 目录必须是有用户库的，否则抛错，因为这肯定是开发者的锅
        if (null == oWWW || !oWWW.has("hm_account_set")) {
            throw Er.create("e.www.session.check.NoAccountSet", oWWW);
        }
        String acsId = oWWW.getString("hm_account_set");
        WnObj oAcsSet = io.get(acsId);
        if (null == oAcsSet || !oAcsSet.isType("thing_set")) {
            throw Er.create("e.www.session.check.AccountSetNoExists", acsId);
        }
        return oAcsSet;
    }

    public WnObj getRoleSet() {
        // 首先，www 目录必须是有用户库的，否则抛错，因为这肯定是开发者的锅
        if (null == oWWW || !oWWW.has("hm_role_set")) {
            return null;
        }
        String rosId = oWWW.getString("hm_role_set");
        WnObj oRoSet = io.get(rosId);
        if (null == oRoSet || !oRoSet.isType("thing_set")) {
            return null;
        }
        return oRoSet;
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
