package com.site0.walnut.api.auth;

import java.util.Map;

import org.nutz.json.JsonIgnore;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.core.bean.WnObjId;
import com.site0.walnut.core.bean.WnObjMode;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class WnAccount {

    /**
     * @deprecated 角色权限之前存放再角色的一个属性里，所以才有的这个常量。 现在（2022-07-08）决定将其转移到 role
     *             的文件内容里。并且格式从 List 变 Map 因为这样可以具备更大的扩展性。从性能的角度，如果增加了 SHA1
     *             的多级缓存，也应该是没问题的
     */
    @Deprecated
    public static final String K_ROLE_ACTIONS = "roleActions";

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

    private String[] jobs;

    private String[] depts;

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
    private NutMap wxOpenOpenIds;

    @JsonIgnore
    private NutMap wxMpOpenIds;

    private String wxUnionId;

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
        this.wxOpenOpenIds = new NutMap();
        this.wxGhOpenIds = new NutMap();
        this.wxMpOpenIds = new NutMap();
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
            // job
            else if ("job".equals(key)) {
                this.setJobs(bean.getAs(key, String[].class));
            }
            // dept
            else if ("dept".equals(key)) {
                this.setDepts(bean.getAs(key, String[].class));
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
            // wx_unionid
            else if ("wx_unionid".equals(key)) {
                this.setWxUnionId(bean.getString(key));
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
            // wx_open_xxx
            else if (key.startsWith("wx_open_")) {
                this.setWxOpenOpenId(key, bean.getString(key));
            }
            // Others put to "meta"
            else {
                Object val = bean.get(key);
                // If roleInDomain or roleInOp
                if (null != val) {
                    if (key.equals(Wn.K_ROLE_IN_OP) || key.equals(Wn.K_ROLE_IN_DOMAIN)) {
                        WnGroupRole role = WnGroupRole.parseAny(val);
                        val = role;
                    }
                }
                this.setMeta(key, val);
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

        }

        // INFO : 账户基本信息
        if (WnAuths.ABMM.asINFO(mode)) {
            // 主组
            if (!Ws.isBlank(groupName))
                bean.put("grp", groupName);

            // 业务角色
            if (this.hasRoleName())
                bean.put("role", roleName);

            // 职位
            if (this.hasJobs()) {
                bean.put("jobs", jobs);
            }

            // 部门
            if (this.hasDepts()) {
                bean.put("depts", depts);
            }

            // 昵称
            if (!Ws.isBlank(nickname))
                bean.put("nickname", nickname);

            // 头像
            if (!Ws.isBlank(thumb))
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
            if (!Ws.isBlank(passwd) && !Ws.isBlank(salt)) {
                bean.put("passwd", passwd);
                bean.put("salt", salt);
            }

            // 是否设置了密码
            bean.put("saltedPasswd", this.hasSaltedPasswd());
        }

        // OAUTH2 : 第三方登录信息
        if (WnAuths.ABMM.asPASSWD(mode) && null != OAuth2s) {
            for (String key : OAuth2s.keySet()) {
                bean.put("oauth_" + key, OAuth2s.get(key));
            }
        }
        // WXOPEN : 微信公众号登录信息
        if (WnAuths.ABMM.asWXOPEN(mode)) {
            if (!Ws.isBlank(this.wxUnionId)) {
                bean.put("wx_unionid", this.wxUnionId);
            }
            if (null != wxGhOpenIds)
                for (String key : wxGhOpenIds.keySet()) {
                    bean.put("wx_gh_" + key, wxGhOpenIds.get(key));
                }
            if (null != wxMpOpenIds)
                for (String key : wxMpOpenIds.keySet()) {
                    bean.put("wx_mp_" + key, wxMpOpenIds.get(key));
                }
            if (null != wxOpenOpenIds)
                for (String key : wxOpenOpenIds.keySet()) {
                    bean.put("wx_open_" + key, wxOpenOpenIds.get(key));
                }
        }

        // 要处理 Meta
        if (WnAuths.ABMM.asMETA(mode)) {
            // Other Meta
            if (null != this.meta)
                bean.putAll(this.meta);

            // 是否设置了密码
            bean.put("saltedPasswd", this.hasSaltedPasswd());
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
        return this.toBean(WnAuths.ABMM.LOGIN
                           | WnAuths.ABMM.INFO
                           | WnAuths.ABMM.WXOPEN
                           | WnAuths.ABMM.META);
    }

    public boolean isSysAccount() {
        return sysAccount;
    }

    public void setSysAccount(boolean sysAccount) {
        this.sysAccount = sysAccount;
    }

    public boolean isSameId(String uid) {
        // 防空
        if (null == id || null == uid)
            return false;

        // 如果直接相等就不判断了
        if (id.equals(uid))
            return true;

        // 这里需要考虑两段式 ID
        WnObjId oid = new WnObjId(uid);
        String myid = oid.getMyId();
        return id.equals(myid);
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

    /**
     * @return 昵称是否是原始的，譬如和 openId 或者 union ID 相等
     */
    public boolean hasRawNickname() {
        if (Ws.isBlank(nickname)
            || nickname.equals(this.wxUnionId)
            || nickname.equals(this.id)
            || nickname.equals("anonymous"))
            return true;

        for (Map.Entry<String, Object> en : this.wxGhOpenIds.entrySet()) {
            if (nickname.equals(en.getValue())) {
                return true;
            }
        }

        for (Map.Entry<String, Object> en : this.wxMpOpenIds.entrySet()) {
            if (nickname.equals(en.getValue())) {
                return true;
            }
        }

        for (Map.Entry<String, Object> en : this.wxOpenOpenIds.entrySet()) {
            if (nickname.equals(en.getValue())) {
                return true;
            }
        }

        for (Map.Entry<String, Object> en : this.OAuth2s.entrySet()) {
            if (nickname.equals(en.getValue())) {
                return true;
            }
        }

        return false;
    }

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

    private String[] __clone_strings(String[] ss) {
        if (null == ss) {
            return null;
        }
        String[] list = new String[ss.length];
        for (int i = 0; i < ss.length; i++) {
            list[i] = ss[i];
        }
        return list;
    }

    public WnAccount clone() {
        WnAccount ta = new WnAccount();
        ta.id = this.id;
        ta.name = this.name;
        ta.jobs = this.__clone_strings(this.jobs);
        ta.depts = this.__clone_strings(this.depts);
        this.mergeTo(ta);
        return ta;
    }

    public void mergeTo(WnAccount ta) {
        ta.setSysAccount(this.sysAccount);
        if (!Ws.isBlank(this.email)) {
            ta.email = this.email;
        }
        if (!Ws.isBlank(this.nickname)) {
            ta.nickname = this.nickname;
        }
        if (!Ws.isBlank(this.thumb)) {
            ta.thumb = this.thumb;
        }
        if (null != this.sex) {
            ta.sex = this.sex;
        }
        if (this.loginAt > 0) {
            ta.loginAt = this.loginAt;
        }
        if (!Ws.isBlank(this.groupName)) {
            ta.groupName = this.groupName;
        }
        if (!Ws.isBlank(this.roleName)) {
            ta.roleName = this.roleName;
        }
        if (!Ws.isBlank(this.passwd) && !Ws.isBlank(this.salt)) {
            ta.passwd = this.passwd;
            ta.salt = this.salt;
        }
        ta.putAllOAuth2(this.OAuth2s);
        ta.putAllWxGhOpenId(this.wxGhOpenIds);
        ta.putAllWxMpOpenId(this.wxMpOpenIds);
        ta.putAllWxOpenOpenId(this.wxOpenOpenIds);
        ta.putAllMeta(this.meta);
    }

    public boolean hasId() {
        return !Ws.isBlank(id);
    }

    public String getId() {
        return id;
    }

    private WnObjId _oid;

    public WnObjId OID() {
        if (null == _oid) {
            _oid = new WnObjId(this.id);
        }
        return _oid;
    }

    public void setId(String id) {
        this.id = id;
        this._oid = null;
    }

    public boolean hasName() {
        return !Ws.isBlank(name);
    }

    public String getName() {
        return name;
    }

    public String getName(String dft) {
        return Ws.sBlank(name, dft);
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasPhone() {
        return !Ws.isBlank(phone);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isMyPhone(String phone) {
        if (null != this.phone && null != phone) {
            return this.phone.equals(phone);
        }
        return false;
    }

    public boolean hasEmail() {
        return !Ws.isBlank(email);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isMyEmail(String email) {
        if (null != this.email && null != email) {
            return this.email.equals(email);
        }
        return false;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean hasThumb() {
        return !Ws.isBlank(thumb);
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public boolean isSexUnknown() {
        return this.sex == null || this.sex == WnHumanSex.UNKNOWN;
    }

    public boolean isSexMale() {
        return WnHumanSex.MALE == this.sex;
    }

    public boolean isSexFemale() {
        return WnHumanSex.FEMALE == this.sex;
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
        sex = Ws.sBlank(sex, "UNKNOWN").toUpperCase();
        this.sex = WnHumanSex.valueOf(sex);
    }

    public boolean hasJobs() {
        return null != jobs && jobs.length > 0;
    }

    public String[] getJobs() {
        return jobs;
    }

    public void setJobs(String[] jobs) {
        this.jobs = jobs;
    }

    public String getJobAsStr() {
        if (this.hasJobs()) {
            return Ws.join(jobs, ",");
        }
        return null;
    }

    public boolean hasDepts() {
        return null != depts && depts.length > 0;
    }

    public String[] getDepts() {
        return depts;
    }

    public String getDeptAsStr() {
        if (this.hasDepts()) {
            return Ws.join(depts, ",");
        }
        return null;
    }

    public void setDepts(String[] depts) {
        this.depts = depts;
    }

    public long getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(long loginAt) {
        this.loginAt = loginAt;
    }

    public boolean hasGroupName() {
        return !Ws.isBlank(groupName);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String group) {
        this.groupName = group;
    }

    public boolean hasRoleName() {
        return !Ws.isBlank(roleName);
    }

    private String[] _role_list = null;

    public String[] getRoleList() {
        if (null == _role_list) {
            _role_list = Ws.splitIgnoreBlank(this.roleName);
        }
        return _role_list;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRoleName(String dftName) {
        return Ws.sBlank(roleName, dftName);
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

    public boolean hasWxUnionId() {
        return !Ws.isBlank(this.wxUnionId);
    }

    public String getWxUnionId() {
        return this.wxUnionId;
    }

    public void setWxUnionId(String wxUnionId) {
        this.wxUnionId = wxUnionId;
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
        // 开放平台
        else if ("open".equals(mode)) {
            this.setWxOpenOpenId(ghOrMpName, openId);
        }
        // 不可能
        else {
            throw Wlang.impossible();
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

    public NutMap getWxOpenOpenIdMap() {
        return wxOpenOpenIds;
    }

    public String getWxOpenOpenId(String mpName) {
        if (null == this.wxOpenOpenIds) {
            return null;
        }
        if (mpName.startsWith("wx_mp_")) {
            mpName = mpName.substring("wx_mp_".length());
        }
        return wxOpenOpenIds.getString(mpName);
    }

    public void setWxOpenOpenId(String mpName, String openId) {
        if (null == this.wxOpenOpenIds) {
            wxOpenOpenIds = new NutMap();
        }
        if (mpName.startsWith("wx_mp_")) {
            mpName = mpName.substring("wx_mp_".length());
        }
        wxOpenOpenIds.put(mpName, openId);
    }

    public void putAllWxOpenOpenId(NutBean map) {
        if (null != map) {
            for (String key : map.keySet()) {
                String val = map.getString(key);
                this.setWxOpenOpenId(key, val);
            }
        }
    }

    public void setWxOpenOpenIds(NutMap wxOpenIds) {
        this.wxOpenOpenIds = wxOpenIds;
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

    public <T> T getMetaAs(String key, Class<T> classOfT) {
        return meta.getAs(key, classOfT);
    }

    public boolean hasHomePath() {
        return this.hasMeta("HOME");
    }

    public String getHomePath() {
        String home = this.getMetaString("HOME");
        if (!Ws.isBlank(home)) {
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

    public boolean setDefaultMeta(String key, Object val) {
        if (WnAuths.isValidMetaName(key)) {
            if (null == this.meta) {
                this.meta = new NutMap();
            }
            // String k2 = key.toUpperCase();
            if (!this.meta.has(key)) {
                this.meta.put(key, val);
                return true;
            }
        }
        return false;
    }

    public int evalPvgMode(NutBean pvg, int dftMode) {
        // 防空
        if (null == pvg || pvg.isEmpty()) {
            return dftMode;
        }
        // 准备权限
        boolean found = false;
        int md = 0;
        String key;
        Object val;
        //
        // 处理各种自定义的权限
        //
        // - 4a98..a123 : 直接是用户的 ID
        key = this.getId();
        val = pvg.get(key);
        if (null != val) {
            found = true;
            WnObjMode wom = WnObjMode.parse(val);
            md |= wom.getValue();
            if (md >= 511) {
                return 511;
            }
        }
        // - @[demo] : 角色【域账户】
        key = "@[" + this.getName() + "]";
        val = pvg.get(key);
        if (null != val) {
            found = true;
            WnObjMode wom = WnObjMode.parse(val);
            md |= wom.getValue();
            if (md >= 511) {
                return 511;
            }
        }
        // - @others : 角色【域账户】
        val = pvg.get("@others");
        if (null != val) {
            found = true;
            WnObjMode wom = WnObjMode.parse(val);
            md |= wom.getValue();
            if (md >= 511) {
                return 511;
            }
        }
        // - @SYS_ADMIN : 角色【域账户】
        if (this.hasRoleName()) {
            for (String role : this.getRoleList()) {
                key = "@" + role;
                val = pvg.get(key);
                if (null != val) {
                    found = true;
                    WnObjMode wom = WnObjMode.parse(val);
                    md |= wom.getValue();
                    if (md >= 511) {
                        return 511;
                    }
                }
            }
        }
        // - +M0A : 用户所属职位或部门
        if (this.hasJobs()) {
            for (String job : this.getJobs()) {
                key = "+" + job;
                val = pvg.get(key);
                if (null != val) {
                    found = true;
                    WnObjMode wom = WnObjMode.parse(val);
                    md |= wom.getValue();
                    if (md >= 511) {
                        return 511;
                    }
                }
            }
        }
        if (this.hasDepts()) {
            for (String dept : this.getDepts()) {
                key = "+" + dept;
                val = pvg.get(key);
                if (null != val) {
                    found = true;
                    WnObjMode wom = WnObjMode.parse(val);
                    md |= wom.getValue();
                    if (md >= 511) {
                        return 511;
                    }
                }
            }
        }
        // 如果已经获得了属性，那么就直接返回
        if (found) {
            return md;
        }

        // 那就是没有啊
        return md > 0 ? md : dftMode;
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

    public boolean putAllDefaultMeta(NutBean meta) {
        boolean putted = false;
        if (null != meta) {
            for (Map.Entry<String, Object> en : meta.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                putted |= this.setDefaultMeta(key, val);
            }
        }
        return putted;
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
        if (!Ws.isBlank(passwd)) {
            if (Ws.isBlank(this.salt)) {
                this.salt = R.UU32();
            }
            this.passwd = Wn.genSaltPassword(passwd, salt);
        }
    }

    public boolean hasSaltedPasswd() {
        return !Ws.isBlank(salt) && !Ws.isBlank(passwd);
    }

    public boolean hasRawPasswd() {
        return Ws.isBlank(salt) && !Ws.isBlank(passwd);
    }

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

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String toString() {
        String px = sysAccount ? "SYS" : "DMN";
        return String.format("%s<%s:%s:%s>@%s[%s/%s]JOB(%s)DEPT(%s){HOME=%s}",
                             px,
                             name,
                             Ws.sBlank(phone),
                             Ws.sBlank(email),
                             groupName,
                             this.getMeta(Wn.K_ROLE_IN_DOMAIN),
                             this.getMeta(Wn.K_ROLE_IN_OP),
                             null == this.jobs ? "" : Ws.join(this.jobs, "+"),
                             null == this.depts ? "" : Ws.join(this.depts, "+"),
                             this.getHomePath());
    }

}
