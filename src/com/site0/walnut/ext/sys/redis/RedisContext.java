package com.site0.walnut.ext.sys.redis;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.sys.redis.hdl.RedisHanlder;
import com.site0.walnut.impl.box.JvmFilterContext;

public class RedisContext extends JvmFilterContext {

    /**
     * 指定一个 result key 的表现形式，这是一个最多三位的字符串
     * 
     * 由<code>I,A,K</code> 三个字母构成
     * 
     * <ul>
     * <li><code>IAK</code> 输出 index, action, key
     * <li><code>AK</code> 输出 action, key
     * <li><code>A</code> 输出 action
     * </ul>
     * 
     * 因此你可以控制输出结果键的顺序，譬如 <code>KAI</code> 将输出 <em>key, action, index</em>
     * 
     * 输出结果键这三个段的分隔符，则在 resultKeySep 段里定义
     * 
     * 默认的，这个值为 "AK" 可以通过 <code>redis IAK</code> 来指定
     */
    private char[] resultKey;

    /**
     * 生成结果集键的字段分隔符
     * 
     * 默认的，这个值为 ":" 可以通过 <code>redis AK:</code> 来指定 即第一个不是 <code>IAK</code>
     * 的字符开始都是分隔字符串
     */
    private String resultKeySep;

    /**
     * Redis 的配置信息，默认存放在 <code>~/.redis/default.json</code> 可以通过
     * <code>redis -conf abc</code> 来指定
     */
    public WedisConfig config;

    /**
     * 每个子命令会根据自身参数，加入自己的执行逻辑，也就是 handler 在主命令结束前会统一一并调用
     */
    private List<RedisHanlder> handlers;

    public NutMap result;

    public RedisContext() {
        this.handlers = new LinkedList<>();
        this.result = new NutMap();
        this.resultKey = "AK".toCharArray();
        this.resultKeySep = ":";
    }

    public void addHandler(RedisHanlder hdl) {
        hdl.setHandleIndex(handlers.size());
        handlers.add(hdl);
    }

    public List<RedisHanlder> getHandlers() {
        return handlers;
    }

    public void setResult(RedisHanlder hdl, Object val) {
        String key = hdl.toResultKey(resultKey, resultKeySep);
        result.put(key, val);
    }

    public String getResultKey() {
        return new String(resultKey);
    }

    public void setResultKey(String resultKey) {
        this.resultKey = resultKey.toCharArray();
    }

    public String getResultKeySep() {
        return resultKeySep;
    }

    public void setResultKeySep(String resultKeySep) {
        this.resultKeySep = resultKeySep;
    }

}
