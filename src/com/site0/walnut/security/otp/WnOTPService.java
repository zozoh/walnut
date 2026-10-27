package com.site0.walnut.security.otp;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.redis.Wedis;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.security.otp.impl.builder.WnCharOTPBuilder;
import com.site0.walnut.security.otp.impl.builder.WnNumberOTPBuilder;
import com.site0.walnut.security.otp.impl.sender.WnSysEmailOTPSender;
import com.site0.walnut.security.otp.impl.store.WnRedisOTPStore;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

/**
 * 动态密码服务：按 <code>~/.otp/{name}.json5</code> 里的 sender / builder /
 * store 三段配置，组装好发送器、生成器、存储器，供业务代码直接使用。
 *
 * <p>
 * 调用方提供一个配置名和运行时（<code>WnSystem</code> 或
 * <code>WnIo + sessionVars</code>）即可得到一个实例：
 *
 * <pre>
 * WnOTPService otps = new WnOTPService(sys, "default");
 * WnOTPBean otp = otps.builder.make(WnOTPType.EMAIL, "abc@xyz.com");
 * otps.store.save(otp);
 * otps.sender.send(otp);
 * </pre>
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnOTPService {

    /**
     * 动态密码发送器（按 sender.type 决定实现类）
     */
    public WnOTPSender sender;

    /**
     * 动态密码生成器（按 builder.type 决定实现类）
     */
    public WnOTPBuilder builder;

    /**
     * 动态密码存储器（按 store.type 决定实现类）
     */
    public WnOTPStore store;

    private WnIo io;

    private NutBean vars;

    private String name;

    // 懒加载
    private NutMap config;

    public WnOTPService(WnSystem sys) {
        this(sys, "default");
    }

    public WnOTPService(WnSystem sys, String name) {
        this(sys.io, sys.session.getEnv(), name);
    }

    public WnOTPService(WnIo io, NutBean sessionVars) {
        this(io, sessionVars, "default");
    }

    public WnOTPService(WnIo io, NutBean sessionVars, String name) {
        this.io = io;
        this.vars = sessionVars;
        this.name = Ws.sBlank(name, "default");
        // 组装三个角色
        this.sender = createSender();
        this.builder = createBuilder();
        this.store = createStore();
    }

    /**
     * @return <code>~/.otp/{name}.json5</code> 的服务配置（懒加载，且只读一次）
     */
    private NutMap getConfig() {
        if (null == config) {
            String ph = Wn.appendPath("~/.otp", name + ".json5");
            WnObj oConf = Wn.checkObj(io, vars, ph);
            config = io.readJson(oConf, NutMap.class);
            if (null == config) {
                throw Er.create("e.otp.BlankConfig", ph);
            }
        }
        return config;
    }

    /**
     * @return sender 配置段，缺省时给一个空的，走各 type 的默认值
     */
    private NutMap getSenderConfig() {
        NutMap conf = getConfig().getAs("sender", NutMap.class);
        return null == conf ? new NutMap() : conf;
    }

    /**
     * @return builder 配置段，缺省时给一个空的，走各 type 的默认值
     */
    private NutMap getBuilderConfig() {
        NutMap conf = getConfig().getAs("builder", NutMap.class);
        return null == conf ? new NutMap() : conf;
    }

    /**
     * @return store 配置段，缺省时给一个空的，走各 type 的默认值
     */
    private NutMap getStoreConfig() {
        NutMap conf = getConfig().getAs("store", NutMap.class);
        return null == conf ? new NutMap() : conf;
    }

    /**
     * 按 sender.type 组装发送器：
     *
     * <ul>
     * <li><code>EMAIL</code> → {@link WnSysEmailOTPSender}，sender.config 为
     * 邮件发送配置名（<code>~/.otp/{config}.json5</code>）</li>
     * </ul>
     */
    private WnOTPSender createSender() {
        NutMap conf = getSenderConfig();
        String type = conf.getString("type", "EMAIL");
        String configName = conf.getString("config", "default");
        if ("EMAIL".equalsIgnoreCase(type)) {
            return new WnSysEmailOTPSender(io, vars, configName);
        }
        throw Er.create("e.otp.sender.UnknownType", type);
    }

    /**
     * 按 builder.type 组装生成器：
     *
     * <ul>
     * <li><code>num</code> → {@link WnNumberOTPBuilder}（n 位数字码）</li>
     * <li><code>char</code> → {@link WnCharOTPBuilder}（n 位字符码）</li>
     * </ul>
     */
    private WnOTPBuilder createBuilder() {
        NutMap conf = getBuilderConfig();
        String type = conf.getString("type", "num");
        int n = conf.getInt("n", 6);
        int duration = conf.getInt("duration", 300);
        if ("num".equalsIgnoreCase(type)) {
            return new WnNumberOTPBuilder(n, duration);
        }
        if ("char".equalsIgnoreCase(type)) {
            return new WnCharOTPBuilder(n, duration);
        }
        throw Er.create("e.otp.builder.UnknownType", type);
    }

    /**
     * 按 store.type 组装存储器：
     *
     * <ul>
     * <li><code>redis</code> → {@link WnRedisOTPStore}，store.config 为
     * Redis 连接配置名（<code>~/.redis/{config}.json</code>）</li>
     * </ul>
     */
    private WnOTPStore createStore() {
        NutMap conf = getStoreConfig();
        String type = conf.getString("type", "redis");
        if ("redis".equalsIgnoreCase(type)) {
            String configName = conf.getString("config", "default");
            WedisConfig redisConf = Wedis.loadConfigByName(io, configName, vars);
            // 键前缀里的域，缺省用服务配置名，可在 store.domain 覆盖
            String domain = conf.getString("domain", name);
            return new WnRedisOTPStore(redisConf, domain);
        }
        throw Er.create("e.otp.store.UnknownType", type);
    }

}
