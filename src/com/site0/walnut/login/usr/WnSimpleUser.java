package com.site0.walnut.login.usr;

import java.util.Date;

import org.nutz.json.Json;
import org.nutz.json.JsonIgnore;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.auth.WnAuths;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.login.UserRace;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.Wobj;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;

public class WnSimpleUser implements WnUser {

    private UserRace userRace;

    private String id;

    private String name;

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
        this.setLoginStr(nameOrPhoneOrEmail);
    }

    public WnSimpleUser(NutBean bean) {
        this.updateBy(bean);
    }

    @Override
    public boolean isSame(WnUser u) {
        if (null == u) {
            return false;
        }
        return id.equals(u.getId());
    }

    @Override
    public WnSimpleUser clone() {
        WnSimpleUser re = new WnSimpleUser();
        re.userRace = this.userRace;
        re.id = this.id;
        re.name = this.name;
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
    public void setLoginStr(String str) {
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
            phone = str;
        }
        // 邮箱
        else if (Strings.isEmail(str)) {
            email = str;
        }
        // 登录名
        else if (WnAuths.isValidAccountName(str)) {
            name = str;
        }
        // 错误的登录字符串
        else {
            throw Er.create("e.auth.loginstr.invalid", str);
        }
    }

    @Override
    public void updateBy(NutBean bean) {
        this.meta = new NutMap();

        // 循环设置值
        for (String key : bean.keySet()) {
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
            // phone
            else if ("phone".equals(key)) {
                this.setPhone(bean.getString(key));
            }
            // email
            else if ("email".equals(key)) {
                this.setEmail(bean.getString(key));
            }
            // mainGroup
            else if ("mainGroup".equals(stdKey)) {
                this.setMainGroup(bean.getString(key));
            }
            // roles
            else if ("roles".equals(key)) {
                this.setRoles(bean.getArray(key, String.class));
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
            // Others put to "meta"
            else {
                Object val = bean.get(key);
                this.meta.put(key, val);
            }
        }
    }

    @Override
    public void mergeToBean(NutBean bean) {
        // 合并其他元数据
        if (null != this.meta) {
            bean.putAll(this.meta);
        }

        // ID
        if (!Ws.isBlank(id))
            bean.put("id", id);

        // Name
        if (!Ws.isBlank(name))
            bean.put("nm", name);

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
            bean.put("mainGroup", mainGroup);
        }

        // 角色
        if (null != this.roles) {
            bean.put("roles", roles);
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

    }

    @Override
    public String toString() {
        return String.format("[%s]%s, id=%s %s",
                             this.userRace,
                             this.name,
                             this.phone,
                             this.email,
                             this.id,
                             Json.toJson(this.meta));
    }

    @Override
    public NutMap toBean() {
        NutMap re = new NutMap();
        this.mergeToBean(re);
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
        this.name = name;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
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
    public String[] getRoles() {
        return roles;
    }

    @Override
    public void setRoles(String[] roleNames) {
        this.roles = roleNames;
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
    public String getPasswd() {
        return passwd;
    }

    @Override
    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    @Override
    public String getSalt() {
        return salt;
    }

    @Override
    public void setSalt(String salt) {
        this.salt = salt;
    }

}
