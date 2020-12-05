package org.nutz.walnut.impl.lock.redis;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.lock.WnLock;
import org.nutz.walnut.api.lock.WnLockApi;
import org.nutz.walnut.api.lock.WnLockBusyException;
import org.nutz.walnut.api.lock.WnLockFailException;
import org.nutz.walnut.api.lock.WnLockNotSameException;
import org.nutz.walnut.ext.redis.Wedis;
import org.nutz.walnut.ext.redis.WedisConfig;
import org.nutz.walnut.impl.lock.WnLockObj;
import org.nutz.walnut.util.Wn;

public class RedisLockApi implements WnLockApi {

    private String prefix;

    private WedisConfig conf;

    /**
     * 获取加锁使用权的时间窗口（秒）
     * <p>
     * 即，获取使用权后，本线程会独占这个锁的读取权限，其他节点线程全都靠边站。<br>
     * 为了防止死锁，要设置一个有效期，通常不会太长，3秒我看是一个合适的时间。
     */
    private int askDuration;

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
    private long askRetryInterval;

    /**
     * 加锁失败，阻塞并重试的次数
     * <p>
     * 默认 5 次
     */
    private int askRetryTimes;

    public RedisLockApi(WedisConfig conf) {
        this.conf = conf;
        NutMap setup = conf.setup();
        this.prefix = setup.getString("prefix", "lock:");
        this.askDuration = setup.getInt("ask-du", 3);
        this.askRetryInterval = setup.getLong("ask-retry-interval", 100);
        this.askRetryTimes = setup.getInt("ask-retry-times", 5);
    }

    @Override
    public WnLock tryLock(String lockName, String owner, String hint, long duInMs)
            throws WnLockBusyException, WnLockFailException {
        // 试图占用
        long ask = canAskLock(lockName, owner);

        // 锁服务繁忙
        if (1 != ask) {
            throw new WnLockBusyException(lockName, owner, hint);
        }

        try {
            // 那么尝试获取锁
            String key = _KEY(lockName);
            String json = Wedis.runGet(conf, jed -> {
                return jed.get(key);
            });

            // 木有锁
            if (null == json) {
                return this.setLock(lockName, owner, hint, duInMs);
            }
            // 判断一下
            else {
                WnLockObj lo = Json.fromJson(WnLockObj.class, json);
                // 锁已经过期
                if (lo.isExpired()) {
                    return this.setLock(lockName, owner, hint, duInMs);
                }
                // 锁还是有效的，
                else {
                    throw new WnLockFailException(lockName, owner, hint);
                }
            }
        }
        // 无论怎样，释放请求
        finally {
            this.freeAskLock(lockName);
        }
    }

    @Override
    public WnLock getLock(String lockName) {
        String key = _KEY(lockName);
        String json = Wedis.runGet(conf, jed -> {
            return jed.get(key);
        });

        // 木有锁
        if (null == json) {
            return null;
        }
        // 去掉 privateKey 以防小人作祟
        // 对方因为不知道 privateKey，所以没法 free 这个锁
        WnLockObj lo = Json.fromJson(WnLockObj.class, json);
        lo.setPrivateKey(null);
        lo.setName(lockName);
        return lo;
    }

    @Override
    public void freeLock(WnLock lock) throws WnLockBusyException, WnLockNotSameException {
        // 防守
        if (null == lock) {
            return;
        }
        String lockName = lock.getName();
        if (null == lockName) {
            return;
        }
        // 试图占用
        long ask = canAskLock(lockName, lock.getOwner());

        // 锁服务繁忙
        if (1 != ask) {
            throw new WnLockBusyException(lock);
        }

        try {
            // 那么尝试获取锁
            String key = _KEY(lockName);
            String json = Wedis.runGet(conf, jed -> {
                return jed.get(key);
            });

            // 木有锁
            if (null == json) {
                return;
            }
            // 判断一下
            else {
                WnLockObj lo = Json.fromJson(WnLockObj.class, json);
                lo.setName(lockName);
                // 不是自己的锁
                if (!lo.isSame(lock)) {
                    throw new WnLockNotSameException(lock);
                }
                // 移除锁
                Wedis.run(conf, jed -> {
                    jed.del(key);
                });
            }
        }
        // 无论怎样，释放请求
        finally {
            this.freeAskLock(lockName);
        }
    }

    private WnLock setLock(String lockName, String owner, String hint, long duInMs) {
        String key = _KEY(lockName);
        long now = Wn.now();
        long expi = now + duInMs;
        WnLockObj lo = new WnLockObj();
        lo.setHoldTime(now);
        lo.setExpiTime(expi);
        lo.setOwner(owner);
        lo.setHint(hint);
        lo.genPrivateKey();
        String json = Json.toJson(lo, JsonFormat.compact().setQuoteName(false));
        Wedis.run(conf, jed -> {
            jed.set(key, json);
            jed.expireAt(key, expi);
        });
        lo.setName(lockName);
        return lo;
    }

    private void freeAskLock(String lockName) {
        String askKey = _ASK_KEY(lockName);
        Wedis.run(conf, jed -> {
            jed.del(askKey);
        });
    }

    private long __canAskLock(String lockName, String owner) {
        String askKey = _ASK_KEY(lockName);
        long ask = Wedis.runGet(conf, jed -> {
            Long askRe = jed.setnx(askKey, owner);
            // 申请成功，那么设个过期时间，超过这个时间无论怎样，都让这个锁失效
            if (1 == askRe) {
                jed.expire(askKey, askDuration);
            }
            return askRe;
        });
        return ask;
    }

    /**
     * 加锁前，都要通过这个函数先占一下坑。因为 Redis "setnx" 是原子性的，所以本操作是跨节点安全的
     * 
     * @param lockName
     *            锁名
     * @param owner
     *            操作者
     * @return 是否可以尝试加锁
     */
    private long canAskLock(String lockName, String owner) {
        long ask = __canAskLock(lockName, owner);

        // 未成功获取权限，阻塞，并重试
        if (ask != 1 && this.askRetryInterval > 0 && this.askRetryTimes > 0) {
            int retryCount = 0;
            do {
                // 超过最大重试次数
                if (retryCount >= this.askRetryTimes) {
                    break;
                }
                // 计数
                retryCount++;
                // 休眠
                try {
                    Thread.sleep(this.askRetryInterval);
                }
                catch (InterruptedException e) {
                    return 0;
                }
                if ((ask = __canAskLock(lockName, owner)) == 1) {
                    break;
                }
            } while (true);
        }
        // 最后的结果
        return ask;
    }

    /**
     * 这个键值是每次请求锁前都要试图设置的一个字段
     * <p>
     * 如果设置成功，则表示这个锁自己可以操作。<br>
     * 等操作完成，会主动删除这个键。以便其他客户端申请锁
     * 
     * @return 加锁前申请权限用的键
     */
    private String _ASK_KEY(String lockName) {
        return this.prefix + lockName + "_ask";
    }

    /**
     * @param lockName
     * @return 真正的锁键
     */
    private String _KEY(String lockName) {
        return this.prefix + lockName;
    }
}
