package org.nutz.walnut.api.auth;

import java.util.Map;

import org.nutz.json.JsonIgnore;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Wn;

public class WnAccount {

    public static WnAccount create(String name) {
        return create(name, name);
    }

    public static WnAccount create(String name, String group) {
        WnAccount u = new WnAccount();
        u.setId(R.UU32());
        u.setName(name);
        u.setGroupName(group);
        return u;
    }

    private String id;

    private String name;

    private String phone;

    private String email;

    private String nickname;

    private String thumb;

    private WnHumanSex sex;

    private long loginAt;

    /**
     * 主组，如果是域账户，主组则为当前域
     */
    private String groupName;

    /**
     * 域角色名，这个只有域用户在域用户表里才能指定的。用来判断业务权限
     */
    private String roleName;

    @JsonIgnore
    private NutMap OAuth2s;

    @JsonIgnore
    private NutMap wxGhOpenIds;

    @JsonIgnore
    private NutMap wxMpOpenIds;

    private NutMap meta;

    @JsonIgnore
    private String passwd;

    @JsonIgnore
    private String salt;

    /**
     * 是否是系统账户
     */
    private boolean sysAccount;

    public WnAccount() {
        this.OAuth2s = new NutMap();
        this.wxGhOpenIds = new NutMap();
        this.meta = new NutMap();
        this.sex = WnHumanSex.UNKNOWN;
    }

    public WnAccount(String str) {
        this();
        this.setLoginStr(str);
    }

    public WnAccount(String str, String passwd) {
        this();
        this.setLoginStr(str);
        this.setRawPasswd(passwd);
    }

    public WnAccount(NutBean bean) {
        this();
        this.updateBy(bean);
    }

    public void updateBy(NutBean bean) {
        // 根据 d0/d1 判断是否为系统用户
        String d0 = bean.getString("d0");
        String d1 = bean.getString("d1");
        this.sysAccount = "sys".equals(d0) && "usr".equals(d1);

        // 域站点用户，设置默认主组
        if (!this.sysAccount) {
            if (!bean.has("grp")) {
                this.setGroupName(d1);
            }
        }

        // 循环设置值
        for (String key : bean.keySet()) {
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
            // group
            else if ("grp".equals(key)) {
                this.setGroupName(bean.getString(key));
            }
            // role
            else if ("role".equals(key)) {
                this.setRoleName(bean.getString(key));
            }
            // nickname
            else if ("nickname".equals(key)) {
                this.setNickname(bean.getString(key));
            }
            // thumb
            else if ("thumb".equals(key)) {
                this.setThumb(bean.getString(key));
            }
            // sex
            else if ("sex".equals(key)) {
                this.setSex(bean.getInt(key));
            }
            // passwd
            else if ("passwd".equals(key)) {
                this.setPasswd(bean.getString(key));
            }
            // salt
            else if ("salt".equals(key)) {
                this.setSalt(bean.getString(key));
            }
            // loginAt
            else if ("login".equals(key)) {
                this.setLoginAt(bean.getLong(key));
            }
            // auth_xxx
            else if (key.startsWith("oauth_")) {
                this.setOAuth2(key, bean.getString(key));
            }
            // wx_gh_xxx
            else if (key.startsWith("wx_gh_")) {
                this.setWxGhOpenId(key, bean.getString(key));
            }
            // wx_mp_xxx
            else if (key.startsWith("wx_mp_")) {
                this.setWxMpOpenId(key, bean.getString(key));
            }
            // Others put to "meta"
            else {
                this.setMeta(key, bean.get(key));
            }
        }
    }

    public void mergeToBean(NutBean bean) {
        this.mergeToBean(bean, WnAuths.ABMM.ALL);
    }

