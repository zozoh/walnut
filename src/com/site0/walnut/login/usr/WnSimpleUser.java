package com.site0.walnut.login.usr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.nutz.json.Json;
import org.nutz.json.JsonIgnore;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.login.UserRace;
import com.site0.walnut.login.role.WnRoleRank;
import com.site0.walnut.login.role.WnRole;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.role.WnRoleLoader;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wobj;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.Wuu;

public class WnSimpleUser implements WnUser {

    private UserRace userRace;

    private String id;

    private String name;

    private String nickname;

    private String phone;

    private String email;

    private long lastLoginAt;

    private String mainGroup;

    private String[] roles;

    private NutBean meta;

    @JsonIgnore
    private String passwd;

    @JsonIgnore
    private String salt;

    public WnSimpleUser() {}

    public WnSimpleUser(String nameOrPhoneOrEmail) {
        this.setLoginStr(nameOrPhoneOrEmail, true);
    }

    public WnSimpleUser(NutBean bean) {
        this.updateBy(bean);
    }

    @Override
    public WnRoleRank getRank(WnRoleList roles) {
        WnRoleRank rank = new WnRoleRank();
        rank.setRoles(roles);
        rank.setUserId(id);
        rank.setUserName(name);
        return rank;
    }

    @Override
    public boolean isSame(WnUser u) {
        if (null == u) {
            return false;
        }
        return id.equals(u.getId());
    }

    @Override
    public boolean isSameId(String userId) {
        return id.equals(userId);
    }

    @Override
    public boolean isSameName(String userName) {
        if (null == userName || null == this.name) {
            return false;
        }
        return this.name.equals(userName);
    }

    @Override
    public boolean isSameMainGroup(String mainGroup) {
        if (null == mainGroup || null == this.mainGroup) {
            return false;
        }
        return this.mainGroup.equals(mainGroup);
    }

    @Override
    public boolean isSysUser() {
        return UserRace.SYS == this.getUserRace();
    }

    @Override
    public boolean isDomainUser() {
        return UserRace.DOMAIN == this.getUserRace();
    }

    @Override
    public String getHomePath() {
        String dftHome = "/home/" + this.getMainGroup();
        if (isSysUser() && "root".equals(this.name)) {
            dftHome = "/root";
        }
        return this.getMetaString("HOME", dftHome);
    }

    @Override
    public void setHomePath(String path) {
        this.setMeta("HOME", path);
    }

    @Override
    public WnUser clone() {
        WnSimpleUser re = new WnSimpleUser();
        re.userRace = this.userRace;
        re.id = this.id;
        re.name = this.name;
        re.nickname = this.nickname;
        re.phone = this.phone;
        re.email = this.email;
        re.lastLoginAt = this.lastLoginAt;
        re.mainGroup = this.mainGroup;
        re.roles = this.roles;
        re.meta = this.meta;
        re.passwd = this.passwd;
        re.salt = this.salt;
        return re;
    }

    @Override
    public void setLoginStr(String str, boolean autoSetName) {
        if (Ws.isBlank(str))
            throw Er.create("e.auth.loginstr.blank");

        // 首先整理一下字符串，去掉所有的空格
        str = str.replaceAll("[ \t\r\n]", "");

        // 确保非空
        if (Ws.isEmpty(str))
            throw Er.create("e.auth.loginstr.empty");

        // zozoh: 这个太坑，去掉吧，如果需要检查应该在入口函数等较高级的地方检查
        // 副作用会比较小
        // if (str.length() < 4)
        // throw Er.create("e.usr.loginstr.tooshort");

        // 用户 ID
        if (str.startsWith("id:")) {
            id = Ws.trim(str.substring(3));
        }
        // 手机
        else if (Strings.isMobile(str)) {
            if (autoSetName) {
                this.setPhoneAndName(str);
            } else {
                this.setPhone(str);
            }
        }
        // 邮箱
        else if (Strings.isEmail(str)) {
            if (autoSetName) {
                this.setEmailAndName(str);
            } else {
                this.setEmail(str);
            }
        }
        // 登录名
        else if (WnUsers.isValidUserName(str)) {
            name = str;
        }
        // 错误的登录字符串
        else {
            throw Er.create("e.auth.loginstr.invalid", str);
        }
    }

