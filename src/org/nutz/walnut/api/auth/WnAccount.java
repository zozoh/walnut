package org.nutz.walnut.api.auth;

import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.json.JsonIgnore;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
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
    private NutMap wxOpenIds;

    private NutMap meta;

    @JsonIgnore
    private String passwd;

    @JsonIgnore
    private String salt;

    public WnAccount() {
        this.OAuth2s = new NutMap();
        this.wxOpenIds = new NutMap();
        this.meta = new NutMap();
        this.sex = WnHumanSex.UNKNOWN;
    }

    public WnAccount(NutBean bean) {
        this();
        this.updateBy(bean);
    }

    public void updateBy(NutBean bean) {
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
            else if ("th_nm".equals(key)) {
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
                this.setWxOpenId(key, bean.getString(key));
            }
            // Others put to "meta"
            else {
                this.setMeta(key, bean.get(key));
            }
        }
    }

    public void mergeToBean(NutBean bean) {
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

        // 主组
        if (!Strings.isBlank(groupName))
            bean.put("grp", groupName);

        // 业务角色
        if (!Strings.isBlank(roleName))
            bean.put("role", roleName);

        // 昵称
        if (!Strings.isBlank(nickname))
            bean.put("th_nm", nickname);

        // 头像
        if (!Strings.isBlank(thumb))
            bean.put("thumb", thumb);

        // 最后登录
        if (loginAt > 0)
            bean.put("login", loginAt);

        // 性别
        if (null != sex)
            bean.put("sex", sex.getValue());

        // 盐与密码同在
        if (!Strings.isBlank(passwd) && !Strings.isBlank(salt)) {
            bean.put("passwd", passwd);
            bean.put("salt", salt);
        }

        // oAuth2
        for (String key : OAuth2s.keySet()) {
            bean.put("oauth_" + key, OAuth2s.get(key));
        }

        // WxOpenIds
        for (String key : wxOpenIds.keySet()) {
            bean.put("wx_gh_" + key, wxOpenIds.get(key));
        }

        // Other Meta
        for (String key : meta.keySet()) {
            if (isValidMetaName(key)) {
                bean.put(key, meta.get(key));
            }
        }
    }

    public NutMap toBean() {
        NutMap bean = new NutMap();
        this.mergeToBean(bean);
        return bean;
    }

    public boolean isSameId(String uid) {
        return null != id && null != uid && id.equals(uid);
    }

    public boolean isSameName(String uname) {
        return null != name && null != uname && name.equals(uname);
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
        else if (str.matches("^[0-9+-]{11,20}$")) {
            phone = str;
        }
        // 邮箱
        else if (str.matches("^[0-9a-zA-Z_.-]+@[0-9a-zA-Z_.-]+.[0-9a-zA-Z_.-]+$")) {
            email = str;
        }
        // 登录名
        else if (WnAuths.isValidUserName(str)) {
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
        ta.email = this.email;
        ta.nickname = this.nickname;
        ta.thumb = this.thumb;
        ta.sex = this.sex;
        ta.loginAt = this.loginAt;
        ta.groupName = this.groupName;
        ta.roleName = this.roleName;
        ta.passwd = this.passwd;
        ta.salt = this.salt;
        if (null != this.OAuth2s) {
            ta.OAuth2s = this.OAuth2s.duplicate();
        }
        if (null != wxOpenIds) {
            ta.wxOpenIds = this.wxOpenIds.duplicate();
        }
        if (null != this.meta) {
            ta.meta = this.meta.duplicate();
        }
        return ta;
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String group) {
        this.groupName = group;
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

    public NutMap getWxOpenIdMap() {
        return wxOpenIds;
    }

    public String getWxOpenId(String ghName) {
        if (null == this.wxOpenIds) {
            return null;
        }
        if (ghName.startsWith("wx_gh_")) {
            ghName = ghName.substring("wx_gh_".length());
        }
        return wxOpenIds.getString(ghName);
    }

    public void setWxOpenId(String ghName, String openId) {
        if (null == this.wxOpenIds) {
            wxOpenIds = new NutMap();
        }
        if (ghName.startsWith("wx_gh_")) {
            ghName = ghName.substring("wx_gh_".length());
        }
        wxOpenIds.put(ghName, openId);
    }

    public void setWxOpenIds(NutMap wxOpenIds) {
        this.wxOpenIds = wxOpenIds;
    }

    public NutMap getMetaMap() {
        NutMap vars = new NutMap();
        if (null != this.meta) {
            for (Map.Entry<String, Object> en : this.meta.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                if (!Strings.isBlank(key)) {
                    String k2 = key.toUpperCase();
                    vars.put(k2, val);
                }
            }
        }
        return vars;
    }

    public Object getMeta(String key) {
        return meta.get(key);
    }

    public String getMetaString(String key) {
        return meta.getString(key);
    }

    public String getHomePath() {
        String home = this.getMetaString("HOME");
        if (!Strings.isBlank(home)) {
            return home;
        }
        if (this.isRoot()) {
            return "/root";
        }
        return "/home/" + this.getName();
    }

    public static boolean isValidMetaName(String name) {
        if (Strings.isBlank(name))
            return false;
        String regex = "^(nm|ph|race|tp|mime|pid|len|sha1"
                       + "|ct|lm|login"
                       + "|c|m|g|md"
                       + "|thumb|phone|email"
                       + "|oauth_.+"
                       + "|wx_.+"
                       + "|th_nm|th_set|th_live"
                       + "|d0|d1"
                       + "|passwd|salt)$";
        Pattern p = Regex.getPattern(regex);
        if (p.matcher(name).find())
            return false;
        return true;
    }

    public void setMeta(String key, Object val) {
        if (isValidMetaName(key)) {
            this.meta.put(key, val);
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
        if (Strings.isBlank(this.salt)) {
            this.salt = R.UU32();
        }
        this.passwd = Wn.genSaltPassword(passwd, salt);
    }

    public boolean isMatchPasswd(String passwd) {
        String pwd = Wn.genSaltPassword(passwd, salt);
        return this.passwd.equals(pwd);
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

}