    public void mergeToBean(NutBean bean, int mode) {
        // LOGIN : 登录相关
        if (WnAuths.ABMM.asLOGIN(mode)) {
            // ID
            if (!Strings.isBlank(id))
                bean.put("id", id);

            // Name
            if (!Strings.isBlank(name))
                bean.put("nm", name);

            // 电话
            if (!Strings.isBlank(phone))
                bean.put("phone", phone);

            // 邮箱
            if (!Strings.isBlank(email))
                bean.put("email", email);
        }

        // INFO : 账户基本信息
        if (WnAuths.ABMM.asINFO(mode)) {
            // 主组
            if (!Strings.isBlank(groupName))
                bean.put("grp", groupName);

            // 业务角色
            if (!Strings.isBlank(roleName))
                bean.put("role", roleName);

            // 昵称
            if (!Strings.isBlank(nickname))
                bean.put("nickname", nickname);

            // 头像
            if (!Strings.isBlank(thumb))
                bean.put("thumb", thumb);

            // 最后登录
            if (loginAt > 0)
                bean.put("login", loginAt);

            // 性别
            if (null != sex && WnHumanSex.UNKNOWN != sex)
                bean.put("sex", sex.getValue());
        }

        // PASSWD : 盐与密码同在
        if (WnAuths.ABMM.asPASSWD(mode)) {
            if (!Strings.isBlank(passwd) && !Strings.isBlank(salt)) {
                bean.put("passwd", passwd);
                bean.put("salt", salt);
            }
        }

        // OAUTH2 : 第三方登录信息
        if (WnAuths.ABMM.asPASSWD(mode) && null != OAuth2s) {
            for (String key : OAuth2s.keySet()) {
                bean.put("oauth_" + key, OAuth2s.get(key));
            }
        }
        // WXOPEN : 微信公众号登录信息
        if (WnAuths.ABMM.asWXOPEN(mode)) {
            if (null != wxGhOpenIds)
                for (String key : wxGhOpenIds.keySet()) {
                    bean.put("wx_gh_" + key, wxGhOpenIds.get(key));
                }
            if (null != wxMpOpenIds)
                for (String key : wxMpOpenIds.keySet()) {
                    bean.put("wx_mp_" + key, wxMpOpenIds.get(key));
                }
        }

        // 要处理 Meta
        if (WnAuths.ABMM.asMETA(mode)) {
            // Other Meta
            if (null != this.meta)
                bean.putAll(this.meta);
        }

        // 强制处理 HOME
        if (WnAuths.ABMM.asHOME(mode)) {
            bean.put("HOME", this.getHomePath());
        }
    }

    public NutMap toBean() {
        return this.toBean(WnAuths.ABMM.ALL);
    }

    public NutMap toBean(int mode) {
        NutMap bean = new NutMap();
        this.mergeToBean(bean, mode);
        return bean;
    }

    public NutMap toBeanOf(String... keys) {
        NutMap bean = this.toBean();
        return bean.pick(keys);
    }

    public NutMap toBeanBy(String regex) {
        NutMap bean = this.toBean();
        return bean.pickBy(regex);
    }

    public NutMap toBeanForClient() {
        return this.toBean(WnAuths.ABMM.LOGIN | WnAuths.ABMM.INFO | WnAuths.ABMM.META);
    }

    public boolean isSysAccount() {
        return sysAccount;
    }

    public void setSysAccount(boolean sysAccount) {
        this.sysAccount = sysAccount;
    }

    public boolean isSameId(String uid) {
        return null != id && null != uid && id.equals(uid);
    }

    public boolean isSameName(String uname) {
        return null != name && null != uname && name.equals(uname);
    }

    public boolean isSameGroup(String grp) {
        return null != groupName && null != grp && groupName.equals(grp);
    }

    public boolean isSame(WnAccount ta) {
        if (null != ta) {
            return id.equals(ta.getId());
        }
        return false;
    }

    public boolean isRoot() {
        return "root".equals(name);
    }

    public boolean isNameSameAsId() {
        return null != id && id.equals(name);
    }

