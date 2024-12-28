我想做一个 redis 客户端，我实现了几个子命令如下：

```java
// ----------- redis_del.java ------------------------
package com.site0.walnut.ext.sys.redis.hdl;

import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;

public class redis_del extends RedisFilter {

    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        String[] keys = params.vals;
        if (keys.length > 0) {
            fc.addHandler(new RedisHanlder("DEL", Ws.join(keys, ",")) {
                public Object run(RedisContext fc, Jedis jed) {
                    return jed.del(keys);
                }
            });
        }
    }

}
// ----------- redis_exists.java ------------------------
package com.site0.walnut.ext.sys.redis.hdl;

import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;

public class redis_exists extends RedisFilter {
    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        String[] keys = params.vals;
        if (keys.length > 0) {
            fc.addHandler(new RedisHanlder("EXISTS ", Ws.join(keys, ",")) {
                public Object run(RedisContext fc, Jedis jed) {
                    return jed.exists(keys);
                }
            });
        }
    }
}

```

请仿照上面的实现，帮我实现 redis 的字符串命令：

SET key value：设置键的值。
GET key：获取键的值。
INCR key：将键的值增加1。
DECR key：将键的值减少1。
APPEND key value：追加数据到键的值后面。

RedisHanlder 这个抽象类构造函数有两个参数，这两个参数只是用来日志追踪的，
如实传递就好。所以如果是 SET 你应该这么写：

```java
fc.addHandler(new RedisHanlder("SET", key) {
    public Object run(RedisContext fc, Jedis jed) {
        return jed.set(key, val);
    }
});
```

对于获取参数， 你应该这么写:

```java
// 获取字符型参数，如果不存在会抛错
String key = params.val_check(0);

// 对于 value 我通常喜欢用 val 作为变量名
String val = params.val_check(1);

// 如果是 long 你可以这么写
long sec = params.val_check_long(1);
```

不要 try catch 异常，我会统一处理，因此 set 应该这么实现

```java
public class redis_set extends RedisFilter {
    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        String key = params.val_check(0);
        String val = params.val_check(1);
        fc.addHandler(new RedisHanlder("SET", key) {
            public Object run(RedisContext fc, Jedis jed) {
                return jed.set(key, val);
            }
        });
    }
}
```
