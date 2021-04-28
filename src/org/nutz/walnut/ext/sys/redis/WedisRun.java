package org.nutz.walnut.ext.sys.redis;

import redis.clients.jedis.Jedis;

public interface WedisRun {

    void exec(Jedis jed);
    
}
