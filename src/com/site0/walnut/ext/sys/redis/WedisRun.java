package com.site0.walnut.ext.sys.redis;

import redis.clients.jedis.Jedis;

public interface WedisRun {

    void exec(Jedis jed);
    
}
