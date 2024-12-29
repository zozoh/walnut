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

请仿照上面的实现，帮我实现 redis 的Hash命令：

- HSET key field value：将哈希表 key 中的字段 field 的值设置为 value。如果字段已经存在，将更新其值。
- HGET key field：获取存储在哈希表 key 中的字段 field 的值。
- HMSET key field value [field value ...]：同时将多个字段设置为多个值（在 Redis 4.0 及以上版本中， 建议使用 HSET 的多字段设置语法）。
- HMGET key field [field ...]：获取哈希表 key 中多个字段的值。
- HINCRBY key field increment：将哈希表 key 中的字段 field 的值增加指定的整数。
- HINCRBYFLOAT key field increment：将哈希表 key 中的字段 field 的值增加指定的浮点数。
- HDEL key field [field ...]：删除哈希表 key 中的一个或多个字段。
- HEXISTS key field：检查哈希表 key 中是否存在指定的字段 field。
- HLEN key：获取哈希表 key 中字段的数量。
- HKEYS key：获取哈希表 key 中所有字段的名称。
- HVALS key：获取哈希表 key 中所有字段的值。
- HGETALL key：获取哈希表 key 中所有的字段和值。
- HSTRLEN key field：获取哈希表 key 中字段 field 的值的字符串长度。
- HSETNX key field value：仅在字段 field 不存在时设置其值。
- HSCAN key cursor [MATCH pattern] [COUNT count]：增量迭代哈希表中的字段和值。
- HRANDFIELD key [count] [WITHVALUES]：从哈希表中随机返回一个或多个字段，如果指定了 WITHVALUES，则返回字段和对应的值。

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


