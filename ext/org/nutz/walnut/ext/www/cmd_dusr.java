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

    private static JsonFormat _u_json_fmt = JsonFormat.nice();

    {
        _u_json_fmt.setLocked("^(_.*|race|nm|pid|ph|d[01]|md|c|m|g)$");
    }

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "^(json)$");

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
            get_session(sys, params.check("session"));
        }
        // 根据ID获取用户
        else if (params.has("uid")) {
            get_usr_by_id(sys, params.check("uid"));
        }
        // 根据条件获取用户
        else if (params.has("u")) {
            get_usr(sys, params);
        }
    }

    private void get_usr(WnSystem sys, ZParams params) {
        NutMap umap = readJsonInput(sys, params, "u");

        WnObj oHome = this.getHome(sys);
        WnObj oHU = sys.io.createIfNoExists(oHome, ".usr", WnRace.DIR);
        WnQuery q = Wn.Q.pid(oHU);
        q.setAll(umap);
        WnObj oU = sys.io.getOne(q);

        __output_usr(sys, oU);
    }

    private void get_usr_by_id(WnSystem sys, String uid) {
        WnObj oHome = this.getHome(sys);
        WnObj oU = sys.io.fetch(oHome, ".usr/" + uid);
        __output_usr(sys, oU);
    }

    private void get_session(WnSystem sys, String dseid) {
        WnObj oHome = this.getHome(sys);
        WnObj oSe = sys.io.fetch(oHome, ".session/" + dseid);

        if (null == oSe) {
            sys.out.println("{\"exists\":false}");
        }
        // 找到了，读取内容
        else {
            NutMap se = sys.io.readJson(oSe, NutMap.class);
            sys.out.println(Json.toJson(se, _u_json_fmt));
        }
    }

    private void __output_usr(WnSystem sys, WnObj oU) {
        if (null == oU) {
            sys.out.println("{\"exists\":false}");
        }
        // 打印用户对象元数据
        else {
            sys.out.println(Json.toJson(oU, _u_json_fmt));
        }
    }

    private void _do_logout(WnSystem sys, ZParams params) {
        String dseid = params.check("logout");

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

        // 创建会话
        WnObj oHSe = sys.io.createIfNoExists(oHome, ".session", WnRace.DIR);
        WnObj oSe = sys.io.create(oHSe, "${id}", WnRace.FILE);
        NutMap se = new NutMap();
        String grp = sys.se.group();
        se.put("grp", grp);
        se.put("usr", oU);
        se.put("id", oSe.id());
        sys.io.writeJson(oSe, se, null);

        // 设置会话过期时间, 最长不能超过3天
        NutMap conf = WWW.read_conf(sys.io, grp);
        long ms = Math.min(conf.getLong("duration", 3600L) * 1000, 86400000L * 3);
        oSe.expireTime(System.currentTimeMillis() + ms);
        sys.io.set(oSe, "^expi$");

        // 最后输出会话信息
        if (params.is("json")) {
            sys.out.println(Json.toJson(se, _u_json_fmt));
        }
        // 仅仅输出 ID
        else {
            sys.out.println(oSe.id());
        }

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
        oU.remove("salt");
        oU.remove("passwd");
        sys.out.println(Json.toJson(oU, _u_json_fmt));
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
        if ("true".equals(s)) {
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
