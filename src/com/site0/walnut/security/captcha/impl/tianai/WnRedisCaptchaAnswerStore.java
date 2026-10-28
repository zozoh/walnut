package com.site0.walnut.security.captcha.impl.tianai;

import java.util.concurrent.TimeUnit;

import org.nutz.json.Json;

import com.site0.walnut.ext.sys.redis.Wedis;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.security.captcha.WnCaptchaAnswerStore;
import com.site0.walnut.util.Ws;

import cloud.tianai.captcha.cache.CacheStore;
import cloud.tianai.captcha.common.AnyMap;
import redis.clients.jedis.params.SetParams;

/**
 * 基于 Redis（{@link Wedis}）的验证码答案存取实现。
 *
 * <p>
 * 一身二任：既实现 Walnut 自己的 {@link WnCaptchaAnswerStore}，
 * 也实现 tianai 的 {@link CacheStore}——引擎校验时读写校验数据用的
 * 就是同一套键空间（前缀 + <code>ans:</code> 区隔 Walnut 侧手工存取的答案）。
 *
 * <p>
 * 答案由生成它的节点在校验时消费，本实现走共享 Redis；
 * 若追求极致性能，可另写一个节点本地缓存的实现替换。
 */
public class WnRedisCaptchaAnswerStore implements WnCaptchaAnswerStore, CacheStore {

    private WedisConfig conf;

    private String prefix;

    public WnRedisCaptchaAnswerStore(WedisConfig conf, String prefix) {
        this.conf = conf;
        this.prefix = prefix;
    }

    /**
     * @param id
     *            验证码 id
     * @return Walnut 侧手工存取答案的键
     */
    private String _ANS_KEY(String id) {
        return this.prefix + "ans:" + id;
    }

    // ---------- WnCaptchaAnswerStore ----------

    @Override
    public void save(String id, String answer, long expire) {
        AnyMap data = AnyMap.create();
        data.put("answer", answer);
        setCache(_ANS_KEY(id), data, expire, TimeUnit.MILLISECONDS);
    }

    @Override
    public String get(String id) {
        AnyMap data = getCache(_ANS_KEY(id));
        if (null == data) {
            return null;
        }
        return data.getString("answer");
    }

    @Override
    public void remove(String id) {
        getAndRemoveCache(_ANS_KEY(id));
    }

    // ---------- CacheStore（tianai 引擎侧） ----------

    @Override
    public AnyMap getCache(String key) {
        String json = Wedis.runGet(conf, jed -> {
            return jed.get(key);
        });
        if (Ws.isBlank(json)) {
            return null;
        }
        return Json.fromJson(AnyMap.class, json);
    }

    @Override
    public AnyMap getAndRemoveCache(String key) {
        String json = Wedis.runGet(conf, jed -> {
            return jed.getDel(key);
        });
        if (Ws.isBlank(json)) {
            return null;
        }
        return Json.fromJson(AnyMap.class, json);
    }

    @Override
    public boolean setCache(String key, AnyMap data, Long expire, TimeUnit timeUnit) {
        String json = Json.toJson(data);
        String re = Wedis.runGet(conf, jed -> {
            SetParams params = new SetParams();
            if (null != expire && expire > 0) {
                params.px(timeUnit.toMillis(expire));
            }
            return jed.set(key, json, params);
        });
        return "OK".equals(re);
    }

    @Override
    public Long incr(String key, long delta, Long expire, TimeUnit timeUnit) {
        return Wedis.runGet(conf, jed -> {
            long re = jed.incrBy(key, delta);
            if (null != expire && expire > 0) {
                jed.pexpire(key, timeUnit.toMillis(expire));
            }
            return re;
        });
    }

    @Override
    public Long getLong(String key) {
        String val = Wedis.runGet(conf, jed -> {
            return jed.get(key);
        });
        if (Ws.isBlank(val)) {
            return null;
        }
        return Long.parseLong(val);
    }

    @Override
    public void close() {
        // Redis 连接池是共享资源，不归本 store 管
    }

}