    public void setLoginStr(String str) {
        if (Strings.isBlank(str))
            throw Er.create("e.auth.loginstr.blank");

        // 首先整理一下字符串，去掉所有的空格
        str = str.replaceAll("[ \t\r\n]", "");

        // 确保非空
        if (Strings.isEmpty(str))
            throw Er.create("e.auth.loginstr.empty");

        // zozoh: 这个太坑，去掉吧，如果需要检查应该在入口函数等较高级的地方检查
        // 副作用会比较小
        // if (str.length() < 4)
        // throw Er.create("e.usr.loginstr.tooshort");

        // 用户 ID
        if (str.startsWith("id:")) {
            id = Strings.trim(str.substring(3));
        }
        // 用户 ID
        else if (Wn.isFullObjId(str)) {
            id = str;
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

    public WnAccount clone() {
        WnAccount ta = new WnAccount();
        ta.id = this.id;
        ta.name = this.name;
        this.mergeTo(ta);
        return ta;
    }

    public void mergeTo(WnAccount ta) {
        ta.setSysAccount(this.sysAccount);
        if (!Strings.isBlank(this.email)) {
            ta.email = this.email;
        }
        if (!Strings.isBlank(this.nickname)) {
            ta.nickname = this.nickname;
        }
        if (!Strings.isBlank(this.thumb)) {
            ta.thumb = this.thumb;
        }
        if (null != this.sex) {
            ta.sex = this.sex;
        }
        if (this.loginAt > 0) {
            ta.loginAt = this.loginAt;
        }
        if (!Strings.isBlank(this.groupName)) {
            ta.groupName = this.groupName;
        }
        if (!Strings.isBlank(this.roleName)) {
            ta.roleName = this.roleName;
        }
        if (!Strings.isBlank(this.passwd) && !Strings.isBlank(this.salt)) {
            ta.passwd = this.passwd;
            ta.salt = this.salt;
        }
        ta.putAllOAuth2(this.OAuth2s);
        ta.putAllWxGhOpenId(this.wxGhOpenIds);
        ta.putAllMeta(this.meta);
    }

    public boolean hasId() {
        return !Strings.isBlank(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasName() {
        return !Strings.isBlank(name);
    }

    public String getName() {
        return name;
    }

    public String getName(String dft) {
        return Strings.sBlank(name, dft);
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasPhone() {
        return !Strings.isBlank(phone);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean hasEmail() {
        return !Strings.isBlank(email);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean hasThumb() {
        return !Strings.isBlank(thumb);
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public WnHumanSex getSex() {
        return sex;
    }

    public void setSex(WnHumanSex sex) {
        this.sex = sex;
    }

    public void setSex(int sex) {
        this.sex = WnHumanSex.parseInt(sex);
    }

    public void setSex(String sex) {
        sex = Strings.sBlank(sex, "UNKNOWN").toUpperCase();
        this.sex = WnHumanSex.valueOf(sex);
    }

    public long getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(long loginAt) {
        this.loginAt = loginAt;
    }

    public boolean hasGroupName() {
        return !Strings.isBlank(groupName);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String group) {
        this.groupName = group;
    }

    public boolean hasRoleName() {
        return !Strings.isBlank(roleName);
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String role) {
        this.roleName = role;
    }

    public String getOAuth2(String key) {
        if (null == this.OAuth2s) {
            return null;
        }
        if (key.startsWith("oauth_")) {
            key = key.substring("oauth_".length());
        }
        return this.OAuth2s.getString(key);
    }

    public NutMap getOAuth2Map() {
        return OAuth2s;
    }

    public void setOAuth2(String key, String val) {
        if (null == this.OAuth2s) {
            OAuth2s = new NutMap();
        }
        if (key.startsWith("oauth_")) {
            key = key.substring("oauth_".length());
        }
        OAuth2s.put(key, val);
    }

    public void putAllOAuth2(NutBean map) {
        if (null != map) {
            for (String key : map.keySet()) {
                String val = map.getString(key);
                this.setOAuth2(key, val);
            }
        }
    }

    public void setWxOpenId(String mode, String ghOrMpName, String openId) {
        // 小程序
        if ("mp".equals(mode)) {
            this.setWxMpOpenId(ghOrMpName, openId);
        }
        // 微信公号
        else if ("gh".equals(mode)) {
            this.setWxGhOpenId(ghOrMpName, openId);
        }
        // 不可能
        else {
            throw Lang.impossible();
        }
    }

    public NutMap getWxGhOpenIdMap() {
        return wxGhOpenIds;
    }

    public String getWxGhOpenId(String ghName) {
        if (null == this.wxGhOpenIds) {
            return null;
        }
        if (ghName.startsWith("wx_gh_")) {
            ghName = ghName.substring("wx_gh_".length());
        }
        return wxGhOpenIds.getString(ghName);
    }

    public void setWxGhOpenId(String ghName, String openId) {
        if (null == this.wxGhOpenIds) {
            wxGhOpenIds = new NutMap();
        }
        if (ghName.startsWith("wx_gh_")) {
            ghName = ghName.substring("wx_gh_".length());
        }
        wxGhOpenIds.put(ghName, openId);
    }

    public void putAllWxGhOpenId(NutBean map) {
        if (null != map) {
            for (String key : map.keySet()) {
                String val = map.getString(key);
                this.setWxGhOpenId(key, val);
            }
        }
    }

    public void setWxGhOpenIds(NutMap wxGhOpenIds) {
        this.wxGhOpenIds = wxGhOpenIds;
    }

    public NutMap getWxMpOpenIdMap() {
        return wxMpOpenIds;
    }

    public String getWxMpOpenId(String mpName) {
        if (null == this.wxMpOpenIds) {
            return null;
        }
        if (mpName.startsWith("wx_mp_")) {
            mpName = mpName.substring("wx_mp_".length());
        }
        return wxMpOpenIds.getString(mpName);
    }

    public void setWxMpOpenId(String mpName, String openId) {
        if (null == this.wxMpOpenIds) {
            wxMpOpenIds = new NutMap();
        }
        if (mpName.startsWith("wx_mp_")) {
            mpName = mpName.substring("wx_mp_".length());
        }
        wxMpOpenIds.put(mpName, openId);
    }

    public void putAllWxMpOpenId(NutBean map) {
        if (null != map) {
            for (String key : map.keySet()) {
                String val = map.getString(key);
                this.setWxMpOpenId(key, val);
            }
        }
    }

    public void setWxMpOpenIds(NutMap wxOpenIds) {
        this.wxMpOpenIds = wxOpenIds;
    }

    public NutMap getMetaMap() {
        NutMap map = new NutMap();
        if (null != this.meta) {
            map.putAll(this.meta);
        }
        return map;
    }

    public Object getMeta(String key) {
        return meta.get(key);
    }

    public String getMetaString(String key) {
        return meta.getString(key);
    }

    public String getMetaString(String key, String dft) {
        return meta.getString(key, dft);
    }

    public int getMetaInt(String key) {
        return meta.getInt(key);
    }

    public int getMetaInt(String key, int dft) {
        return meta.getInt(key, dft);
    }

    public long getMetaLong(String key) {
        return meta.getLong(key);
    }

    public long getMetaLong(String key, long dft) {
        return meta.getLong(key, dft);
    }

    public boolean hasHomePath() {
        return this.hasMeta("HOME");
    }

    public String getHomePath() {
        String home = this.getMetaString("HOME");
        if (!Strings.isBlank(home)) {
            return home;
        }
        if (this.isRoot()) {
            return "/root/";
        }
        if (this.hasGroupName()) {
            return "/home/" + this.getGroupName() + "/";
        }
        if (this.hasName()) {
            return "/home/" + this.getName() + "/";
        }
        return "/home/" + this.getId() + "/";
    }

    public void setHomePath(String path) {
        this.setMeta("HOME", path);
    }

    public boolean hasMeta(String key) {
        if (null == this.meta) {
            return false;
        }
        return this.meta.has(key);
    }

    public void setMeta(String key, Object val) {
        if (WnAuths.isValidMetaName(key)) {
            if (null == this.meta) {
                this.meta = new NutMap();
            }
            // String k2 = key.toUpperCase();
            this.meta.put(key, val);
        }
    }

    public void putAllMeta(NutBean meta) {
        if (null != meta) {
            for (Map.Entry<String, Object> en : meta.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                this.setMeta(key, val);
            }
        }
    }

    public void removeMeta(String... keys) {
        if (null != this.meta && !this.meta.isEmpty())
            for (String key : keys) {
                this.meta.remove(key);
            }
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public void setRawPasswd(String passwd) {
        if (!Strings.isBlank(passwd)) {
            if (Strings.isBlank(this.salt)) {
                this.salt = R.UU32();
            }
            this.passwd = Wn.genSaltPassword(passwd, salt);
        }
    }

    public boolean hasSaltedPasswd() {
        return !Strings.isBlank(salt) && !Strings.isBlank(passwd);
    }

    public boolean hasRawPasswd() {
        return Strings.isBlank(salt) && !Strings.isBlank(passwd);
    }

    public boolean isMatchedRawPasswd(String passwd) {
        if (null == this.passwd) {
            return false;
        }
        if (Strings.isBlank(salt)) {
            return this.passwd.equals(passwd);
        }
        String pwd = Wn.genSaltPassword(passwd, salt);
        return this.passwd.equals(pwd);
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String toString() {
        String px = sysAccount ? "SYS" : "DMN";
        return String.format("%s<%s:%s:%s>@%s{HOME=%s}",
                             px,
                             name,
                             Strings.sBlank(phone),
                             Strings.sBlank(email),
                             groupName,
                             this.getHomePath());
    }

}
