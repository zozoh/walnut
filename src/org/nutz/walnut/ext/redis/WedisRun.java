package org.nutz.walnut.ext.redis;

import redis.clients.jedis.Jedis;

public interface WedisRun<T> {

    T run(Jedis jed);

}
