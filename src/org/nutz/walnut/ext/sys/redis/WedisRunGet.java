package org.nutz.walnut.ext.sys.redis;

import redis.clients.jedis.Jedis;

public interface WedisRunGet<T> {

    T exec(Jedis jed);

}
