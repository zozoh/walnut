package com.site0.walnut.impl.lock.redis;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.api.lock.WnLockBusyException;
import com.site0.walnut.api.lock.WnLockFailException;
import com.site0.walnut.api.lock.WnLockInvalidKeyException;
import com.site0.walnut.ext.sys.redis.Wedis;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.impl.lock.WnLockObj;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

import redis.clients.jedis.params.SetParams;

public class QuickRedisLockApi implements WnLockApi {

    private String prefix;

    private WedisConfig conf;

    /**
     * 加锁失败，阻塞并重试的间隔时间（毫秒）
     * <p>
     * 如果申请加锁权限失败，直接返回有点太暴力。我们的策略是：
     * 
     * <pre>
     * 重试几次，每次一定的时间间隔
     * </pre>
     * 
     * 如果依然失败，就返回失败了
     * <p>
     * 这样，非常高并发的时候，总会有一定数量请求会失败，但是会在一个合理的区间内。
     * <p>
     * 默认 100ms。 负数和零表示不重试
     */
    private long retryInterval;

    /**
     * 加锁失败，阻塞并重试的次数
     * <p>
     * 默认 5 次
     */
    private int retryTimes;

    public QuickRedisLockApi(WedisConfig conf) {
        this.conf = conf;
        NutMap setup = conf.setup();
        this.prefix = setup.getString("prefix", "lock:");
        this.retryInterval = setup.getLong("retry-interval", 100);
        this.retryTimes = setup.getInt("retry-times", 5);
    }

    @Override
    public WnLock tryLock(String lockName, String owner, String hint, long duInMs)
            throws WnLockBusyException, WnLockFailException {
        // 准备锁
        WnLockObj lo = this.genLock(lockName, owner, hint);
        String lv = lo.toValue();

        // 准备结果
        boolean ok = false;
        int retryCount = 0;
        String key = _KEY(lockName);
        long duInSec = duInMs / 1000L;

        while (!ok) {
            // 用 SET NX EX 去设置锁
            ok = Wedis.runGet(conf, jed -> {
                SetParams params = new SetParams();
                params.nx().ex(duInSec);
                String re = jed.set(key, lv, params);
                return "OK".equals(re);
            });

            // 搞定
            if (ok) {
                break;
            }

            // 失败重试
            retryCount++;

            // 超过重试次数
            if (this.retryTimes > 0 && retryCount > this.retryTimes) {
                break;
            }

            // 随机休眠
            try {
                int max = Math.min(3600000, (int) this.retryInterval);
                long ms = R.random(0, max);

                Thread.sleep(this.retryInterval + ms);
            }
            catch (InterruptedException e) {
                break;
            }
        }

        // 加锁还是失败
        if (!ok) {
            throw new WnLockFailException(lockName, owner, hint);
        }

        // 加锁成功
        return lo;
    }

    @Override
    public WnLock getLock(String lockName) {
        String key = _KEY(lockName);
        String str = Wedis.runGet(conf, jed -> {
            return jed.get(key);
        });
        if (!Ws.isBlank(str)) {
            return WnLockObj.create(lockName, str);
        }
        return null;
    }

    @Override
    public synchronized void freeLock(WnLock lock)
            throws WnLockInvalidKeyException, WnLockBusyException {
        freeLock(lock.getName(), lock.getPrivateKey());
    }

    // Lua 脚本，将判断锁和释放锁变为一步操作
    private static final String LUA_FREE_LOCK = "if redis.call('get', KEYS[1]) == ARGV[1] "
                                                + "then "
                                                + "  return redis.call('del', KEYS[1]) "
                                                + "else "
                                                + "  return 0 "
                                                + "end";

    @Override
    public void freeLock(String lockName, String privateKey)
            throws WnLockBusyException, WnLockInvalidKeyException {
        String key = _KEY(lockName);
        List<String> ks = Wlang.list(key);
        List<String> vs = Wlang.list(privateKey);
        Wedis.run(conf, jed -> {
            jed.eval(LUA_FREE_LOCK, ks, vs);
        });
    }

    @Override
    public List<WnLock> list() {
        List<WnLock> re = new LinkedList<>();
        Wedis.run(conf, jed -> {
            // TODO
        });
        return re;
    }

    private WnLockObj genLock(String lockName, String owner, String hint) {
        WnLockObj lo = new WnLockObj();
        lo.setOwner(owner);
        lo.setHint(hint);
        lo.genPrivateKey();
        lo.setName(lockName);
        return lo;
    }

    /**
     * @param lockName
     * @return 真正的锁键
     */
    private String _KEY(String lockName) {
        return this.prefix + lockName;
    }
}
