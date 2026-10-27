package com.site0.walnut.security.otp;

import org.nutz.lang.util.NutBean;
import org.nutz.web.ajax.AjaxReturn;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.redis.Wedis;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.security.otp.bean.WnOTPBean;
import com.site0.walnut.security.otp.bean.WnOtpBuilderConfig;
import com.site0.walnut.security.otp.bean.WnOtpConfig;
import com.site0.walnut.security.otp.bean.WnOtpNumBuilderConfig;
import com.site0.walnut.security.otp.bean.WnOtpRedisStoreConfig;
import com.site0.walnut.security.otp.bean.WnOtpSenderConfig;
import com.site0.walnut.security.otp.bean.WnOtpStoreConfig;
import com.site0.walnut.security.otp.bean.WnOTPType;
import com.site0.walnut.security.otp.impl.builder.WnCharOTPBuilder;
import com.site0.walnut.security.otp.impl.builder.WnNumberOTPBuilder;
import com.site0.walnut.security.otp.impl.sender.WnSysEmailOTPSender;
import com.site0.walnut.security.otp.impl.store.WnRedisOTPStore;
import com.site0.walnut.util.Wn;

/**
 * 动态密码服务：按 <code>~/.otp/{name}.json5</code> 里的 sender / builder /
 * store 三段配置组装而成，对业务只暴露一组组合函数。
 *
 * <p>
 * 调用方提供一个配置名和运行时（<code>WnSystem</code> 或
 * <code>WnIo + sessionVars</code>）即可得到一个实例：
 *
 * <pre>
 * WnOTPService otps = new WnOTPService(sys, "default");
 * WnOTPBean otp = otps.create("abc@xyz.com");
 * otps.save(otp);
 * otps.send(otp);
 * // ... 用户回填验证码 ...
 * if (otps.check("abc@xyz.com", "839214")) {
 *     // 验证通过
 * }
 * </pre>
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnOTPService {

    /**
     * 动态密码发送器（按 sender.type 装配）
     */
    private WnOTPSender sender;

    /**
     * 动态密码生成器（按 builder.type 装配）
     */
    private WnOTPBuilder builder;

    /**
     * 动态密码存储器（按 store.type 装配）
     */
    private WnOTPStore store;

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
        WnOtpConfig conf = loadConfig(io, sessionVars, name);
        this.sender = createSender(io, sessionVars, conf.getSender());
        this.builder = createBuilder(conf.getBuilder());
        this.store = createStore(io, sessionVars, name, conf.getStore());
    }

    /**
     * @return <code>~/.otp/{name}.json5</code> 的服务配置（解析成 {@link WnOtpConfig}）
     */
    private static WnOtpConfig loadConfig(WnIo io, NutBean vars, String name) {
        String ph = Wn.appendPath("~/.otp", name + ".json5");
        WnObj oConf = Wn.checkObj(io, vars, ph);
        WnOtpConfig conf = io.readJson(oConf, WnOtpConfig.class);
        if (null == conf) {
            throw Er.create("e.otp.BlankConfig", ph);
        }
        return conf;
    }

    /**
     * 按 sender.type 装配发送器：
     *
     * <ul>
     * <li><code>EMAIL</code> → {@link WnSysEmailOTPSender}，
     * sender.config 为 {@link com.site0.walnut.security.otp.bean.WnOtpEmailSenderConfig}</li>
     * </ul>
     */
    private static WnOTPSender createSender(WnIo io, NutBean vars, WnOtpSenderConfig conf) {
        String type = conf.getType();
        if ("EMAIL".equalsIgnoreCase(type)) {
            return new WnSysEmailOTPSender(io, vars, conf.getConfig());
        }
        throw Er.create("e.otp.sender.UnknownType", type);
    }

    /**
     * 按 builder.type 装配生成器：
     *
     * <ul>
     * <li><code>num</code> → {@link WnNumberOTPBuilder}（n 位数字码）</li>
     * <li><code>char</code> → {@link WnCharOTPBuilder}（n 位字符码）</li>
     * </ul>
     */
    private static WnOTPBuilder createBuilder(WnOtpBuilderConfig conf) {
        String type = conf.getType();
        WnOtpNumBuilderConfig c = conf.getConfig();
        if ("num".equalsIgnoreCase(type)) {
            return new WnNumberOTPBuilder(c.getN(), c.getDuration());
        }
        if ("char".equalsIgnoreCase(type)) {
            return new WnCharOTPBuilder(c.getN(), c.getDuration());
        }
        throw Er.create("e.otp.builder.UnknownType", type);
    }

    /**
     * 按 store.type 装配存储器：
     *
     * <ul>
     * <li><code>redis</code> → {@link WnRedisOTPStore}，
     * store.config 为 {@link WnOtpRedisStoreConfig}（Redis 连接配置名 + 键前缀）</li>
     * </ul>
     */
    private static WnOTPStore createStore(WnIo io, NutBean vars, String name, WnOtpStoreConfig conf) {
        String type = conf.getType();
        if ("redis".equalsIgnoreCase(type)) {
            WnOtpRedisStoreConfig c = conf.getConfig();
            WedisConfig redisConf = Wedis.loadConfigByName(io, c.getName(), vars);
            // 键前缀，缺省 otp:{服务配置名}:
            String prefix = c.hasPrefix() ? c.getPrefix() : "otp:" + name + ":";
            return new WnRedisOTPStore(redisConf, prefix, c.getMaxRetryTimes());
        }
        throw Er.create("e.otp.store.UnknownType", type);
    }

    /**
     * @return 本服务处理的是哪种类型的动态密码（由发送器决定）
     */
    public WnOTPType type() {
        return sender.getType();
    }

    /**
     * 生成一个动态密码
     *
     * @param account
     *            账号（手机号或邮箱）
     * @return 动态密码信息
     */
    public WnOTPBean create(String account) {
        WnOTPType type = type();
        return builder.make(type, account);
    }

    /**
     * 发送动态密码
     *
     * @param otp
     *            动态密码信息
     * @return 发送结果
     */
    public AjaxReturn send(WnOTPBean otp) {
        return sender.send(otp);
    }

    /**
     * 持久化动态密码（同账号旧码被覆盖，「一发一验」）
     *
     * @param otp
     *            动态密码信息
     * @return 是否保存成功
     */
    public boolean save(WnOTPBean otp) {
        return store.save(otp);
    }

    /**
     * 删除一个账号的动态密码
     *
     * @param account
     *            账号
     * @return 是否删除成功（本来就不存在也算失败）
     */
    public boolean remove(String account) {
        WnOTPType type = type();
        return store.remove(type, account);
    }

    /**
     * 获取一个账号当前有效的动态密码
     *
     * @param account
     *            账号
     * @return 动态密码信息，不存在或已过期返回 <code>null</code>
     */
    public WnOTPBean get(String account) {
        WnOTPType type = type();
        return store.get(type, account);
    }

    /**
     * 检查用户输入的动态密码对不对。
     *
     * <p>
     * 组合 {@link #get(String)} / {@link #remove(String)} 实现：校验成功返回
     * <code>true</code> 并删除动态密码（一次性消费）；值不对则累计重试次数
     * （重试上限由存储实现管理，比如 Redis 实现默认 3 次，超限自动删除）；
     * 动态密码不存在、已过期或已作废时返回 <code>false</code>
     *
     * @param account
     *            账号
     * @param value
     *            用户输入的验证码
     * @return 是否验证通过
     */
    public boolean check(String account, String value) {
        WnOTPType type = type();
        WnOTPBean otp = store.check(type, account, value);
        // 不存在 / 已过期 / 已作废 / 值不对
        if (null == otp) {
            return false;
        }
        // 验证通过，动态密码一次性消费
        remove(account);
        return true;
    }

}
