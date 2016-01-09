package org.nutz.walnut.impl.usr;

import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.util.Wn;

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

    private WnObj _check_usr_obj(String str) {
        WnObj o = _get_usr_obj(str);

        if (null == o)
            throw Er.create("e.usr.noexists", str);

        return o;
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
        u.id(oU.id()).name(oU.name()).salt(R.UU32());
        u.password(Lang.sha1(pwd + u.salt())); // 这里的hash算法与checkPassword/setPassword中一致

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
        u.group(u.name());

        // 保存所有的索引信息
        io.appendMeta(oU, "^phone|email$");

        // 创建用户组
        WnObj oPeople = io.create(oGrps, u.group() + "/people", WnRace.DIR);
        WnObj oMe = io.create(oPeople, u.id(), WnRace.FILE);
        oMe.setv("role", Wn.ROLE.ADMIN);
        io.appendMeta(oMe, "^role$");

        // 创建组的主目录
        final String phHome = Wn.getUsrHome(u.name());
        Wn.WC().su(u, new Atom() {
            public void run() {
                Wn.WC().me(u.name(), u.group());
                WnSecurity se = Wn.WC().getSecurity();
                Wn.WC().setSecurity(null); // FIXME 一定情况下会出问题, 还在查 @pw
                WnObj oHome = io.create(null, phHome, WnRace.DIR);
                // 保护主目录
                oHome.mode(0750);
                io.appendMeta(oHome, "^md$");
                Wn.WC().setSecurity(se);
            }
        });

        // 写入用户注册信息
        u.home(phHome);
        io.writeJson(oU, u, null);

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
            io.appendMeta(oMe, "^role$");
        }
    }

    @Override
    public int removeRoleFromGroup(WnUsr u, String grp) {
        WnObj oMe = io.fetch(oGrps, grp + "/people/" + u.id());
        if (null == oMe)
            return Wn.ROLE.OTHERS;

        int re = oMe.getInt("role", 0);
        io.delete(oMe);
        return re;
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
        WnObj o = _check_usr_obj("id:" + u.id());
        io.delete(o);
    }

    @Override
    public WnUsr setPassword(String str, String pwd) {
        __assert(null, str, "passwd");

        WnObj o = _check_usr_obj(str);
        WnUsr u = io.readJson(o, IoWnUsr.class);
        u.salt(R.UU32()); // 先设置salt
        u.password(Lang.sha1(pwd + u.salt())); // 然后设置加盐后的密码
        io.writeJson(o, u, null);

        return u;
    }

    @Override
    public boolean checkPassword(String nm, String pwd) {
        __assert(null, nm, "passwd");

        WnObj o = _check_usr_obj(nm);
        if (o == null)
            return false;
        WnUsr u = io.readJson(o, IoWnUsr.class);

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

        WnObj o = _check_usr_obj(str);

        // 写入内容
        WnUsr u = io.readJson(o, IoWnUsr.class);

        // 如果节点名字发生变化
        if (!o.name().equals(nm)) {
            o = io.rename(o, nm);

            // 修改主目录名称
            String phHome = "/home/" + u.id();
            WnObj oHome = io.fetch(null, phHome);
            if (null != oHome) {
                io.rename(oHome, nm);
                u.home(oHome.path());
            }
            // 警告一下
            else {
                if (log.isWarnEnabled())
                    log.warnf("usr::rename(%s) noHome : %s", u.id(), phHome);
            }
        }

        u.name(nm);
        io.writeJson(o, u, null);

        // 返回
        return u;
    }

    @Override
    public WnUsr setPhone(String str, String phone) {
        __assert(regexPhone, phone, "phone");

        WnObj o = _check_usr_obj(str);
        WnUsr u = io.readJson(o, IoWnUsr.class);
        u.phone(phone);
        io.writeJson(o, u, null);
        // 更新索引
        o.setv("phone", phone);
        io.appendMeta(o, "^phone$");

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
        WnObj o = _check_usr_obj(str);
        WnUsr u = io.readJson(o, IoWnUsr.class);
        u.setOrRemove(key, val);
        io.writeJson(o, u, null);

        return u;
    }

    @Override
    public WnUsr setEmail(String str, String email) {
        __assert(regexEmail, email, "email");

        WnObj o = _check_usr_obj(str);
        WnUsr u = io.readJson(o, IoWnUsr.class);
        u.email(email);
        io.writeJson(o, u, null);
        // 更新索引
        o.setv("email", email);
        io.appendMeta(o, "^email$");

        // 返回
        return u;

    }

    @Override
    public WnUsr setHome(String str, String home) {
        // 确保 HOME 存在
        io.check(null, home);

        // 修改
        WnObj o = _check_usr_obj(str);
        WnUsr u = io.readJson(o, IoWnUsr.class);
        u.home(home);
        io.writeJson(o, u, null);

        // 返回
        return u;

    }

    @Override
    public WnUsr fetch(String str) {
        WnObj o = _get_usr_obj(str);

        if (null == o)
            return null;

        return io.readJson(o, IoWnUsr.class);
    }

    @Override
    public IoWnUsr check(String str) {
        WnObj o = _check_usr_obj(str);
        return io.readJson(o, IoWnUsr.class);
    }

    private void __assert(Pattern p, String str, String key) {
        if (!Strings.isBlank(str))
            if (null != p && !p.matcher(str).find())
                throw Er.create("e.usr.invalid." + key, str);
    }

}
