package org.nutz.walnut.impl.auth;

import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuths;
import org.nutz.walnut.api.auth.WnGroupAccount;
import org.nutz.walnut.api.auth.WnGroupRole;
import org.nutz.walnut.api.auth.WnGroupRoleService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

public class WnSysAuthSetup extends AbstractWnAuthSetup {

    private int seDftDu;

    private int seTmpDu;

    private WnGroupRoleService groupRoles;

    public WnSysAuthSetup(WnIo io, int seDftDu, int seTmpDu) {
        super(io);
        this.seDftDu = seDftDu;
        this.seTmpDu = seTmpDu;
        this.groupRoles = new WnGroupRoleServiceImpl(io);
    }

    @Override
    public WnObj getRoleDir() {
        return null;
    }

    @Override
    public String getDefaultRoleName() {
        return "admin";
    }

    @Override
    protected WnObj createOrFetchAccountDir() {
        String aph = "/sys/usr/";
        return io.createIfNoExists(null, aph, WnRace.DIR);
    }

    @Override
    public WnObj getAvatarObj(WnAccount user, boolean autoCreate) {
        throw Lang.noImplement();
    }

    @Override
    protected WnObj createOrFetchSessionDir() {
        String aph = "/var/session/";
        return io.createIfNoExists(null, aph, WnRace.DIR);
    }

    @Override
    protected WnObj createOrFetchCaptchaDir() {
        String aph = "/var/captcha/";
        return io.createIfNoExists(null, aph, WnRace.DIR);
    }

    @Override
    public int getSessionDefaultDuration() {
        return seDftDu;
    }

    @Override
    public int getSessionTransientDuration() {
        return seTmpDu;
    }

    @Override
    protected WnObj getWeixinConf(String codeType) {
        throw Lang.noImplement();
    }

    @Override
    public void afterAccountCreated(WnAuthService auth, WnAccount user) {
        // 确保设置了主组
        if (!user.hasGroupName()) {
            user.setGroupName(user.getName());
            auth.saveAccount(user, WnAuths.ABMM.INFO);
        }

        // 为用户创建组并设置管理员
        groupRoles.setGroupRole(user, user.getGroupName(), WnGroupRole.ADMIN);

        // 为用户创建主目录
        String homePath = user.getHomePath();
        WnObj oHome = io.createIfNoExists(null, homePath, WnRace.DIR);
        oHome.creator(user.getName());
        oHome.group(user.getGroupName());
        oHome.mode(Wn.Io.modeFromOctalMode("750"));
        io.set(oHome, "^(c|g|md)$");

        // 更新用户元数据，设置主目录
        if (!user.hasHomePath()) {
            user.setHomePath(homePath);
            auth.saveAccount(user, WnAuths.ABMM.META);
        }
    }

    @Override
    public boolean beforeAccountRenamed(WnAuthService auth, WnAccount user, String newName) {
        // root 用户当然不能重命名了
        if (user.isRoot()) {
            return false;
        }
        // 只有名称与 ID 相同的账户才可以被重命名
        // 因为放开这个的话，那就麻烦大了，所有的 Obj 记录的可都是账户的 nm 啊
        // 所以，界面层应该保证，如果注册了一个 Walnut 的域用户，那么首先的操作就是要求它改个名
        // 并且警告，只能改一次
        // 对于普通域用户，自然没有这个限制，所以在 WnDomainAuthSetup 则是统统放行的
        if (!user.isSameName(user.getId())) {
            return false;
        }
        if (!user.isSameId(user.getGroupName())) {
            return false;
        }
        // 重命名用户所在组
        NutMap meta = Lang.map("grp", newName);
        WnObj oDir = auth.getSysRoleDir();
        WnQuery q = Wn.Q.pid(oDir);
        q.setv("grp", user.getId());
        List<WnObj> list = io.query(q);
        for (WnObj oR : list) {
            io.appendMeta(oR, meta);
        }

        // 如果新目录是存在的, 那么就用新目录
        String newHomePath = "/home/" + newName;
        WnObj oHome = io.fetch(null, newHomePath);

        // 否则看看是否需要改名还是创建？
        if (null == oHome) {
            String homePath = user.getHomePath();
            oHome = io.fetch(null, homePath);
            // 并不存在旧的 home，那么创建一个咯
            if (null == oHome) {
                oHome = io.create(null, newHomePath, WnRace.DIR);
            }
            // 嗯，既然旧的 home 是存在的，那么...
            else {
                // 名字不同，有必要重命名一下
                if (!oHome.isSameName(newName)) {
                    // 但是目标名称是否已经存在了呢？
                    WnObj oTaHome = io.fetch(oHome.parent(), newName);
                    if (null == oTaHome) {
                        io.rename(oHome, newName);
                        // 重新设置一下用户主目录的所有者和主组
                        oHome.creator(newName);
                        oHome.group(newName);
                        io.set(oHome, "^(c|g)$");
                    }
                    // 存在了就用对方吧
                    else {
                        oHome = oTaHome;
                    }
                }
            }
        }

        // 更新一下账户的元数据
        meta.setv("HOME", oHome.getRegularPath());
        auth.saveAccount(user, meta);

        // 放行
        return true;
    }

    @Override
    public boolean beforeAccountDeleted(WnAuthService auth, WnAccount user) {
        // 删除账户所在的组
        if (user.hasGroupName()) {
            List<WnGroupAccount> list = groupRoles.getGroups(user);
            for (WnGroupAccount wga : list) {
                WnAccount u = wga.getAccount();
                String grp = wga.getGroupName();
                groupRoles.removeGroupRole(u, grp);
            }
        }
        return true;
    }

}
