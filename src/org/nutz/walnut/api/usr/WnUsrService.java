package org.nutz.walnut.api.usr;

import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

/**
 * 这个实现类基于一个 ZIo 的实现，保存和读取 Session 的数据
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnUsrService {

    private static final Log log = Logs.get();

    private WnIo io;

    // ^[0-9a-zA-Z._-]{4,}$
    private Pattern regexName;

    // ^[0-9+-]{11,20}
    private Pattern regexPhone;

    // ^[0-9a-zA-Z_.-]+@[0-9a-zA-Z_.-]+.[0-9a-zA-Z_.-]+$
    private Pattern regexEmail;

    private WnObj oUsrs;

    public void on_create() {
        oUsrs = io.createIfNoExists(null, "/usr", WnRace.DIR);
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
        WnUsr u = new WnUsr();
        u.id(oU.id()).name(oU.name()).password(pwd);

        // 电话
        if (q.containsKey("phone")) {
            u.phone(q.getString("phone"));
            oU.setv("phone", u.phone());
            io.set(oU, "^phone$");
        }
        // 邮箱
        else if (q.containsKey("email")) {
            u.email(q.getString("email"));
            oU.setv("email", u.email());
            io.set(oU, "^email$");
        }
        // 用户名
        else if (q.containsKey("nm")) {
            u.name(q.getString("nm"));
            io.rename(oU, u.name());
        }
        // 不可能
        else {
            throw Lang.impossible();
        }

        // 创建主目录以及关键文件
        String phHome = "root".equals(u.name()) ? "/root" : "/home/" + u.name();

        WnObj oHome = io.create(null, phHome, WnRace.DIR);

        // 创建相关账号权限
        WnObj oPeople = io.create(oHome, ".people", WnRace.DIR);
        WnObj oMe = io.create(oPeople, u.id(), WnRace.FILE);
        oMe.setv("role", Wn.ROLE.ADMIN);
        io.set(oMe, "^role$");

        // 写入用户注册信息
        u.home(phHome);
        io.writeJson(oU, u, null);

        // 返回
        return u;
    }

    /**
     * 删除一个用户
     * 
     * @param nm
     *            用户名
     * @return 被删除的用户，null 表用户不存在
     */
    public void delete(WnUsr u) {
        WnObj o = _check_usr_obj("id:" + u.id());
        io.delete(o);
    }

    public void setPassword(String str, String pwd) {
        __assert(null, str, "pwd");

        WnObj o = _check_usr_obj(str);
        WnUsr u = io.readJson(o, WnUsr.class);
        u.password(pwd);
        io.writeJson(o, u, null);
    }

    public WnUsr setName(String str, String nm) {
        __assert(regexName, nm, "nm");

        WnObj o = _check_usr_obj(str);

        // 写入内容
        WnUsr u = io.readJson(o, WnUsr.class);

        // 如果节点名字发生变化
        if (!o.name().equals(nm)) {
            io.rename(o, nm);

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

    public WnUsr setPhone(String str, String phone) {
        __assert(regexPhone, phone, "phone");

        WnObj o = _check_usr_obj(str);
        WnUsr u = io.readJson(o, WnUsr.class);
        u.phone(phone);
        io.writeJson(o, u, null);
        // 更新索引
        o.setv("phone", phone);
        io.set(o, "^phone$");

        // 返回
        return u;

    }

    public WnUsr setEmail(String str, String email) {
        __assert(regexEmail, email, "email");

        WnObj o = _check_usr_obj(str);
        WnUsr u = io.readJson(o, WnUsr.class);
        u.email(email);
        io.writeJson(o, u, null);
        // 更新索引
        o.setv("email", email);
        io.set(o, "^email$");

        // 返回
        return u;

    }

    public WnUsr setHome(String str, String home) {
        // 确保 HOME 存在
        io.check(null, home);

        // 修改
        WnObj o = _check_usr_obj(str);
        WnUsr u = io.readJson(o, WnUsr.class);
        u.home(home);
        io.writeJson(o, u, null);

        // 返回
        return u;

    }

    public WnUsr fetch(String str) {
        WnObj o = _get_usr_obj(str);

        if (null == o)
            return null;

        return io.readJson(o, WnUsr.class);
    }

    public WnUsr check(String str) {
        WnObj o = _check_usr_obj(str);
        return io.readJson(o, WnUsr.class);
    }

    private void __assert(Pattern p, String str, String key) {
        if (!Strings.isBlank(str))
            if (null != p && !p.matcher(str).find())
                throw Er.create("e.usr.invalid." + key, str);
    }

}
