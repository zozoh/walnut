package org.nutz.walnut.impl.usr;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnRole;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

/**
 * 这个实现类基于一个 ZIo 的实现，保存和读取 Session 的数据
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class IoWnUsrService implements WnUsrService {

    private static final Log log = Logs.get();

    private WnIo io;

    // ^[0-9a-zA-Z._-]{4,}$
    private Pattern regexName;

    // ^[0-9+-]{11,20}
    private Pattern regexPhone;

    // ^[0-9a-zA-Z_.-]+@[0-9a-zA-Z_.-]+.[0-9a-zA-Z_.-]+$
    private Pattern regexEmail;

    private NutMap initEnvs;

    private String usrHome;

    private String grpHome;

    private WnObj oUsrs;

    private WnObj oGrps;

    public void on_create() {
        Wn.WC().me("root", "root");
        oUsrs = io.createIfNoExists(null, usrHome, WnRace.DIR);
        oGrps = io.createIfNoExists(null, grpHome, WnRace.DIR);
    }

    private WnQuery _eval_query(String str) {
        // 去掉空白
        str = Strings.trim(str);

        // 准备查询对象
        WnQuery q = new WnQuery();
        q.setv("pid", oUsrs.id());

        // 根据 ID
        if (str.startsWith("id:")) {
            q.setv("id", str.substring("id:".length()));
        }
        // 根据手机
        else if (regexPhone.matcher(str).find()) {
            q.setv("phone", str);
        }
        // 根据 Email
        else if (regexEmail.matcher(str).find()) {
            q.setv("email", str);
        }
        // 根据名称
        else if (regexName.matcher(str).find()) {
            q.setv("nm", str);
        }
        // 不可能
        else {
            throw Er.create("e.usr.invalid.str", str);
        }
        return q;
    }

    private WnObj _get_usr_obj(String str) {
        WnQuery q = _eval_query(str);
        return io.getOne(q);
    }

    @Override
    public WnUsr create(String str, String pwd) {
        // 分析
        WnQuery q = _eval_query(str);

        // 检查同名
        WnObj oU = io.getOne(q);

        if (null != oU)
            throw Er.create("e.usr.exists", str);

        // 创建对象
        oU = io.create(oUsrs, "${id}", WnRace.FILE);

        // 创建用户对象
        final WnUsr u = new IoWnUsr();
        u.update2(oU);
        u.salt(R.UU32());

        // 这里的hash算法与checkPassword/setPassword中一致
        u.password(Lang.sha1(pwd + u.salt()));

        // 添加所有的初始环境变量
        if (null != initEnvs) {
            u.putAll(initEnvs);
        }

        // 电话
        if (q.first().containsKey("phone")) {
            u.phone(q.first().getString("phone"));
            oU.setv("phone", u.phone());
        }
        // 邮箱
        else if (q.first().containsKey("email")) {
            u.email(q.first().getString("email"));
            oU.setv("email", u.email());
        }
        // 用户名
        else if (q.first().containsKey("nm")) {
            u.name(q.first().getString("nm"));
            oU = io.rename(oU, u.name());
        }
        // 不可能
        else {
            throw Lang.impossible();
        }

        // 设定用户组
        u.mainGroup(u.name());

        // 创建用户组
        WnObj oPeople = io.create(oGrps, u.mainGroup() + "/people", WnRace.DIR);
        WnObj oMe = io.create(oPeople, u.id(), WnRace.FILE);
        oMe.setv("role", Wn.ROLE.ADMIN);
        io.appendMeta(oMe, "^role$");

        // 创建组的主目录
        final String phHome = Wn.getUsrHome(u.name());
        final WnContext wc = Wn.WC();
        wc.su(u, () -> {
            wc.me(u.name(), u.mainGroup());
            wc.security(null, () -> {
                WnObj oHome = io.create(null, phHome, WnRace.DIR);
                // 保护主目录
                oHome.mode(0750);
                io.appendMeta(oHome, "^md$");
            });
            // WnSecurity se = wc.getSecurity();
            // wc.setSecurity(null); // FIXME 一定情况下会出问题, 还在查 @pw
            // WnObj oHome = io.create(null, phHome, WnRace.DIR);
            // // 保护主目录
            // oHome.mode(0750);
            // io.appendMeta(oHome, "^md$");
            // wc.setSecurity(se);
        });

        // 写入用户注册信息
        u.home(phHome);
        io.set(u, null);

        // 返回
        return u;
    }

    @Override
    public int getRoleInGroup(WnUsr u, String grp) {
        WnObj oMe = io.fetch(oGrps, grp + "/people/" + u.id());
        if (null != oMe) {
            return oMe.getInt("role", 0);
        }
        return Wn.ROLE.OTHERS;
    }

    @Override
    public void setRoleInGroup(WnUsr u, String grp, int role) {
        WnObj oMe = io.createIfNoExists(oGrps, grp + "/people/" + u.id(), WnRace.FILE);
        if (!oMe.containsKey("role") || role != oMe.getInt("role")) {
            oMe.setv("role", role);
            io.set(oMe, "^(role)$");
        }
        // 同步一下组数据
        if (!u.myGroups().contains(grp))
            __sync_my_groups(u);
    }

    @Override
    public int removeRoleFromGroup(WnUsr u, String grp) {
        // 查询
        WnObj oMe = io.fetch(oGrps, grp + "/people/" + u.id());
        if (null == oMe)
            return Wn.ROLE.OTHERS;

        // 删除索引
        int re = oMe.getInt("role", 0);
        io.delete(oMe);

        // 同步一下组数据
        __sync_my_groups(u);

        return re;
    }

    @Override
    public List<String> findMyGroups(WnUsr u) {
        WnQuery q = new WnQuery();
        q.setv("d0", oGrps.d0()).setv("d1", oGrps.d1());
        q.setv("nm", u.id());

        List<String> list = new LinkedList<String>();
        io.each(q, (int index, WnObj o, int length) -> {
            list.add(o.parent().parent().name());
        });
        return list;
    }

    @Override
    public boolean isInGroup(WnUsr u, String grp) {
        WnObj oMe = io.fetch(oGrps, grp + "/people/" + u.id());
        if (null == oMe)
            return false;
        return true;
    }

    @Override
    public void eachInGroup(String grp, WnQuery q, Each<WnRole> callback) {
        if (null != callback) {
            WnObj oHome = io.fetch(oGrps, grp + "/people");
            if (null != oHome) {
                if (null == q)
                    q = new WnQuery();
                q.setv("pid", oHome.id());
                io.each(q, (int index, WnObj o, int len) -> {
                    WnRole r = new WnRole();
                    r.grp = grp;
                    r.usr = o.name();
                    r.role = o.getInt("role", 0);
                    callback.invoke(index, r, len);
                });
            }
        }
    }

    @Override
    public List<WnUsr> queryInGroup(String grp, WnQuery q) {
        List<WnUsr> list = new LinkedList<WnUsr>();
        eachInGroup(grp, q, (int index, WnRole r, int len) -> {
            WnUsr u = fetch("id:" + r.usr);
            if (null != u)
                list.add(u);
        });
        return list;
    }

    @Override
    public void each(WnQuery q, Each<WnUsr> callback) {
        if (null != callback) {
            if (null == q)
                q = new WnQuery();
            q.setv("pid", oUsrs.id());
            io.each(q, (int index, WnObj o, int len) -> {
                WnUsr u = new IoWnUsr();
                u.update2(o);
                callback.invoke(index, u, len);
            });
        }
    }

    @Override
    public List<WnUsr> query(WnQuery q) {
        List<WnUsr> list = new LinkedList<WnUsr>();
        each(q, (int index, WnUsr u, int len) -> {
            list.add(u);
        });
        return list;
    }

    /**
     * 删除一个用户
     * 
     * @param nm
     *            用户名
     * @return 被删除的用户，null 表用户不存在
     */
    @Override
    public void delete(WnUsr u) {
        io.delete(u);
    }

    @Override
    public WnUsr setPassword(String str, String pwd) {
        __assert(null, str, "passwd");

        WnUsr u = this.check(str);
        u.salt(R.UU32()); // 先设置salt
        u.password(Lang.sha1(pwd + u.salt())); // 然后设置加盐后的密码
        io.set(u, "^(passwd|salt)$");

        return u;
    }

    @Override
    public boolean checkPassword(String nm, String pwd) {
        __assert(null, nm, "passwd");

        WnUsr u = check(nm);

        if (log.isDebugEnabled())
            log.debugf("read u: %s :: %s :: %s ", u.name(), u.password(), u.salt());

        if (u.salt() == null) { // 没有加盐? 加盐之
            u = setPassword(nm, u.password());
        }

        if (log.isDebugEnabled())
            log.debugf(" -- Lang.sha1('%s', '%s') == %s", pwd, u.salt(), u.password());

        return Lang.sha1(pwd + u.salt()).equals(u.password());
    }

    @Override
    public WnUsr setName(String str, String nm) {
        __assert(regexName, nm, "nm");

        // 写入内容
        WnUsr u = this.check(str);

        // 如果节点名字发生变化
        if (!u.name().equals(nm)) {
            io.rename(u, nm);

            // 修改主目录名称
            String phHome = "/home/" + u.id();
            WnObj oHome = io.fetch(null, phHome);
            if (null != oHome) {
                io.rename(oHome, nm);

                // 更新用户的主目录
                u.home(oHome.path());
                io.set(u, "^home$");

            }
            // 警告一下
            else {
                if (log.isWarnEnabled())
                    log.warnf("usr::rename(%s) noHome : %s", u.id(), phHome);
            }
        }

        // 返回
        return u;
    }

    @Override
    public WnUsr setPhone(String str, String phone) {
        __assert(regexPhone, phone, "phone");

        WnUsr u = this.check(str);
        u.setv("phone", phone);
        io.appendMeta(u, "^phone$");

        // 返回
        return u;

    }

    @Override
    public WnUsr set(String str, String key, String val) {
        if (key.matches("^nm|pwd|phone|email")) {
            throw Er.create("e.u.forbiden.set", key);
        }
        if ("home".equals(key)) {
            return this.setHome(str, val);
        }
        // 其他自由的属性
        WnUsr u = this.check(str);
        u.setv(key, val);
        io.set(u, "^" + key + "$");

        return u;
    }

    @Override
    public WnUsr setEmail(String str, String email) {
        __assert(regexEmail, email, "email");

        WnUsr u = this.check(str);
        u.setv("email", email);
        io.set(u, "^email$");

        // 返回
        return u;

    }

    @Override
    public WnUsr setHome(String str, String home) {
        // 确保 HOME 存在
        io.check(null, home);

        // 修改
        WnUsr u = this.check(str);
        u.home(home);
        io.set(u, "^home$");

        // 返回
        return u;

    }

    @Override
    public WnUsr fetch(String str) {
        WnObj o = _get_usr_obj(str);

        if (null == o)
            return null;

        // 这个就准备返回了
        WnUsr u = new IoWnUsr();

        // zozoh: 这里检查一下，如果还是写到了文件里，统统读出来作为元数据
        if (!o.has("passwd")) {
            NutMap map = io.readJson(o, NutMap.class);
            io.appendMeta(o, map);
        }
        // 嗯，文件内容没用了，写成空吧
        if (o.len() > 0) {
            io.writeText(o, "");
        }

        // 根据 WnObj 得到用户对象
        u.update2(o);

        // 没有缓存过组的索引，那么就缓存一下
        if (u.myGroups().size() == 0) {
            __sync_my_groups(u);
        }

        // 返回
        return u;
    }

    private void __sync_my_groups(WnUsr u) {
        List<String> groups = this.findMyGroups(u);
        u.myGroups(groups);
        io.set(u, "^my_grps$");
    }

    @Override
    public WnUsr check(String str) {
        WnUsr u = fetch(str);

        if (null == u)
            throw Er.create("e.usr.noexists", str);

        return u;
    }

    private void __assert(Pattern p, String str, String key) {
        if (!Strings.isBlank(str))
            if (null != p && !p.matcher(str).find())
                throw Er.create("e.usr.invalid." + key, str);
    }

}
