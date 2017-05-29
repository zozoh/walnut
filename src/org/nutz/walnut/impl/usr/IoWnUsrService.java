package org.nutz.walnut.impl.usr;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
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
import org.nutz.walnut.api.usr.WnUsrInfo;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.web.WebException;

/**
 * 这个实现类基于一个 ZIo 的实现，保存和读取 Session 的数据
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class IoWnUsrService implements WnUsrService {

    private static final Log log = Logs.get();

    private WnIo io;

    private NutMap initEnvs;

    // 得到用户存放的目录
    WnObj oUsrHome;
    WnObj oGrpHome;

    public void on_create() {
        Wn.WC().me("root", "root");
        oUsrHome = io.createIfNoExists(null, "/sys/usr", WnRace.DIR);
        oGrpHome = io.createIfNoExists(null, "/sys/grp", WnRace.DIR);
    }

    @Override
    public WnUsr create(WnUsrInfo info) {
        // 分析
        WnQuery q = Wn.Q.pid(oUsrHome);
        info.joinQuery(q);

        // 检查同名
        WnObj oU = io.getOne(q);

        if (null != oU)
            throw Er.create("e.usr.exists", info.toString());

        // 创建对象
        oU = io.create(oUsrHome, "${id}", WnRace.FILE);

        // 创建用户对象，并设置信息
        final WnUsr u = new IoWnUsr();
        u.update2(oU);
        try {
            info.joinObj(u);
        }
        // 信息加入不成功，删除已创建的对象
        catch (WebException e) {
            io.delete(oU);
            throw e;
        }

        // 添加所有的初始环境变量
        if (null != initEnvs) {
            u.putAll(initEnvs);
        }

        // 设定用户组
        u.mainGroup(u.name());

        // 设定用户文件的访问权限
        u.group(u.mainGroup());

        // 创建用户组
        String aph = Wn.appendPath("/sys/grp", u.mainGroup(), "/people");
        WnObj oPeople = io.createIfNoExists(null, aph, WnRace.DIR);
        WnObj oMe = io.create(oPeople, u.id(), WnRace.FILE);
        oMe.setv("role", Wn.ROLE.ADMIN);
        io.set(oMe, "^(role)$");

        // 创建组的主目录
        final String phHome = Wn.getUsrHome(u.name());
        final WnContext wc = Wn.WC();
        wc.su(u, () -> {
            wc.me(u.name(), u.mainGroup());
            wc.security(new WnEvalLink(io), () -> {
                WnObj oHome = io.createIfNoExists(null, phHome, WnRace.DIR);
                // 保护主目录
                oHome.mode(0750);
                io.set(oHome, "^md$");
            });
        });

        // 写入用户注册信息
        u.home(phHome);
        io.set(u, null);

        // 如果有密码，设置密码
        if (info.hasLoginPassword()) {
            this.setPassword(u, info.getLoginPassword());
        }

        // 返回
        return u;
    }

    @Override
    public void rename(WnUsr u, String newName) {
        if (log.isInfoEnabled())
            log.infof("rename [%s].nm to: '%s'", u, newName);

        // 没必要
        if (u.name().equals(newName)) {
            if (log.isInfoEnabled())
                log.info(" .. ignore");
            return;
        }

        // 检查用户是否重名
        WnUsr dbUsr = this.fetch(newName);
        if (null != dbUsr)
            throw Er.create("e.usr.rename.exists", newName);

        // 记录旧名称
        String oldName = u.name();

        // 开始记时
        Stopwatch sw = Stopwatch.begin();

        // 修改用户主目录到新名称
        String aph = Wn.getUsrHome(oldName);
        WnObj oHome = io.fetch(null, aph);
        if (null != oHome && !oHome.name().equals(newName)) {
            if (log.isInfoEnabled())
                log.infof(" - rename home: %s -> %s", oHome, newName);
            io.rename(oHome, newName);
        }

        // 修改用户的主组到新名称
        WnObj oGrp = io.fetch(null, "/sys/grp/" + oldName);
        if (null != oGrp && !oGrp.name().equals(newName)) {
            if (log.isInfoEnabled())
                log.infof(" - rename mainGroup: %s -> %s", oGrp, newName);
            io.rename(oGrp, newName);
        }

        // 修改自身
        if (log.isInfoEnabled())
            log.info(" - update self");
        u.mainGroup(newName);
        u.name(newName);
        u.home(oHome.path());
        io.set(u, "^(home|grp|nm)$");

        // 同步所属组的冗余数据
        if (log.isInfoEnabled())
            log.info(" - sync_my_groups");
        this.__sync_my_groups(u);

        // 最后在 Tree 上同步一下所有所属对象
        if (log.isInfoEnabled())
            log.info(" - sync_tree: c");
        this.__sync_tree("c", oldName, newName);

        if (log.isInfoEnabled())
            log.info(" - sync_tree: m");
        this.__sync_tree("m", oldName, newName);

        if (log.isInfoEnabled())
            log.info(" - sync_tree: m");
        this.__sync_tree("g", oldName, newName);

        // 最后结束
        sw.stop();
        if (log.isInfoEnabled())
            log.info(" - All done: " + sw.toString());
    }

    private void __sync_tree(String key, String oldName, String newName) {
        WnQuery q = new WnQuery();
        q.setv(key, oldName);
        io.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                o.setv(key, newName);
                io.set(o, "^(" + key + ")$");
            }
        });
    }

    @Override
    public boolean checkPassword(String nm, String passwd) {
        if (Strings.isBlank(nm) || Strings.isBlank(passwd))
            return false;

        WnUsr u = check(nm);

        return checkPassword(u, passwd);
    }

    @Override
    public boolean checkPassword(WnUsr u, String passwd) {
        if (null == u || Strings.isBlank(passwd))
            return false;

        if (log.isDebugEnabled())
            log.debugf("read u: %s :: %s :: %s ", u.name(), u.password(), u.salt());

        if (u.salt() == null) { // 没有加盐? 加盐之
            setPassword(u, Strings.sBlank(u.password(), "123456"));
        }

        if (log.isDebugEnabled())
            log.debugf(" -- Lang.sha1('%s', '%s') == %s", passwd, u.salt(), u.password());

        return Lang.sha1(passwd + u.salt()).equals(u.password());
    }

    @Override
    public void setPassword(WnUsr u, String passwd) {
        if (Strings.isBlank(passwd))
            throw Er.create("e.usr.passwd.blank");

        if (passwd.length() < 6)
            throw Er.create("e.usr.passwd.tooshort");

        // 先加盐
        u.salt(R.UU32());

        // 然后设置加盐后的密码
        u.password(Lang.sha1(passwd + u.salt()));

        // 存吧
        io.set(u, "^(passwd|salt)$");

    }

    @Override
    public void set(WnUsr u, String key, Object val) {
        // 密码
        if ("passwd".equals(key)) {
            this.setPassword(u, val.toString());
        }
        // 改名
        else if ("nm".equals(key)) {
            this.rename(u, val.toString());
        }
        // 其他
        else {
            u.setv(key, val);
            io.set(u, "^" + key + "$");
        }
    }

    @Override
    public void set(WnUsr u, NutMap meta) {
        // 不能改动密码
        meta.remove("passwd");
        meta.remove("salt");

        // 更新
        io.appendMeta(u, meta);
    }

    @Override
    public void delete(WnUsr u) {
        io.delete(u);
    }

    @Override
    public WnUsr fetch(String str) {
        WnUsrInfo info = new WnUsrInfo();
        info.valueOf(str);
        return fetchBy(info);
    }

    @Override
    public WnUsr check(String str) {
        WnUsrInfo info = new WnUsrInfo();
        info.valueOf(str);
        return checkBy(info);
    }

    @Override
    public WnUsr fetchBy(WnUsrInfo info) {
        // 得到用户的文件对象
        WnObj oU = __fetch_usr_obj(info);

        // 木有？
        if (null == oU)
            return null;

        // 包裹返回
        return __wrap_usr_obj(oU);
    }

    private WnObj __fetch_usr_obj(WnUsrInfo info) {

        // 生成查询条件
        WnQuery q = Wn.Q.pid(oUsrHome);
        info.joinQuery(q);

        // 查询一个对象
        WnObj oU = io.getOne(q);
        return oU;
    }

    private WnUsr __wrap_usr_obj(WnObj oU) {
        WnUsr u = new IoWnUsr();

        // zozoh: 这里检查一下，如果还是写到了文件里，统统读出来作为元数据
        if (!oU.has("passwd")) {
            NutMap map = io.readJson(oU, NutMap.class);
            io.appendMeta(oU, map);
        }
        // 嗯，文件内容没用了，写成空吧
        if (oU.len() > 0) {
            io.writeText(oU, "");
        }

        // 根据 WnObj 得到用户对象
        u.update2(oU);

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
    public WnUsr checkBy(WnUsrInfo info) {
        WnUsr u = fetchBy(info);
        if (null == u)
            throw Er.create("e.usr.noexists", info);
        return u;
    }

    @Override
    public int getRoleInGroup(WnUsr u, String grp) {
        String aph = Wn.appendPath("/sys/grp", grp, "/people", u.id());
        WnObj oMe = io.fetch(null, aph);
        if (null != oMe) {
            return oMe.getInt("role", 0);
        }
        return Wn.ROLE.OTHERS;
    }

    @Override
    public boolean isMemberOfGroup(WnUsr u, String... grps) {
        for (String grp : grps) {
            int role = this.getRoleInGroup(u, grp);
            if (Wn.ROLE.ADMIN == role || Wn.ROLE.MEMBER == role)
                return true;
        }
        return false;
    }

    @Override
    public boolean isAdminOfGroup(WnUsr u, String... grps) {
        for (String grp : grps) {
            int role = this.getRoleInGroup(u, grp);
            if (Wn.ROLE.ADMIN == role)
                return true;
        }
        return false;
    }

    @Override
    public void setRoleInGroup(WnUsr u, String grp, int role) {
        String aph = Wn.appendPath("/sys/grp", grp, "/people", u.id());
        WnObj oMe = io.createIfNoExists(null, aph, WnRace.FILE);
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
        String aph = Wn.appendPath("/sys/grp", grp, "/people", u.id());
        WnObj oMe = io.createIfNoExists(null, aph, WnRace.FILE);
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
        q.setv("d0", "sys").setv("d1", "grp");
        q.setv("nm", u.id());

        List<String> list = new LinkedList<String>();
        io.each(q, (int index, WnObj o, int length) -> {
            list.add(o.parent().parent().name());
        });
        return list;
    }

    @Override
    public boolean isInGroup(WnUsr u, String grp) {
        int role = this.getRoleInGroup(u, grp);
        return Wn.ROLE.OTHERS == role;
    }

    @Override
    public void eachInGroup(String grp, WnQuery q, Each<WnRole> callback) {
        if (null != callback) {
            String aph = Wn.appendPath("/sys/grp", grp, "/people");
            WnObj oHome = io.fetch(null, aph);
            if (null != oHome) {
                if (null == q)
                    q = new WnQuery();
                q.setv("pid", oHome.id());
                io.each(q, (int index, WnObj o, int len) -> {
                    WnRole r = new WnRole();
                    r.grp = grp;
                    r.usr = o.name();
                    r.role = o.getInt("role", 0);
                    r.roleName = Wn.ROLE.getRoleName(r.role);
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
            WnObj oUsrs = io.fetch(null, "/sys/usr");
            if (null == oUsrs)
                return;
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
}
