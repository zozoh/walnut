package com.site0.walnut.security.otp.impl.store;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.sys.redis.Wedis;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.security.otp.WnOTPStore;
import com.site0.walnut.security.otp.bean.WnOTPBean;
import com.site0.walnut.security.otp.bean.WnOTPType;
import com.site0.walnut.util.Ws;

import redis.clients.jedis.params.SetParams;

public class WnRedisOTPStore implements WnOTPStore {

    private String prefix;

    private WedisConfig conf;

    private int maxRetryTimes;

    public WnRedisOTPStore(WedisConfig conf, String domain) {
        this.conf = conf;
        NutMap setup = conf.setup();
        String dft_prefix = String.format("otp:%s:", Ws.sBlank(domain, ""));
        this.prefix = setup.getString("prefix", dft_prefix);
        this.maxRetryTimes = setup.getInt("maxRetryTimes", 3);
    }

    /**
     * @param key
     * @return 真正的锁键
     */
    private String _KEY(WnOTPBean otp) {
        return _KEY(otp.getType(), otp.getAccount());
    }

    private String _KEY(WnOTPType type, String account) {
        String key = WnOTPBean.getKey(type, account);
        return this.prefix + key;
    }

    @Override
    public boolean save(WnOTPBean otp) {
        // 防空
        if (null == otp)
            return false;

        // 准备操作键
        String key = _KEY(otp);

        // 再次防空
        if (Ws.isBlank(key))
            return false;

        // 准备值
        String json = Json.toJson(otp);
        long duInSec = otp.getDuration();

        // 存储
        boolean ok = Wedis.runGet(conf, jed -> {
            SetParams params = new SetParams();
            params.ex(duInSec);
            String re = jed.set(key, json, params);
            return "OK".equals(re);
        });

        // 返回
        return ok;
    }

    @Override
    public boolean remove(WnOTPBean otp) {
        if (null == otp) {
            return false;
        }
        WnOTPType type = otp.getType();
        String account = otp.getAccount();
        return remove(type, account);
    }

    @Override
    public boolean remove(WnOTPType type, String account) {
        String key = _KEY(type, account);
        long re = Wedis.runGet(conf, jed -> {
            return jed.del(key);
        });
        return 1 == re;
    }

    @Override
    public WnOTPBean get(WnOTPType type, String account) {
        String key = _KEY(type, account);
        String json = Wedis.runGet(conf, jed -> {
            return jed.get(key);
        });
        if (Ws.isBlank(json)) {
            return null;
        }
        WnOTPBean otp = Json.fromJson(WnOTPBean.class, json);
        return otp;
    }

    @Override
    public WnOTPBean check(WnOTPType type, String account, String value) {
        WnOTPBean otp = get(type, account);
        if (null == otp) {
            return null;
        }
        if (otp.getRetryTimes() >= this.maxRetryTimes) {
            remove(type, account);
            return null;
        }
        if (!otp.isSameValue(value)) {
            otp.increaseRetryTimes();
            save(otp);
            return null;
        }
        return otp;
    }

}