    @Override
    public synchronized void updateBy(NutBean bean) {
        this.meta = new NutMap();
        Set<String> ks = bean.keySet();
        String[] keys = ks.toArray(new String[ks.size()]);

        // 循环设置值
        for (String key : keys) {
            // 支持三种形式的键，以便适应数据表/Mongo Document 等场景
            String stdKey = Ws.camelCase(key);
            // 无视私有键
            if (key.startsWith("__")) {
                continue;
            }
            // id
            if ("id".equals(key)) {
                this.setId(bean.getString(key));
            }
            // name
            else if ("nm".equals(key)) {
                this.setName(bean.getString(key));
            }
            // nickname
            else if ("nickname".equals(key)) {
                this.setNickname(bean.getString(key));
            }
            // phone
            else if ("phone".equals(key)) {
                this.setPhone(bean.getString(key));
            }
            // email
            else if ("email".equals(key)) {
                this.setEmail(bean.getString(key));
            }
            // mainGroup
            else if ("grp".equals(stdKey)) {
                this.setMainGroup(bean.getString(key));
            }
            // loginAt
            else if ("lastLoginAt".equals(stdKey)) {
                Object str = bean.getString(key);
                // 空
                if (null == str) {
                    this.setLastLoginAt(0);
                }
                // 字符串或者时间戳
                else {
                    long ams = Wtime.parseAnyAMSUTC(str);
                    this.setLastLoginAt(ams);
                }
            }
            // passwd
            else if ("passwd".equals(key)) {
                this.setPasswd(bean.getString(key));
            }
            // salt
            else if ("salt".equals(key)) {
                this.setSalt(bean.getString(key));
            }
            // 忽略标准字段
            else if (Wobj.isReserveKey(key)) {
                continue;
            }
            // 元数据
            else if ("meta".equals(key)) {
                NutMap val = bean.getAs(key, NutMap.class);
                this.meta.putAll(val);
            }
            // Others put to "meta"
            else {
                Object val = bean.get(key);
                this.meta.put(key, val);
            }
        }
    }

    @Override
    public void mergeToBean(NutBean bean) {
        // ID
        if (!Ws.isBlank(id))
            bean.put("id", id);

        // Name
        if (!Ws.isBlank(name))
            bean.put("nm", name);

        // 昵称
        if (!Ws.isBlank(nickname)) {
            bean.put("nickname", nickname);
        }

        // 电话
        if (!Ws.isBlank(phone))
            bean.put("phone", phone);

        // 邮箱
        if (!Ws.isBlank(email))
            bean.put("email", email);

        if (null != this.userRace) {
            bean.put("userRace", userRace.toString());
        }

        // 主组
        if (null != this.mainGroup) {
            bean.put("grp", mainGroup);
        }

        // 角色
        if (null != this.roles) {
            bean.put("role", roles);
        }

        // 最后登录时间
        bean.put("lastLoginAt", this.lastLoginAt);
        bean.put("lastLoginAtInUTC", this.getLastLoginAtInUTC());

        // 标记一下密码
        if (!Ws.isBlank(this.salt)) {
            bean.put("salt", true);
        }

        if (!Ws.isBlank(this.passwd)) {
            bean.put("passwd", true);
        }

        // 自定义元数据
        NutMap meta = new NutMap();
        if (null != this.meta) {
            meta.putAll(this.meta);
        }
        meta.put("HOME", this.getHomePath());
        bean.put("meta", meta);
    }

    @Override
    public String toString() {
        return String.format("[%s]%s(%s), id=%s, phone=%s, email=%s, meta=%s",
                             this.userRace,
                             this.name,
                             this.nickname,
                             this.id,
                             this.phone,
                             this.email,
                             Json.toJson(this.meta));
    }

    @Override
    public NutMap toBean() {
        NutMap re = new NutMap();
        this.mergeToBean(re);
        return re;
    }

