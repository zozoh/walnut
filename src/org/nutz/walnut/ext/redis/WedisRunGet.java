package org.nutz.walnut.ext.redis;

import redis.clients.jedis.Jedis;

public interface WedisRunGet<T> {

    T exec(Jedis jed);

}
