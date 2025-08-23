package com.site0.walnut.core.mapping.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.core.refer.redis.RedisReferService;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wobj;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web.WnConfig;

public class WnVoBMOptions extends WnVofsOptions {

    private final static Pattern _P = Pattern.compile("^(s3|cos|oss|obs|kodo)"
                                                      + ":(sha1)"
                                                      + ":([1-9]*)"
                                                      + ":([^#]+)#([^@]+)"
                                                      + "(@(redis))?$");

    // public String osType;
    public String signAlg;
    public String parts;
    // public String domainHomePath;
    // public String configName;
    public String referSercieType;

    public WnVoBMOptions(String str) {
        if (!Ws.isBlank(str)) {
            Matcher m = _P.matcher(str);
            if (!m.find()) {
                throw Er.create("e.vobm.InvalidSetting", str);
            }
            osType = m.group(1);
            signAlg = m.group(2);
            parts = m.group(3);
            domainHomePath = m.group(4).trim();
            configName = m.group(5).trim();
            referSercieType = Ws.sBlank(m.group(7), "redis").trim();
        }
        assertValid();
    }

    /**
     * 本管理器需要一个引用计数服务，以便归零时清除内容。<br>
     * 默认的， 它将采用 `RedisReferService` 其配置文件，存放在:
     * `{/home/demo}/.redis/{test}.io.refer.json`
     * 
     * 当然，假设，我们支持了某种其他的引用计数服务，譬如叫 `@abc`<br>
     * 其配置文件，存放在 `{/home/demo}/.{abc}/{test}.io.refer.json`
     * 
     * 同时为了考虑安全性，我们不能让随便一个用户采用 `@redis` 作为引用计数服务，
     * 因为他们可能将全局引用计数搞乱掉。除非在配置文件中显式的声明了
     * 
     * <pre>
     * # 允许任何用户与使用系统内置的 Redis 
     * redis-host-allow-domain=*
     * 
     * # 允许某几个指定用户域使用系统内置的 Redis 
     * redis-host-allow-domain=demo,admin
     * 
     * # 如果用户被允许连接系统内置的 Redis 
     * # 可以用这个选项限制能连接的数据库，如果不指定，则可以连接任何数据库
     * redis-database-allow=5,6
     * </pre>
     * 
     * 当然，如果用户声明的 Redis 连接与系统的 `redis-host` 不一致，那么就随便连接。<br>
     * 因为他的确可以自己购买或者搭建自己的 Redis 服务，并仅提供给自己的域来使用
     */
    public WnReferApi getReferApi(WnIo io, WnConfig config) {
        // 获取当前用户域
        String domain = Wn.WC().getMyGroup();

        // 未能找到用户域，说明当前线程并未登录
        if (Ws.isBlank(domain)) {
            throw Er.create("e.io.VoBM.WithoutDomain");
        }

        // 读取域配置文件：当然，当前线程没有权限，则会自然抛错
        String _ph = String.format("%s/.%s/%s.io.refer.json",
                                   domainHomePath,
                                   referSercieType,
                                   configName);
        WnObj oConf = io.check(null, _ph);
        WedisConfig wedisConf = io.readJson(oConf, WedisConfig.class);

        // root 域不检查
        if (!"root".equals(domain)) {
            // 获取系统的 Redis 连接, 看看是否允许当前域使用内置的 Redis
            String sysRedisHost = config.get("redis-host", "localhost");
            String sysRedisPort = config.get("redis-port", "6379");
            String sysRedisLink = sysRedisHost + ":" + sysRedisPort;

            // 如果它要连接系统的 Redis 服务，那么需要检查
            if (sysRedisLink.equals(wedisConf.getLink())) {
                // 允许的 Domain (必须是显式的指明允许的域)
                // 系统管理员需要明确的知道，有那几个域是可以允许对方乱搞的，因为那可能就是他自己
                String allowDomains = config.get("redis-host-allow-domain", "");
                if (!"*".equals(allowDomains)) {
                    String[] allows = Ws.splitIgnoreBlank(allowDomains);
                    if (Wlang.indexOf(allows, domain) < 0) {
                        throw Er.create("e.io.VoBM.DomainNoAllowed",
                                        domain + "!=>" + allowDomains);
                    }
                }
                // 允许的 database (默认就是 *)
                String allowDatabases = config.get("redis-database-allow", "*");
                if (!"*".equals(allowDatabases)) {
                    String myDB = "" + wedisConf.getDatabase();
                    String[] allows = Ws.splitIgnoreBlank(allowDatabases);
                    if (Wlang.indexOf(allows, myDB) < 0) {
                        throw Er.create("e.io.VoBM.DatabaseNoAllowed",
                                        myDB + "!=>" + allowDatabases);
                    }
                }
            }
        }

        // 一切都没有问题，返回
        String pkey = Wobj.encodePathToBase64(this.toString());
        String prefix = "io:ref:" + oConf.d1() + ":" + pkey;
        return new RedisReferService(prefix, wedisConf);
    }

    void assertValid() {
        try {
            if (!osType.matches("^(s3|cos|oss|obs|kodo)$")) {
                throw Er.create("Invalid osType", this.toString());
            }
            if (!signAlg.matches("^(sha1)$")) {
                throw Er.create("Invalid signAlg", this.toString());
            }
            if (!parts.matches("^([1-9]*)$")) {
                throw Er.create("Invalid osType", this.toString());
            }
            if (!referSercieType.matches("^(redis)$")) {
                throw Er.create("Invalid referSercieType", this.toString());
            }
            if (Ws.isBlank(domainHomePath)) {
                throw Er.create("Invalid referSercieType", this.toString());
            }
            if (Ws.isBlank(configName)) {
                throw Er.create("Invalid referSercieType", this.toString());
            }
        }
        catch (Throwable e) {
            throw Er.wrap(e);
        }
    }

    public String toString() {
        Object ref = "";
        if (!Ws.isBlank(referSercieType)) {
            ref = "@" + referSercieType;
        }
        return String.format("%s:%s:%s:%s#%s%s",
                             osType,
                             signAlg,
                             parts,
                             domainHomePath,
                             configName,
                             ref);
    }

}
