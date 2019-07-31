package org.nutz.walnut.ext.www.impl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.ext.thing.util.ThQuery;
import org.nutz.walnut.ext.www.bean.WnWebSession;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnLoginObj;
import org.nutz.walnut.util.WnPager;

public class WnWebAuthService {

    private WnIo io;

    private WnObj oAccountHome;

    private WnObj oRoleHome;

    private WnObj oSessionHome;

    private long sessionDuration;

    public WnWebAuthService(WnIo io,
                            WnObj oAccountHome,
                            WnObj oRoleHome,
                            WnObj oSessionHome,
                            long se_du) {
        this.io = io;
        this.oAccountHome = oAccountHome;
        this.oRoleHome = oRoleHome;
        this.oSessionHome = oSessionHome;
        this.sessionDuration = se_du;
    }

    /**
     * @return 角色库中的默认角色
     */
    public String getDefaultRoleName() {
        // 首先查询出对应的用户对象
        WnThingService things = new WnThingService(io, oRoleHome);
        ThQuery q = new ThQuery();
        q.qStr = "isdft:true";
        q.wp = new WnPager(1, 0);
        WnObj oRole = things.getOne(q);

        if (null != oRole)
            return oRole.name();

        return null;
    }

    /**
     * 根据会话票据，找回自身。执行次操作将会自动更新票据
     * 
     * @param ticket
     *            票据
     * @return 更新后的会话对象
     * @throws "e.www.ticket.noexist"
     *             : 票据找不到对应会话
     * @throws "e.www.account.noexist"
     *             : 会话对应用户不存在
     * @throws "e.www.account.invalid"
     *             : 会话对应用户非法
     */
    public WnWebSession checkMe(String ticket) {
        WnObj oSe = io.fetch(oSessionHome, ticket);
        if (null == oSe) {
            throw Er.create("e.www.ticked.noexist", ticket);
        }
        // 取得用户
        String uid = oSe.getString("uid");
        WnObj oU = io.get(uid);
        if (null == oU) {
            throw Er.create("e.www.account.noexist", oSe);
        }
        if (!this.oAccountHome.isSameId(oU.getString("th_set"))) {
            throw Er.create("e.www.account.invalid", oU);
        }
        // 返回对象
        return new WnWebSession(oSe, oU);
    }

    /**
     * 用户名（手机·邮箱）密码登录
     * 
     * @param str
     *            登录字符串（手机号|邮箱|登录名）
     * @param passwd
     *            密码
     * @param salted
     *            密码是否已经加过盐了
     * @return 登录成功后的会话
     * 
     * @throws "e.www.login.noexists"
     *             : 用户不存在
     * @throws "e.www.login.fail"
     *             : 用户名密码错误
     * @throws "e.www.login.forbid"
     *             : 没声明密码，因此禁止此种登录形式
     */
    public WnWebSession loginByPasswd(String str, String passwd, boolean salted) {
        WnLoginObj lo = new WnLoginObj(str);

        // 准备查询条件
        NutMap qstr = new NutMap();
        // 登录名
        if (lo.isByName()) {
            qstr.put("nm", lo.getValue());
        }
        // 手机
        else if (lo.isByPhone()) {
            qstr.put("phone", lo.getValue());
        }
        // 邮箱
        else if (lo.isByEmail()) {
            qstr.put("email", lo.getValue());
        }
        // 不可能
        else {
            throw Lang.impossible();
        }

        // 首先查询出对应的用户对象
        WnThingService things = new WnThingService(io, oAccountHome);
        ThQuery q = new ThQuery();
        q.qStr = Json.toJson(qstr);
        q.wp = new WnPager(1, 0);
        WnObj oU = things.getOne(q);

        // 没找到
        if (null == oU) {
            throw Er.create("e.www.login.noexists");
        }

        // 核对密码和盐
        String expect_pwd = oU.getString("passwd");
        String salt = oU.getString("salt");

        // 没有密码或盐
        if (Strings.isBlank(expect_pwd) || Strings.isBlank(salt)) {
            throw Er.create("e.www.login.forbid");
        }

        // 加盐验证
        String salted_pwd = passwd;
        if (!salted) {
            salted_pwd = Wn.genSaltPassword(passwd, salt);
        }

        // 验证密码
        if (!salted_pwd.equals(expect_pwd)) {
            throw Er.create("e.www.login.fail");
        }

        // 默认一天过期
        long expi = System.currentTimeMillis() + (this.sessionDuration * 1000L);

        // 验证通过后，创建会话
        String ticket = R.UU64();
        WnObj oSe = io.create(oSessionHome, ticket, WnRace.FILE);
        WnWebSession se = new WnWebSession(ticket);
        se.setId(oSe.id());
        se.setMe(oU);
        se.setExpi(expi);

        // 更新会话
        NutMap meta = se.toMeta();
        io.appendMeta(oSe, meta);

        // 返回
        return se;

    }
}