    @Override
    public NutMap toBean(WnRoleLoader rl) {
        NutMap re = this.toBean();
        if (null != rl) {
            WnRoleList roles = rl.getRoles(this);
            List<NutBean> rlist = new ArrayList<>(roles.size());
            for (WnRole role : roles) {
                NutBean rb = role.toBean();
                rb.pick("grp", "type", "role");
                rlist.add(rb);
            }
            re.put("roles", roles);
        }
        return re;
    }

    @Override
    public UserRace getUserRace() {
        return userRace;
    }

    @Override
    public void setUserRace(UserRace userRace) {
        this.userRace = userRace;
    }

    @Override
    public boolean hasId() {
        return !Ws.isBlank(this.id);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (!WnUsers.isValidUserName(name)) {
            throw Er.create("e.auth.usr.InvalidName", name);
        }
        this.name = name;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPhoneAndName(String phone) {
        this.phone = phone;
        if (Ws.isBlank(this.name)) {
            this.setName(phone);
        }
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailAndName(String email) {
        this.email = email;
        if (Ws.isBlank(this.name)) {
            int pos = email.indexOf('@');
            if (pos > 0) {
                this.setName(email.substring(0, pos).trim());
            } else {
                this.setName(email);
            }
        }
    }

    @Override
    public long getLastLoginAt() {
        return lastLoginAt;
    }

    @Override
    public String getLastLoginAtInUTC() {
        if (this.lastLoginAt <= 0) {
            return null;
        }
        Date d = new Date(this.lastLoginAt);
        return Wtime.formatUTC(d, "yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public void setLastLoginAt(long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    @Override
    public String getMainGroup() {
        return mainGroup;
    }

    @Override
    public void setMainGroup(String mainGroup) {
        this.mainGroup = mainGroup;
    }

    @Override
    public String getSalt() {
        return salt;
    }

    @Override
    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public String getPasswd() {
        return passwd;
    }

    @Override
    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    @Override
    public void genSaltAndRawPasswd(String rawPasswd) {
        String salt = Wuu.UU32();
        String passwd = Wn.genSaltPassword(rawPasswd, salt);
        setSalt(salt);
        setPasswd(passwd);
    }

    @Override
    public boolean hasSaltedPasswd() {
        return !Ws.isBlank(this.salt) && !Ws.isBlank(this.passwd);
    }

    @Override
    public void setRawPasswd(String passwd) {
        if (!Ws.isBlank(passwd)) {
            if (Ws.isBlank(this.salt)) {
                this.salt = Wuu.UU32();
            }
            this.passwd = Wn.genSaltPassword(passwd, salt);
        }
    }

    @Override
    public boolean isMatchedRawPasswd(String passwd) {
        if (null == this.passwd) {
            return false;
        }
        if (Ws.isBlank(salt)) {
            return this.passwd.equals(passwd);
        }
        String pwd = Wn.genSaltPassword(passwd, salt);
        return this.passwd.equals(pwd);
    }

    @Override
    public boolean hasMeta() {
        return null != meta && !meta.isEmpty();
    }

    @Override
    public NutBean getMeta() {
        return meta;
    }

    @Override
    public String getMetaString(String key) {
        return getMetaString(key, null);
    }

    @Override
    public String getMetaString(String key, String dft) {
        if (null != meta) {
            return meta.getString(key, dft);
        }
        return dft;
    }

    @Override
    public void setMeta(NutBean meta) {
        this.meta = meta;
    }

    @Override
    public void putMetas(NutBean meta) {
        if (null == this.meta) {
            this.meta = new NutMap();
        }
        this.meta.putAll(meta);
    }

    @Override
    public void setMeta(String key, Object val) {
        if (null == this.meta) {
            this.meta = new NutMap();
        }
        this.meta.put(key, val);
    }

    @Override
    public void removeMeta(String... keys) {
        if (null == this.meta) {
            return;
        }
        for (String key : keys) {
            this.meta.remove(key);
        }
    }

}
