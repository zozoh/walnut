package org.nutz.walnut.ext.www;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_dusr extends JvmExecutor {

    private JsonFormat __jfmt(ZParams params) {
        return this.gen_json_format(params).setLocked("^(_.*|ph|salt|passwd)$");
    }

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "^(json|c|q|n)$");

        // 创建用户
        if (params.has("create")) {
            _do_create(sys, params);
        }
        // 登录
        else if (params.has("login")) {
            _do_login(sys, params);
        }
        // 登出
        else if (params.has("logout")) {
            _do_logout(sys, params);
        }
        // 获取 session
        else if (params.has("session")) {
            WnObj oSe = get_session(sys, params);
            if (null == oSe) {
                sys.out.println("{\"noexists\":true}");
            }
            // 找到了，读取内容
            else {
                NutMap se = sys.io.readJson(oSe, NutMap.class);
                sys.out.println(Json.toJson(se, __jfmt(params)));
            }
        }
        // 获取用户
        else if (params.has("get")) {
            WnObj oU = get_usr(sys, params);
            if (null == oU) {
                sys.out.println("{\"noexists\":true}");
            }
            // 打印用户对象元数据
            else {
                sys.out.println(Json.toJson(oU, __jfmt(params)));
            }
        }
    }

    private WnObj get_usr(WnSystem sys, ZParams params) {
        String uid = params.get("get");

        // 得到当前会话的用户
        if (Strings.isBlank(uid) || "true".equals(uid)) {
            String dseid = Wn.WC().getString(WWW.AT_SEID);

            if (Strings.isBlank(dseid))
                return null;

            WnObj oHome = this.getHome(sys);
            WnObj oSe = sys.io.fetch(oHome, ".session/" + dseid);

            if (null == oSe)
                return null;

            // 延长会话的过期时间
            String grp = sys.se.group();
            NutMap conf = WWW.read_conf(sys.io, grp);
            long duInMs = __get_se_duration(conf);
            oSe.expireTime(System.currentTimeMillis() + duInMs);
            sys.io.set(oSe, "^(expi)$");

            // 得到当前用户的 ID
            uid = oSe.getString("dusr_id");
        }

        // 这个是个 JSON 字符串
        if (Strings.isQuoteBy(uid, '{', '}')) {
            WnQuery q = new WnQuery();
            q.setv("d1", sys.se.group());
            NutMap map = Json.fromJson(NutMap.class, uid);
            q.setAll(map);
            return sys.io.getOne(q);
        }

        // 直接通过 ID 获取
        return sys.io.get(uid);
    }

    private WnObj get_session(WnSystem sys, ZParams params) {
        String dseid = params.get("session");
        // 得到当前的的会话 ID
        if (Strings.isBlank(dseid) || "true".equals(dseid)) {
            dseid = Wn.WC().getString(WWW.AT_SEID);
        }
        // 获取
        WnObj oHome = this.getHome(sys);
        return sys.io.fetch(oHome, ".session/" + dseid);
    }

    private void _do_logout(WnSystem sys, ZParams params) {
        String dseid = params.get("logout");
        // 得到当前的的会话 ID
        if (Strings.isBlank(dseid) || "true".equals(dseid)) {
            dseid = Wn.WC().getString(WWW.AT_SEID);
        }

        if (Strings.isBlank(dseid)) {
            throw Er.create("e.cmd.dusr.logout.noexist");
        }

        WnObj oHome = this.getHome(sys);
        WnObj oSe = sys.io.fetch(oHome, ".session/" + dseid);

        if (null == oSe) {
            throw Er.create("e.cmd.dusr.logout.noexist", dseid);
        }

        sys.io.delete(oSe);
        sys.out.println(dseid);
    }

    private void _do_login(WnSystem sys, ZParams params) {

        // 分析输入，并取出密码
        NutMap umap = readJsonInput(sys, params, "login");
        String passwd = __check_passwd(umap);

        // 得到主目录
        WnObj oHome = this.getHome(sys);
        WnObj oHU = sys.io.createIfNoExists(oHome, ".usr", WnRace.DIR);

        // 看看用户是否存在
        WnQuery q = Wn.Q.pid(oHU);
        q.setAll(umap);
        WnObj oU = sys.io.getOne(q);
        if (null == oU) {
            throw Er.create("e.cmd.dusr.login.noexists");
        }

        // 检查一下用户密码
        String salt = oU.getString("salt", "NOSALT");
        String expapasswd = oU.getString("passwd", "NO-PASSWD");
        String saltpasswd = Lang.sha1(passwd + salt);
        if (!expapasswd.equals(saltpasswd)) {
            throw Er.create("e.cmd.dusr.login.invalid");
        }
        // 密码可不敢给别人看见
        oU.remove("salt");
        oU.remove("passwd");

        // 确保会话主目录存在
        String grp = sys.se.group();
        WnObj oHSe = sys.io.createIfNoExists(oHome, ".session", WnRace.DIR);
        WnObj oSe = null;
        NutMap se = null;

        // 复用会话
        if (params.is("reuse")) {
            oSe = sys.io.getOne(Wn.Q.pid(oHSe).setv("dusr_id", oU.id()));
        }

        // 创建会话
        if (null == oSe) {
            oSe = sys.io.create(oHSe, "${id}", WnRace.FILE);
            se = new NutMap();
            se.put("grp", grp);
            se.put("usr", oU);
            se.put("id", oSe.id());
            sys.io.writeJson(oSe, se, null);
        }
        // 读取会话
        else {
            se = sys.io.readJson(oSe, NutMap.class);
        }

        // 设置会话过期时间, 最长不能超过3天
        NutMap conf = WWW.read_conf(sys.io, grp);
        long duInMs = __get_se_duration(conf);
        oSe.expireTime(System.currentTimeMillis() + duInMs);

        // 标记一下会话的元数据，和用户关联，以后说不定有用
        oSe.setv("dusr_id", oU.id());
        oSe.setv("dusr_nm", oU.name());

        // 保存
        sys.io.set(oSe, "^(expi|dusr_id|dusr_nm)$");

        // 最后输出会话信息
        if (params.is("json")) {
            sys.out.println(Json.toJson(se, __jfmt(params)));
        }
        // 仅仅输出 ID
        else {
            sys.out.println(oSe.id());
        }

    }

    private long __get_se_duration(NutMap conf) {
        long ms = Math.min(conf.getLong("duration", 3600L) * 1000, 86400000L * 3);
        return ms;
    }

    private void _do_create(WnSystem sys, ZParams params) {
        NutMap umap = readJsonInput(sys, params, "create");
        String passwd = __check_passwd(umap);

        // 得到主目录
        WnObj oHome = this.getHome(sys);
        WnObj oHU = sys.io.createIfNoExists(oHome, ".usr", WnRace.DIR);

        // 看看用户是否存在
        WnQuery q = Wn.Q.pid(oHU);
        q.setAll(umap);
        if (null != sys.io.getOne(q)) {
            throw Er.create("e.cmd.dusr.create.exists");
        }

        // 设置密码加盐
        String salt = R.UU64();
        umap.put("salt", salt);
        umap.put("passwd", Lang.sha1(passwd + salt));

        // 创建用户对象并保存
        WnObj oU = sys.io.createIfNoExists(oHU, "${id}", WnRace.DIR);
        sys.io.appendMeta(oU, umap);

        // 输出用户对象, 密码可不敢给别人看见
        // 嗯不用，jfmt 会过滤掉的
        // oU.remove("salt");
        // oU.remove("passwd");
        sys.out.println(Json.toJson(oU, __jfmt(params)));
    }

    private String __check_passwd(NutMap umap) {
        String passwd = Castors.me().castToString(umap.remove("passwd"));

        // 检查密码格式
        if (Strings.isBlank(passwd))
            throw Er.create("e.cmd.dusr.create.blankPasswd");

        if (passwd.length() < 6)
            throw Er.create("e.cmd.dusr.create.shortPasswd");
        return passwd;
    }

    public static NutMap readJsonInput(WnSystem sys, ZParams params, String key) {
        String s = params.get(key);
        // 来自管道
        if ((Strings.isBlank(s) || "true".equals(s)) && sys.pipeId > 0) {
            s = sys.in.readAll();
        }
        // 来自文件
        else if (!Strings.isQuoteBy(s, '{', '}')) {
            WnObj oIn = Wn.checkObj(sys, s);
            s = sys.io.readText(oIn);
        }
        return Json.fromJson(NutMap.class, s);
    }

}
